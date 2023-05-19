package org.hango.cloud.dashboard.webservice

import com.predic8.schema.*
import com.predic8.schema.creator.AbstractSchemaCreator
import com.predic8.schema.restriction.BaseRestriction
import com.predic8.schema.restriction.facet.*
import com.predic8.soamodel.Consts
import com.predic8.soamodel.ModelAccessException
import groovy.xml.MarkupBuilderHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SoapTemplateCreator extends AbstractSchemaCreator<SoapCreatorContext> {

    private static final Logger log = LoggerFactory.getLogger(SoapTemplateCreator.class)


    void createElement(Element element, SoapCreatorContext ctx) {
        ctx.elements.add(element)
        if (ctx.elements.findAll { it == ctx.element }.size() >= ctx.maxRecursionDepth) {
            yield("""\n<!-- Element ${ctx.element.name} has been defined recursivly and will not be created more than ${ctx.maxRecursionDepth} times.
				For more repetition increase the  maxRecursionDepth value in the CreatorContext.-->""")
            return
        }

        boolean isArray = false
        boolean isOptional = false
        // 例如：<xsd:element ref="tns:Book" minOccurs="0" maxOccurs="1"/>
        // 指明了属性 minOccurs与maxOccurs，但不包括真正的element定义
        // element是ref类型，这时候需要找到element的真正类型，使用refElement指向真正element定义
        def refElement = element.ref ? element.schema.getElement(element.ref) : element
        if (element.minOccurs == element.maxOccurs && element.maxOccurs != '1') {
            ctx.analyzerContext.arrayElements.add(refElement)
            yield("\n<!-- must occur exact ${element.minOccurs} times -->")
            isArray = true
            ctx.isArrayItem = true
        } else if (element.minOccurs == '0' && element.maxOccurs == '1') {
            ctx.analyzerContext.optionalElements.add(refElement)
            yield("\n<!-- optional -->")
            isOptional = true
        } else if (element.minOccurs != element.maxOccurs) {
            ctx.analyzerContext.arrayElements.add(refElement)
            yield("\n<!-- from ${element.minOccurs} to ${element.maxOccurs} -->")
            isArray = true
            ctx.isArrayItem = true
        }
        if (element.nillable) {
            ctx.analyzerContext.nullableElements.add(refElement)
            yield("\n<!-- This element may be left empty if xsi:nil='true' is set. -->")
        }
        log.debug "Element ${refElement?.name}"
        if (isArray) {
            def arrayPath = "${ctx.path}".substring(0, "${ctx.path}".length() - 1)
            def itemName = "${refElement.name}Item"
            ctx.path = "${itemName}."
            yield("\n{% for ${itemName} in ${arrayPath} %}")
        }
        if (isOptional) {
            def path = "${ctx.path}${refElement.name}"
            yield("\n{% if ${path} %}")
        }
        if (element.ref) {
            element.schema.getElement(element.ref).create(this, ctx)
        } else {
            ctx.element = element
            ctx.analyzerContext.allElements.add(element)

            if (element.fixedValue != null) {    // != null for empty strings
                yield("\n<!-- this element has a fixed value -->")
                builder."${getElementTagName(element, ctx)}"(element.fixedValue,)

            } else if (element.defaultValue != null) {    // != null for empty strings
                yield("\n<!-- this element has a default value -->")
                builder."${getElementTagName(element, ctx)}"(element.defaultValue,)
            } else if (element.embeddedType) {
                element.embeddedType.create(this, ctx)
            } else if (element.type) {
                def refType = element.schema.getType(element.type)
                if (refType && !(refType instanceof BuiltInSchemaType)) {
                    refType.create(this, ctx)
                } else if (refType && (refType instanceof BuiltInSchemaType)) {
                    def attrs = [:]
                    declNSifNeeded(getNSPrefix(element, ctx), element.namespaceUri, attrs, ctx)
                    if (element.type.localPart == 'dateTime') yield('<!--dateTime-->')
                    builder."${getElementTagName(element, ctx)}"(TemplateUtil.getTemplateValue(element.name, ctx), attrs)
                }
            } else if (!element.type && !element.embeddedType) {
                builder."${getElementTagName(element, ctx)}"()
            }
        }
        if (isOptional) {
            yield('\n{% endif %}')
        }
        if (isArray) {
            yield('\n{% endfor %}')
        }
    }

    void createComplexType(ComplexType complexType, SoapCreatorContext ctx) {
        log.debug "ComplexType ${complexType?.name}"
        def schema = complexType.schema
        if (!ctx.isArrayItem || !ctx.ignoreArrayParent) {
            ctx.path = "${ctx.path}${ctx.element.name}."
        }
        if (ctx.isArrayItem) ctx.isArrayItem = false
        if (complexType.model instanceof ComplexContent && complexType.model.hasRestriction()) {
            complexType.model?.create(this, ctx)
            complexType.anyAttribute?.create(this, ctx)
            return
        }

        def attrs = [:]
        declNSifNeeded(getNSPrefix(ctx.element, ctx), ctx.element.namespaceUri, attrs, ctx)
        attrs.putAll(createAttributes(complexType, ctx))
        builder."${getElementTagName(ctx.element, ctx)}"(attrs) {
            complexType.model?.create(this, ctx)
            complexType.anyAttribute?.create(this, ctx)
        }
    }

    Map createAttributes(Object obj, SoapCreatorContext ctx) {
        def res = [:]
        def attrs = obj.allAttributes
        attrs.each {
            def attr = it.ref ? obj.schema.getAttribute(it.ref) : it
            if (attr.fixedValue) {
                res[attr.name] = attr.fixedValue
                return
            }
            if (attr.simpleType) {
                if (attr.simpleType.restriction) {
                    res[attr.name] = TemplateUtil.getAttrTemplateValue(attr.name, ctx)
                }
            } else {
                declNSifNeeded(getNSPrefix(attr, ctx), attr.namespaceUri, res, ctx)
                res["${getNSPrefix(attr, ctx)}:${attr.name}"] = TemplateUtil.getAttrTemplateValue(attr.name, ctx)
            }
        }
        res
    }

    void createSimpleRestriction(BaseRestriction restriction, SoapCreatorContext ctx) {
        if (restriction.facets) {
            yield("\n<!-- ")
            restriction.facets.each {
                it.create(this, ctx)
                if (it == restriction.facets.last()) return
                yield(", ")
            }
            yield(" -->")
        }
        buildElement(ctx, text: '???')
    }

    void createEnumerationFacet(EnumerationFacet facet, SoapCreatorContext ctx) {
        yield("possible value: ${facet.value}")
    }

    void createLengthFacet(LengthFacet facet, SoapCreatorContext ctx) {
        yield("maximum length: ${facet.value}")
    }

    void createTotalDigitsFacet(TotalDigitsFacet facet, SoapCreatorContext ctx) {
        yield("maximum number of digits: ${facet.value}")
    }

    void createFractionDigits(FractionDigits fdigits, SoapCreatorContext ctx) {
        yield("fraction digits: ${fdigits.value}")
    }

    void createPatternFacet(PatternFacet facet, SoapCreatorContext ctx) {
        yield("Pattern: ${facet.value}")
    }

    void createMaxLengthFacet(MaxLengthFacet facet, SoapCreatorContext ctx) {
        yield("Max Length: ${facet.value}")
    }

    void createMinLengthFacet(MinLengthFacet facet, SoapCreatorContext ctx) {
        yield("Min Lenght: ${facet.value}")
    }

    void createMaxInclusiveFacet(MaxInclusiveFacet facet, SoapCreatorContext ctx) {
        yield("Max Inclusive: ${facet.value}")
    }

    void createMinInclusiveFacet(MinInclusiveFacet facet, SoapCreatorContext ctx) {
        yield("Min Inclusive: ${facet.value}")
    }

    void createMaxExclusiveFacet(MaxExclusiveFacet facet, SoapCreatorContext ctx) {
        yield("Max Exclusive: ${facet.value}")
    }

    void createMinExclusiveFacet(MinExclusiveFacet facet, SoapCreatorContext ctx) {
        yield("Min Exclusive: ${facet.value}")
    }

    void createWhiteSpaceFacet(WhiteSpaceFacet facet, SoapCreatorContext ctx) {
        yield("White Space: ${facet.value}")
    }

    void createExtension(Extension extension, SoapCreatorContext ctx) {
        if (extension.base.namespaceURI.equals(Consts.SCHEMA_NS)) {
            yield("${TemplateUtil.getBaseTemplateValue(ctx)}")
            return
        }
        def baseType = extension.schema.getType(extension.base)
        if (!baseType) throw new ModelAccessException("Could not find the referenced type '${extension.basePN}' in schema '${extension.schema.targetNamespace}'.")
        if (baseType instanceof SimpleType) return
        baseType.model?.create(this, ctx)
        extension.anyAttribute?.create(this, ctx)
        extension.model?.create(this, ctx)
    }

    void createAnnotation(Annotation annotation, SoapCreatorContext ctx) {
        annotation.documentations.each { yield("<!--${it.content}-->") }
    }

    void createPart(part, SoapCreatorContext ctx) {
        if (!part.type) throw new ModelAccessException("There is no type information for the part '${part.name}' although the referencing operation was declared as RPC style.")
        if (part.type instanceof ComplexType || part.type instanceof SimpleType) {
            part.type.create(this, ctx)
            return
        }
        if (part.type instanceof BuiltInSchemaType) {
            if (part.type.qname.localPart == 'dateTime') yield('<!--dateTime-->')
            builder."${part.name}"(TemplateUtil.getTemplateValue(part.name, ctx))
        }
    }

    void createSimpleContent(SimpleContent simpleContent, SoapCreatorContext ctx) {
        simpleContent.extension?.create(this, ctx)
        simpleContent.restriction?.create(this, ctx)
    }

    void createAny(Any any, SoapCreatorContext ctx) {
        /*TODO
         * Change the yield calls to comment, like the followin:
         * yield("<!-- This element can be extended by any element from ${any.namespace ?: 'any'} namespace -->")
         */
        builder.mkp.comment "This element can be extended by any element from ${any.namespace ?: 'any'} namespace"
    }

    void createAnyAttribute(AnyAttribute anyAttribute, SoapCreatorContext ctx) {
        yield("<!-- This element can be extended by any attribute from ${anyAttribute.namespace ?: 'any'} namespace -->")
    }

    void createComplexContentRestriction(Restriction restriction, SoapCreatorContext ctx) {
        if (restriction.base) restriction.schema.getType(restriction.base).create(this, ctx)
    }

    @Override
    protected getElementTagName(Element element, ctx) {
        /*Only if the element is from the same namespace as the
        * top-level-element of the request, it doesn't need a prefix.
        */
        if (!element.toplevel && element.schema.elementFormDefault == "unqualified" && ctx.elements[0].namespaceUri == element.namespaceUri)
            return element.name
        else
            return "${getNSPrefix(element, ctx)}:${element.name}"
    }

    private yield(s) {
        new MarkupBuilderHelper(builder).yieldUnescaped(s)
    }
}
