package org.hango.cloud.webservice.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.predic8.schema.Element;
import com.predic8.soamodel.ValidationError;
import com.predic8.wsdl.*;
import com.predic8.xml.util.ResourceDownloadException;
import groovy.xml.MarkupBuilder;
import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.impl.PluginServiceInfoImpl;
import org.hango.cloud.dashboard.webservice.*;
import org.hango.cloud.envoy.infra.webservice.dao.EnvoyRouteWsParamDao;
import org.hango.cloud.envoy.infra.webservice.dao.EnvoyServiceWsdlInfoDao;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyRouteWsParamInfo;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlBindingItem;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlInfo;
import org.hango.cloud.envoy.infra.webservice.service.IEnvoyWebServiceService;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE;
import static org.hango.cloud.envoy.infra.webservice.service.impl.EnvoyWebServiceServiceImpl.WS_PLUGIN_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Xin Li
 * @date 2023/1/11 12:34
 */
@SpringBootTest
public class EnvoyWebServiceServiceImplTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyWebServiceServiceImplTest.class);

    private long mockId = 1L;

    private long mockVirtualGwId = 1L;

    private long mockServiceId = 1L;

    private long mockRouteId = 1L;

    private String mockRequestTemplate = "mockRequestTemplate";

    private List<String> mockResponseArrayTypeList = Arrays.asList("{http://www.cleverbuilder.com/BookService/}:AddBookResponse");

    private String mockWsPortType = "BookService";

    private String mockWsOperation = "AddBook";

    private String mockWsBinding = "BookServiceSOAP";

    private String mockWsAddress = "http://localhost:26999/services/soap/book";

    private String mockWsdlFileName = "mockWsdlFileName.xml";

    private String mockWsdlFileContent = "mockWsdlFileContent";

    private String mockPluginConfiguration = "mockPluginConfiguration";
    private List<EnvoyServiceWsdlBindingItem> mockWsdlBindingList = Collections.singletonList(new EnvoyServiceWsdlBindingItem());
    @Autowired
    private IEnvoyWebServiceService envoyWebServiceService;

    @Autowired
    private PluginServiceInfoImpl pluginServiceInfoService;

    private static volatile boolean flag;
    @Autowired
    private EnvoyRouteWsParamDao envoyRouteWsParamDao;

    @Autowired
    private EnvoyServiceWsdlInfoDao envoyServiceWsdlInfoDao;

    @Autowired
    private IPluginBindingInfoDao pluginBindingInfoDao;

    @PostConstruct
    public void init() {
        synchronized (EnvoyWebServiceServiceImplTest.class) {
            if (!flag) {
                MockitoAnnotations.openMocks(this);
                EnvoyRouteWsParamInfo envoyRouteWsParamInfo = new EnvoyRouteWsParamInfo();
                envoyRouteWsParamInfo.setId(mockId);
                envoyRouteWsParamInfo.setVirtualGwId(mockVirtualGwId);
                envoyRouteWsParamInfo.setServiceId(mockServiceId);
                envoyRouteWsParamInfo.setRouteId(mockRouteId);
                envoyRouteWsParamInfo.setRequestTemplate(mockRequestTemplate);
                envoyRouteWsParamInfo.setResponseArrayTypeList(mockResponseArrayTypeList);
                envoyRouteWsParamInfo.setWsPortType(mockWsPortType);
                envoyRouteWsParamInfo.setWsOperation(mockWsOperation);
                envoyRouteWsParamInfo.setWsBinding(mockWsBinding);
                envoyRouteWsParamInfo.setWsAddress(mockWsAddress);
                envoyRouteWsParamDao.add(envoyRouteWsParamInfo);

                EnvoyServiceWsdlInfo envoyServiceWsdlInfo = new EnvoyServiceWsdlInfo();
                envoyServiceWsdlInfo.setVirtualGwId(mockVirtualGwId);
                envoyServiceWsdlInfo.setServiceId(mockServiceId);
                envoyServiceWsdlInfo.setWsdlFileName(mockWsdlFileName);
                envoyServiceWsdlInfo.setWsdlFileContent(mockWsdlFileContent);
                envoyServiceWsdlInfo.setWsdlBindingList(mockWsdlBindingList);
                envoyServiceWsdlInfoDao.add(envoyServiceWsdlInfo);

                //协议转换插件配置
                PluginBindingInfo pluginBindingInfo = new PluginBindingInfo();
                pluginBindingInfo.setVirtualGwId(mockVirtualGwId);
                pluginBindingInfo.setBindingObjectId(String.valueOf(mockRouteId));
                pluginBindingInfo.setBindingObjectType(BINDING_OBJECT_TYPE_ROUTE_RULE);
                pluginBindingInfo.setPluginType(WS_PLUGIN_TYPE);
                pluginBindingInfo.setPluginConfiguration(mockPluginConfiguration);
                pluginBindingInfo.setBindingStatus(PluginBindingInfo.BINDING_STATUS_DISABLE);
                pluginBindingInfo.setGwType("envoy");
                pluginBindingInfoDao.add(pluginBindingInfo);
                flag = true;
            }
        }
    }

    @Test
    public void testGetServiceWsdlInfo() {
        EnvoyServiceWsdlInfo serviceWsdlInfo = envoyWebServiceService.getServiceWsdlInfo(mockServiceId);
        assertEquals(mockWsdlFileContent, serviceWsdlInfo.getWsdlFileContent());
    }

    @Test
    public void testDeleteServiceWsdlInfo() {
        ErrorCode errorCode = envoyWebServiceService.deleteServiceWsdlInfo(mockVirtualGwId, mockServiceId);
        assertEquals(CommonErrorCode.SUCCESS, errorCode);
    }

    @Test
    public void testDeleteRouteProxyWsParam() {
        EnvoyRouteWsParamInfo routeProxyWsParam = envoyWebServiceService.getRouteProxyWsParam(mockVirtualGwId, mockServiceId, mockRouteId);
        envoyRouteWsParamDao.delete(routeProxyWsParam);
    }

    @Test
    public void testSOAModel() throws Exception {
        File wsdlDirectory = FileSystems.getDefault().getPath("src/test/resources/wsdl").toFile();
        if (wsdlDirectory.isDirectory()) {
            File[] wsdls = wsdlDirectory.listFiles();
            if (Objects.isNull(wsdls)) return;
            for (File wsdl : wsdls) {
                assertTrue(checkWsdl(wsdl.toPath()));
                //打印略
                printRequestResponseTemplate(wsdl.toPath());
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

                    logger.debug("PortType:[{}], Operation:[{}], Binding[{}], Input[{}], Output[{}]", binding.getPortType().getName(), operation.getName(), binding.getName(), input, output);
                    logger.debug("\nRequest:");
                    SoapCreatorContext requestContext = (SoapCreatorContext) creator.createRequest(binding.getPortType().getName(), operation.getName(), binding.getName());
                    logger.debug("\nResponse:");
                    SoapCreatorContext responseContext = (SoapCreatorContext) responseCreator.createRequest(binding.getPortType().getName(), operation.getName(), binding.getName());
                    logger.debug("\nRequest Elements:");
                    AnalyzerContext analyzerResult = requestContext.getAnalyzerContext();
                    logger.debug("\nAllElements:{}\nArrayElements:{}\nOptionalElements:{}\nNullableElements:{}",
                            getElementNames(analyzerResult.getAllElements()),
                            getElementNames(analyzerResult.getArrayElements()),
                            getElementNames(analyzerResult.getOptionalElements()),
                            getElementNames(analyzerResult.getNullableElements()));
                    logger.debug("\nResponse Elements:");
                    analyzerResult = responseContext.getAnalyzerContext();
                    logger.debug("\nAllElements:{}\nArrayElements:{}\nOptionalElements:{}\nNullableElements:{}",
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
