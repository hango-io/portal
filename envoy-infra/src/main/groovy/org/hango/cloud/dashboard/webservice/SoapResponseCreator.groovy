package org.hango.cloud.dashboard.webservice

import com.predic8.soamodel.AbstractCreator
import com.predic8.soamodel.Consts
import com.predic8.wsdl.AbstractSOAPBinding
import com.predic8.wsdl.Binding
import com.predic8.wsdl.BindingOperation
import com.predic8.wsdl.Definitions
import com.predic8.wsdl.soap11.SOAPBinding as SOAP11Binding
import com.predic8.wsdl.soap11.SOAPBody as SOAP11Body
import com.predic8.wsdl.soap11.SOAPHeader as SOAP11Header
import com.predic8.wsdl.soap12.SOAPBinding as SOAP12Binding
import com.predic8.wsdl.soap12.SOAPBody as SOAP12Body
import com.predic8.wsdl.soap12.SOAPHeader as SOAP12Header
import groovy.xml.MarkupBuilder
import groovy.xml.MarkupBuilderHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Not threadsafe
 */

class SoapResponseCreator extends AbstractCreator {

    private static final Logger log = LoggerFactory.getLogger(SoapResponseCreator.class)

    public SoapResponseCreator(Definitions definitions, def creator, MarkupBuilder builder) {
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
                    if (bop.getOutput().getBindingElements().get(0).getNamespace() != null) {
                        return bop.getOutput().getBindingElements().get(0).getNamespace();
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
                it.part.element.create(creator, creatorContext)
            }
        }

    }

    private isHeaderExisting() {
        bindingOperation.output.bindingElements.find { it instanceof SOAP11Header || it instanceof SOAP12Header }
    }

    private getSoapNamespace() {
        if (binding.binding instanceof SOAP11Binding)
            return Consts.SOAP11_NS

        if (binding.binding instanceof SOAP12Binding)
            return Consts.SOAP12_NS
        ''
    }

    private getSoapPrefix() {
        if (binding.binding instanceof SOAP11Binding)
            return "s11"
        if (binding.binding instanceof SOAP12Binding)
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
        bindingOperation.output.bindingElements.findAll { it instanceof SOAP11Header || it instanceof SOAP12Header }
    }

    private getBodyElement() {
        bindingOperation.output.bindingElements.find { it instanceof SOAP11Body || it instanceof SOAP12Body }
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
