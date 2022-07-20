package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.Jinjava;
import com.predic8.schema.Element;
import com.predic8.soamodel.ModelAccessException;
import com.predic8.soamodel.ValidationError;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLParserContext;
import com.predic8.xml.util.ResourceDownloadException;
import groovy.xml.MarkupBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.dao.EnvoyRouteWsParamDao;
import org.hango.cloud.dashboard.envoy.dao.EnvoyServiceWsdlInfoDao;
import org.hango.cloud.dashboard.envoy.meta.BindingPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginInfo;
import org.hango.cloud.dashboard.envoy.meta.webservice.ElementInfo;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyRouteWsParamInfo;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyServiceWsdlBindingItem;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyServiceWsdlInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyWebServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class EnvoyWebServiceServiceImpl implements IEnvoyWebServiceService {
    public static final String errorCode = "errorCode";
    /**
     * 存放createWsRequestTemplate生成的请求模板
     */
    public static final String WS_REQUEST_TEMPLATE = "wsRequestTemplate";
    /**
     * 存放renderWsRequestTemplate生成的模板渲染结果
     */
    public static final String WS_TEMPLATE_RENDER_RESULT = "wsTemplateRenderResult";
    /**
     * webservice插件类型
     */
    public static final String WS_PLUGIN_TYPE = "soap-json-transcoder";
    private static final Logger logger = LoggerFactory.getLogger(EnvoyWebServiceServiceImpl.class);
    private final WSDLParser wsdlParser = new WSDLParser();
    private final Jinjava jinjava = new Jinjava();
    @Autowired
    private EnvoyServiceWsdlInfoDao envoyServiceWsdlInfoDao;
    @Autowired
    private EnvoyRouteWsParamDao envoyRouteWsParamDao;
    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;

    @Override
    @SuppressWarnings("unchecked")
    public ErrorCode checkUploadWsdlFile(long gwId, long serviceId, MultipartFile wsdlFile) {
        //文件是否为空校验
        if (wsdlFile == null || wsdlFile.isEmpty()) {
            return CommonErrorCode.FileIsEmpty;
        }
        String fileName = wsdlFile.getOriginalFilename();
        String fileType = null;
        if (fileName.contains(".")) {
            fileType = fileName.substring(fileName.lastIndexOf("."));
        }
        //文件格式校验
        if (!".xml".equals(fileType) && !".wsdl".equals(fileType)) {
            return CommonErrorCode.IllegalFileFormat;
        }
        String fileContent;
        try {
            byte[] bytes = wsdlFile.getBytes();
            fileContent = new String(bytes, Const.DEFAULT_ENCODING);
            if (StringUtils.isBlank(fileContent)) {
                return CommonErrorCode.IllegalWsdlFormat("The contents of the wsdl are empty");
            }
        } catch (IOException e) {
            logger.warn("读取wsdl文件内容时发生异常", e);
            return CommonErrorCode.InternalServerError;
        }
        //解析wsdl文件
        Definitions wsdlDefinitions;
        try {
            wsdlDefinitions = wsdlParser.parse(new ByteArrayInputStream(fileContent.getBytes()));
        } catch (ResourceDownloadException e) {
            logger.warn("下载wsdl 依赖资源:{}失败，检查网络是否可到达", e.getUrl());
            e.printStackTrace();
            return CommonErrorCode.ResourceDownloadFailed(e.getUrl());
        } catch (Exception e) {
            logger.warn("解析wsdl文件时发生异常", e);
            e.printStackTrace();
            return CommonErrorCode.IllegalWsdlFormat(e.getMessage());
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
                        return CommonErrorCode.IllegalWsdlFormat(errors.get(0).getMessage());
                    }
                    // 其他情况只是输出warn日志
                    logger.warn("非法wsdl格式, wsdl:{} message:{}", fileName, error.getMessage());
                }
            }
        }
        //校验是否有非Soap类型的binding
        if (hasNonSoapBinding(wsdlDefinitions)) {
            // 不支持非soap类型的binding
            logger.warn("wsdl不支持非soap类型的binding（例如httpBinding）");
        }
        //新增或覆盖envoyServiceWsInfo

        try {
            EnvoyServiceWsdlInfo wsdlInfo = createEnvoyServiceWsInfo(gwId, serviceId, fileName, fileContent, wsdlDefinitions);
            EnvoyServiceWsdlInfo oldWsdlInfo = envoyServiceWsdlInfoDao.getByServiceId(gwId, serviceId);
            if (Objects.nonNull(oldWsdlInfo)) {
                oldWsdlInfo.setModifyDate(wsdlInfo.getModifyDate());
                oldWsdlInfo.setWsdlFileName(wsdlInfo.getWsdlFileName());
                oldWsdlInfo.setWsdlFileContent(wsdlInfo.getWsdlFileContent());
                oldWsdlInfo.setWsdlBindingList(wsdlInfo.getWsdlBindingList());
                envoyServiceWsdlInfoDao.update(oldWsdlInfo);
            } else {
                envoyServiceWsdlInfoDao.add(wsdlInfo);
            }
        } catch (ModelAccessException e) {
            logger.warn("An exception occurred while parsing an element of WSDL");
            e.printStackTrace();
            return CommonErrorCode.IllegalWsdlFormat(e.getMessage());
        }

        return CommonErrorCode.Success;
    }

    @Override
    public EnvoyServiceWsdlInfo getServiceWsdlInfo(long gwId, long serviceId) {
        return envoyServiceWsdlInfoDao.getByServiceId(gwId, serviceId);
    }

    @Override
    public ErrorCode deleteServiceWsdlInfo(long gwId, long serviceId) {
        // 获取服务上的wsdl信息
        EnvoyServiceWsdlInfo wsdlInfo = getServiceWsdlInfo(gwId, serviceId);
        if (Objects.isNull(wsdlInfo)) {
            return CommonErrorCode.NoSuchWsdlInfo;
        }
        envoyServiceWsdlInfoDao.deleteByServiceId(gwId, serviceId);
        return CommonErrorCode.Success;
    }

    @Override
    public EnvoyRouteWsParamInfo getRouteProxyWsParam(long gwId, long serviceId, long routeId) {
        return envoyRouteWsParamDao.getByRoute(gwId, serviceId, routeId);
    }

    @Override
    public ErrorCode updateRouteProxyWsParam(EnvoyRouteWsParamInfo wsParamInfo) {
        long gwId = wsParamInfo.getGwId();
        long serviceId = wsParamInfo.getServiceId();
        long routeId = wsParamInfo.getRouteId();
        // 获取服务上的wsdl信息
        EnvoyServiceWsdlInfo wsdlInfo = getServiceWsdlInfo(gwId, serviceId);
        if (Objects.isNull(wsdlInfo)) {
            return CommonErrorCode.NoSuchWsdlInfo;
        }
        // 校验webservice信息里是否包含指定binding参数
        Optional<EnvoyServiceWsdlBindingItem> bindingOptional = wsdlInfo.getWsdlBindingList().stream().filter(item ->
                Objects.equals(wsParamInfo.getWsPortType(), item.getPortType()) &&
                        Objects.equals(wsParamInfo.getWsOperation(), item.getOperation()) &&
                        Objects.equals(wsParamInfo.getWsBinding(), item.getBinding())).findFirst();
        if (!bindingOptional.isPresent()) {
            return CommonErrorCode.IllegalBindingParam;
        }

        // 生成插件配置
        String pluginConfiguration = generatePluginConfiguration(wsParamInfo, wsdlInfo, bindingOptional.get());

        // 更新插件配置
        boolean pluginBindingUpdateResult;
        EnvoyPluginBindingInfo oldPluginBindingInfo = getWsPluginBindingInfo(gwId, serviceId, routeId);
        EnvoyPluginBindingInfo pluginBindingInfo = createEnvoyPluginBindingInfo(gwId, serviceId, routeId, pluginConfiguration);
        if (Objects.nonNull(oldPluginBindingInfo)) {
            oldPluginBindingInfo.setUpdateTime(pluginBindingInfo.getUpdateTime());
            oldPluginBindingInfo.setPluginConfiguration(pluginBindingInfo.getPluginConfiguration());
            pluginBindingUpdateResult = envoyPluginInfoService.updatePluginConfiguration(oldPluginBindingInfo.getId(), oldPluginBindingInfo.getPluginConfiguration(), 0);
        } else {
            BindingPluginInfo bindingPluginInfo =
                    BindingPluginInfo.createBindingPluginFromEnvoyPluginBindingInfo(pluginBindingInfo);
            pluginBindingUpdateResult = envoyPluginInfoService.bindingPlugin(bindingPluginInfo, pluginBindingInfo.getProjectId(), pluginBindingInfo.getTemplateId());
        }
        if (!pluginBindingUpdateResult) {
            return CommonErrorCode.BindingWsPluginFailed;
        }

        // 更新serviceWsProxyInfo
        EnvoyRouteWsParamInfo oldWsProxyInfo = getRouteProxyWsParam(gwId, serviceId, routeId);
        if (Objects.nonNull(oldWsProxyInfo)) {
            wsParamInfo.setId(oldWsProxyInfo.getId());
            envoyRouteWsParamDao.update(wsParamInfo);
        } else {
            envoyRouteWsParamDao.add(wsParamInfo);
        }

        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode deleteRouteProxyWsParam(long gwId, long serviceId, long routeId) {
        EnvoyPluginBindingInfo oldPluginBindingInfo = getWsPluginBindingInfo(gwId, serviceId, routeId);
        if (Objects.isNull(oldPluginBindingInfo)) {
            return CommonErrorCode.RouteWsPluginNonExist;
        }
        boolean unbindingResult = envoyPluginInfoService.unbindingPlugin(oldPluginBindingInfo.getId());
        if (!unbindingResult) {
            return CommonErrorCode.UnBindingWsPluginFailed;
        }
        EnvoyRouteWsParamInfo oldWsProxyInfo = getRouteProxyWsParam(gwId, serviceId, routeId);
        envoyRouteWsParamDao.delete(oldWsProxyInfo);
        return CommonErrorCode.Success;
    }

    @Override
    public Map<String, Object> createWsRequestTemplate(long gwId, long serviceId, String wsPortType, String wsOperation, String wsBinding) {
        Map<String, Object> out = new HashMap<>(2);
        // 预设返回结果
        out.put(errorCode, CommonErrorCode.Success);
        out.put(WS_REQUEST_TEMPLATE, StringUtils.EMPTY);

        EnvoyServiceWsdlInfo wsdlInfo = getServiceWsdlInfo(gwId, serviceId);
        // 校验webservice信息里是否包含指定binding参数
        Optional<EnvoyServiceWsdlBindingItem> bindingOptional = wsdlInfo.getWsdlBindingList().stream().filter(item ->
                Objects.equals(wsPortType, item.getPortType()) &&
                        Objects.equals(wsOperation, item.getOperation()) &&
                        Objects.equals(wsBinding, item.getBinding())).findFirst();
        if (!bindingOptional.isPresent()) {
            out.put(errorCode, CommonErrorCode.IllegalBindingParam);
            return out;
        }
        EnvoyServiceWsdlBindingItem binding = bindingOptional.get();
        // 创建请求模板
        Definitions definitions = wsdlParser.parse(new ByteArrayInputStream(wsdlInfo.getWsdlFileContent().getBytes()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
//            SoapRequestCreator creator = new SoapRequestCreator(definitions, new SoapTemplateCreator(), new MarkupBuilder(new PrintWriter(bos)));
//            creator.createRequest(binding.getPortType(), binding.getOperation(), binding.getBinding());
            String templateResult = bos.toString();
            out.put(WS_REQUEST_TEMPLATE, templateResult);
        } catch (Exception e) {
            e.printStackTrace();
            out.put(errorCode, CommonErrorCode.CreateWsTemplateFailed);
        }
        return out;
    }

    @Override
    public Map<String, Object> renderWsRequestTemplate(String template, Map<String, Object> context) {
        Map<String, Object> out = new HashMap<>(2);
        // 预设返回结果
        out.put(errorCode, CommonErrorCode.Success);
        out.put(WS_TEMPLATE_RENDER_RESULT, StringUtils.EMPTY);
        try {
            String renderedTemplate = jinjava.render(template, context);
            out.put(WS_TEMPLATE_RENDER_RESULT, renderedTemplate);
        } catch (Exception e) {
            e.printStackTrace();
            out.put(errorCode, CommonErrorCode.RenderWsTemplateFailed);
        }
        return out;
    }

    /**
     * 验证是否有非soap的webservice binding(不支持soap以外的webservice)
     *
     * @param definitions wsdl definitions
     * @return 是否有非soap的webservice binding，如果为null返回false
     */
    private boolean hasNonSoapBinding(Definitions definitions) {
        if (Objects.isNull(definitions)) return false;
        return definitions.getBindings().stream().anyMatch(binding ->
                !Objects.equals(binding.getBinding().getProtocol(), "SOAP11") && !Objects.equals(binding.getBinding().getProtocol(), "SOAP12"));
    }

    private EnvoyServiceWsdlInfo createEnvoyServiceWsInfo(long gwId, long serviceId, String fileName, String fileContext, Definitions definitions) {
        EnvoyServiceWsdlInfo out = new EnvoyServiceWsdlInfo();
        long createAndModifyDate = System.currentTimeMillis();
        out.setCreateDate(createAndModifyDate);
        out.setModifyDate(createAndModifyDate);
        out.setGwId(gwId);
        out.setServiceId(serviceId);
        out.setWsdlFileName(fileName);
        out.setWsdlFileContent(fileContext);
        List<EnvoyServiceWsdlBindingItem> wsdlBindingList = new ArrayList<>();
        for (Service service : definitions.getServices()) {
            for (Port port : service.getPorts()) {
                Binding binding = port.getBinding();
                for (BindingOperation operation : binding.getOperations()) {
                    // 排除非SOAP协议类型binding
                    if (!Objects.equals(binding.getBinding().getProtocol(), "SOAP11") && !Objects.equals(binding.getBinding().getProtocol(), "SOAP12")) {
                        continue;
                    }
                    EnvoyServiceWsdlBindingItem bindingItem = new EnvoyServiceWsdlBindingItem();
                    Optional<Object> inputMessage = Optional.ofNullable(operation.getInput().invokeMethod("getMessage", new Object[0]));
                    Optional<Object> outputMessage = Optional.ofNullable(operation.getOutput().invokeMethod("getMessage", new Object[0]));
                    String input = inputMessage.map(item -> ((Message) item).getName()).orElse("");
                    String output = outputMessage.map(item -> ((Message) item).getName()).orElse("");

                    bindingItem.setService(service.getName());
                    bindingItem.setPort(port.getName());
                    bindingItem.setPortType(binding.getPortType().getName());
                    bindingItem.setOperation(operation.getName());
                    bindingItem.setBinding(binding.getName());
                    bindingItem.setInput(input);
                    bindingItem.setOutput(output);
                    bindingItem.setAddress(port.getAddress().getLocation());
                    bindingItem.setRequestAllElements(new ArrayList<>());
                    bindingItem.setResponseAllElements(new ArrayList<>());

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    SoapRequestCreator requestCreator = new SoapRequestCreator(definitions, new SoapTemplateCreator(), new MarkupBuilder(new PrintWriter(bos)));
//                    SoapResponseCreator responseCreator = new SoapResponseCreator(definitions, new SoapTemplateCreator(), new MarkupBuilder(new PrintWriter(bos)));
//                    SoapCreatorContext requestResultContext = (SoapCreatorContext) requestCreator.createRequest(binding.getPortType().getName(), operation.getName(), binding.getName());
//                    SoapCreatorContext responseResultContext = (SoapCreatorContext) responseCreator.createRequest(binding.getPortType().getName(), operation.getName(), binding.getName());
//                    bindingItem.setRequestAllElements(getElementList(requestResultContext.getAnalyzerContext().getAllElements()));
//                    bindingItem.setResponseAllElements(getElementList(responseResultContext.getAnalyzerContext().getAllElements()));


                    wsdlBindingList.add(bindingItem);

                }
            }
        }
        out.setWsdlBindingList(wsdlBindingList);
        return out;
    }

    private List<ElementInfo> getElementList(List<Element> elements) {
        if (Objects.isNull(elements)) return new ArrayList<>();
        return elements.stream().map(item -> {
            ElementInfo elementInfo = new ElementInfo();
            elementInfo.setName(item.getName());
            elementInfo.setNamespace(item.getNamespaceUri());
            elementInfo.setQName(String.format("%s:%s", item.getQname().getNamespaceURI(), item.getQname().getLocalPart()));
            elementInfo.setPrefix(item.getPrefix());
            elementInfo.setMinOccurs(item.getMinOccurs());
            elementInfo.setMaxOccurs(item.getMaxOccurs());
            elementInfo.setNillable(item.getNillable());
            elementInfo.setDefaultValue(item.getDefaultValue());
            elementInfo.setFixedValue(item.getFixedValue());
            elementInfo.setArrayType(item.getArrayType());
            elementInfo.setQName(item.getQname().getLocalPart());

            if (!StringUtils.isEmpty(item.getQname().getNamespaceURI())) {
                elementInfo.setQName(String.format("{%s}:%s", item.getQname().getNamespaceURI(), item.getQname().getLocalPart()));
            }
            if (Objects.equals(item.getMinOccurs(), item.getMaxOccurs()) && !Objects.equals(item.getMaxOccurs(), "1")) {
                elementInfo.setMayArrayType(true);
            } else if (Objects.equals(item.getMinOccurs(), "0") && Objects.equals(item.getMaxOccurs(), "1")) {
                elementInfo.setMayOptional(true);
            } else if (!Objects.equals(item.getMinOccurs(), item.getMaxOccurs())) {
                elementInfo.setMayArrayType(true);
            }
            if (item.isNillable()) {
                elementInfo.setMayNullable(true);
            }
            return elementInfo;
        }).collect(Collectors.toList());
    }

    private EnvoyPluginBindingInfo createEnvoyPluginBindingInfo(long gwId, long serviceId, long routeId, String pluginConfiguration) {
        EnvoyPluginInfo pluginInfo = envoyPluginInfoService.getPluginInfoFromApiPlane(gwId, WS_PLUGIN_TYPE);

        EnvoyPluginBindingInfo out = new EnvoyPluginBindingInfo();
        long createAndModifyDate = System.currentTimeMillis();
        out.setGwId(gwId);
        out.setBindingObjectType(EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
        out.setBindingObjectId(String.valueOf(routeId));
        out.setCreateTime(createAndModifyDate);
        out.setUpdateTime(createAndModifyDate);
        out.setPluginConfiguration(pluginConfiguration);
        out.setBindingStatus(EnvoyPluginBindingInfo.BINDING_STATUS_ENABLE);
        out.setProjectId(ProjectTraceHolder.getProId());
        out.setPluginPriority(pluginInfo.getPluginPriority());
        out.setPluginType(WS_PLUGIN_TYPE);
        return out;
    }

    /**
     * 获取路由上绑定的webservice插件
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @param routeId   路由id
     * @return webservice插件信息
     */
    private EnvoyPluginBindingInfo getWsPluginBindingInfo(long gwId, long serviceId, long routeId) {
        List<EnvoyPluginBindingInfo> pluginBindingInfoList = envoyPluginInfoService.getBindingPluginList(gwId, ProjectTraceHolder.getProId(), String.valueOf(routeId), ImmutableList.of(EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE), WS_PLUGIN_TYPE, NumberUtils.LONG_ZERO, NumberUtils.LONG_ONE, "id", "desc");
        if (Objects.nonNull(pluginBindingInfoList) && pluginBindingInfoList.size() > 0) {
            return pluginBindingInfoList.get(0);
        }
        return null;
    }

    /**
     * 生成soap插件configuration
     *
     * @param wsParamInfo webservice请求参数
     * @return plugin configuration
     */
    private String generatePluginConfiguration(EnvoyRouteWsParamInfo wsParamInfo, EnvoyServiceWsdlInfo wsdlInfo, EnvoyServiceWsdlBindingItem binding) {
        Map<String, Object> protoRouteConfig = new HashMap<>();
        Map<String, Object> operation = new HashMap<>();
        operation.put("name", wsParamInfo.getWsOperation());
        operation.put("request_xml_template", wsParamInfo.getRequestTemplate());
        operation.put("response_xml_selector", "/*[local-name()='Envelope']/*[local-name()='Body']/*[1]");
        protoRouteConfig.put("operation", operation);
        protoRouteConfig.put("array_elements", wsParamInfo.getResponseArrayTypeList());
        protoRouteConfig.put("soap_service_address", wsParamInfo.getWsAddress());
        protoRouteConfig.put("parse_query", Boolean.TRUE);
        protoRouteConfig.put("parse_namespace", Boolean.TRUE);
        protoRouteConfig.put("remove_response_prefix", Boolean.TRUE);
        protoRouteConfig.put("kind", WS_PLUGIN_TYPE);
        return JSON.toJSONString(protoRouteConfig);
    }
}
