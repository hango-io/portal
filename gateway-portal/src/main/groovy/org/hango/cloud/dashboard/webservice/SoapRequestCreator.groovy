package org.hango.cloud.dashboard.webservice

import com.predic8.soamodel.AbstractCreator
import com.predic8.soamodel.Consts
import com.predic8.wsdl.AbstractSOAPBinding
import com.predic8.wsdl.Binding
import com.predic8.wsdl.BindingOperation
import com.predic8.wsdl.Definitions
import com.predic8.wsdl.soap11.SOAPBinding
import com.predic8.wsdl.soap11.SOAPBody
import com.predic8.wsdl.soap11.SOAPHeader
import groovy.xml.MarkupBuilder
import groovy.xml.MarkupBuilderHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SoapRequestCreator extends AbstractCreator {

    private static final Logger log = LoggerFactory.getLogger(SoapRequestCreator.class)

    public SoapRequestCreator(Definitions definitions, def creator, MarkupBuilder builder) {
        super();
        this.definitions = definitions;
        this.creator = creator
        this.builder = builder
    }

    Definitions definitions
    def creator
    def formParams
    int maxRecursionDepth = 2

    private String operationName
    private String bindingName
    private String portTypeName

    def wrapEnvelope(body) {
        creator.builder = builder
        builder."$soapPrefix:Envelope"("xmlns:$soapPrefix": soapNamespace) {
            if (isHeaderExisting()) {
                buildHeader(builder)
            }
            "$soapPrefix:Body"() { yieldUnescaped(body) }
        }
    }

    def createRequest(String portTypeName, String operationName, String bindingName) { //todo remove parameter list
        this.bindingName = bindingName
        this.operationName = operationName
        log.debug "createRequest"
        log.debug "bindingName $bindingName"
        log.debug "operationName $operationName"
        log.debug "portTypeName $portTypeName"
        creator.builder = builder
        log.debug "creator class : ${creator.getClass().name}"

        def out
        builder."$soapPrefix:Envelope"("xmlns:$soapPrefix": soapNamespace) {
            if (isHeaderExisting()) {
                buildHeader(builder)
            }
            out = buildBody(builder)
        }
        return out
    }

    private buildBody(builder) {
        def ctx = creatorContext
        builder."$soapPrefix:Body"() {
            log.debug "creating body"
            if (isRPC(bindingName)) {
                log.debug "isRPC"
                "ns1:$operationName"('xmlns:ns1': getNamespaceForRCPBinding()) {
                    log.debug "create body from bodyElement"
                    ctx.path = "body."
                    ctx.path = "${ctx.path}${operationName}."
                    bodyElement.parts.each {
                        it.create(creator, ctx)
                    }
                }
            } else {
                log.debug "creating body from definitions"
                log.debug "element : ${bodyElement.parts[0].element}"
                ctx.path = "body."
                bodyElement.parts[0].element.create(creator, ctx)
            }
        }
        return ctx
    }

    private getNamespaceForRCPBinding() {
        for (Binding bnd : definitions.getBindings()) {
            for (BindingOperation bop : bnd.getOperations()) {
                if (bnd.getBinding() instanceof AbstractSOAPBinding && bop.getName().equals(operationName)) {
                    if (bop.getInput().getBindingElements().get(0).getNamespace() != null) {
                        return bop.getInput().getBindingElements().get(0).getNamespace();
                    } else {
                        return definitions.targetNamespace;
                    }
                }
            }
        }
    }

    private buildHeader(builder) {
        log.debug "creating headers"
        builder."$soapPrefix:Header"() {
            getHeaderBindingElements(bindingOperation).each {
                def ctx = creatorContext
                ctx.path = "header."
                it.part.element.create(creator, ctx)
            }
        }

    }

    private isHeaderExisting() {
        bindingOperation.input.bindingElements.find { it instanceof SOAPHeader || it instanceof com.predic8.wsdl.soap12.SOAPHeader }
    }

    private getSoapNamespace() {
        if (binding.binding instanceof SOAPBinding)
            return Consts.SOAP11_NS

        if (binding.binding instanceof com.predic8.wsdl.soap12.SOAPBinding)
            return Consts.SOAP12_NS
        ''
    }

    private getSoapPrefix() {
        if (binding.binding instanceof SOAPBinding)
            return "s11"
        if (binding.binding instanceof com.predic8.wsdl.soap12.SOAPBinding)
            return "s12"
        ''
    }

    private getBindingOperation() {
        binding.getOperation(operationName)
    }

    private getBinding() {
        definitions.getBinding(bindingName)
    }

    private getHeaderBindingElements(bindingOperation) {
        bindingOperation.input.bindingElements.findAll { it instanceof SOAPHeader || it instanceof com.predic8.wsdl.soap12.SOAPHeader }
    }

    private getBodyElement() {
        bindingOperation.input.bindingElements.find { it instanceof SOAPBody || it instanceof com.predic8.wsdl.soap12.SOAPBody }
    }

    private isRPC(bindingName) {
        'rpc'.equalsIgnoreCase(definitions.getBinding(bindingName).binding.style)
    }

    private getCreatorContext() {
        new SoapCreatorContext(maxRecursionDepth: maxRecursionDepth)
    }

    private yieldUnescaped(s) {
        new MarkupBuilderHelper(builder).yieldUnescaped(s)
    }
}
