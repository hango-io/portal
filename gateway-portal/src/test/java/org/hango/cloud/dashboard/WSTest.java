package org.hango.cloud.dashboard;

import com.predic8.schema.Element;
import com.predic8.soamodel.ValidationError;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLParserContext;
import com.predic8.xml.util.ResourceDownloadException;
import groovy.xml.MarkupBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.webservice.AnalyzerContext;
import org.hango.cloud.dashboard.webservice.SoapCreatorContext;
import org.hango.cloud.dashboard.webservice.SoapRequestCreator;
import org.hango.cloud.dashboard.webservice.SoapResponseCreator;
import org.hango.cloud.dashboard.webservice.SoapTemplateCreator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WSTest {
    private static final Logger logger = LoggerFactory.getLogger(WSTest.class);

    @Test
    public void testSOAModel() throws Exception {
        File wsdlDirectory = FileSystems.getDefault().getPath("src/test/resources/wsdl").toFile();
        if (wsdlDirectory.isDirectory()) {
            File[] wsdls = wsdlDirectory.listFiles();
            if (Objects.isNull(wsdls)) return;
            for (File wsdl : wsdls) {
                if (checkWsdl(wsdl.toPath())) printRequestResponseTemplate(wsdl.toPath());
            }
        }
    }

    private boolean checkWsdl(Path wsdlPath) {
        WSDLParser parser = new WSDLParser();
        Definitions wsdlDefinitions;
        try {
            wsdlDefinitions = parser.parse(Files.newInputStream(wsdlPath));
        } catch (ResourceDownloadException e) {
            logger.warn("下载wsdl 依赖资源:{}失败，检查网络是否可到达", e.getUrl());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            logger.warn("解析wsdl文件时发生异常", e);
            e.printStackTrace();
            return false;
        }
        //校验wsdl
        WSDLParserContext parserContext = new WSDLParserContext();
        wsdlDefinitions.validate(parserContext);
        if (Objects.nonNull(parserContext.getErrors()) && parserContext.getErrors() instanceof List) {
            List<ValidationError> errors = (List<ValidationError>) parserContext.getErrors();
            if (CollectionUtils.isNotEmpty(errors)) {
                for (ValidationError error : errors) {
                    // 当error包含exception时，抛出异常到前端
                    if (Objects.nonNull(error.getCause())) {
                        error.getCause().printStackTrace();
                        return false;
                    }
                    // 其他情况只是输出warn日志
                    logger.warn("非法wsdl格式, wsdl:{} message:{}", wsdlPath.toString(), error.getMessage());
                }
            }
        }
        return true;
    }

    private void printRequestResponseTemplate(Path wsdlPath) throws Exception {
        logger.info(">>>>>>>>Processor :" + wsdlPath.toString());
        WSDLParser parser = new WSDLParser();
        Definitions wsdl = parser.parse(Files.newInputStream(wsdlPath));
        SoapRequestCreator creator = new SoapRequestCreator(wsdl, new SoapTemplateCreator(), new MarkupBuilder(new PrintWriter(System.out)));
        SoapResponseCreator responseCreator = new SoapResponseCreator(wsdl, new SoapTemplateCreator(), new MarkupBuilder(new PrintWriter(System.out)));
        for (Binding binding : wsdl.getBindings()) {
            if (Objects.equals(binding.getBinding().getProtocol(), "SOAP11") || Objects.equals(binding.getBinding().getProtocol(), "SOAP12")) {
                for (BindingOperation operation : binding.getOperations()) {
                    logger.info("\n--------------------------------------------------------------------------------------------");
                    Operation op = wsdl.getOperation(operation.getName(), binding.getPortType().getName());
                    String input = op.getInput().getMessage().getName();
                    String output = op.getOutput().getMessage().getName();

                    logger.info("PortType:[{}], Operation:[{}], Binding[{}], Input[{}], Output[{}]", binding.getPortType().getName(), operation.getName(), binding.getName(), input, output);
                    logger.info("\nRequest:");
                    SoapCreatorContext requestContext = (SoapCreatorContext) creator.createRequest(binding.getPortType().getName(), operation.getName(), binding.getName());
                    logger.info("\nResponse:");
                    SoapCreatorContext responseContext = (SoapCreatorContext) responseCreator.createRequest(binding.getPortType().getName(), operation.getName(), binding.getName());
                    logger.info("\nRequest Elements:");
                    AnalyzerContext analyzerResult = requestContext.getAnalyzerContext();
                    logger.info("\nAllElements:{}\nArrayElements:{}\nOptionalElements:{}\nNullableElements:{}",
                            getElementNames(analyzerResult.getAllElements()),
                            getElementNames(analyzerResult.getArrayElements()),
                            getElementNames(analyzerResult.getOptionalElements()),
                            getElementNames(analyzerResult.getNullableElements()));
                    logger.info("\nResponse Elements:");
                    analyzerResult = responseContext.getAnalyzerContext();
                    logger.info("\nAllElements:{}\nArrayElements:{}\nOptionalElements:{}\nNullableElements:{}",
                            getElementNames(analyzerResult.getAllElements()),
                            getElementNames(analyzerResult.getArrayElements()),
                            getElementNames(analyzerResult.getOptionalElements()),
                            getElementNames(analyzerResult.getNullableElements()));
                }
            } else {
                //log:
            }
        }
    }

    private List<String> getElementNames(Object elements) {
        return (List<String>) ((List) elements).stream().map(item -> ((Element) item).getName()).collect(Collectors.toList());
    }
}
