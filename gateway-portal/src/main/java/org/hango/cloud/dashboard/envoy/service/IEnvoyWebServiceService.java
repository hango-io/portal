package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyRouteWsParamInfo;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyServiceWsdlInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Envoy webservice转rest相关接口
 * webservice -> rest流程如下：
 * 1. 用户导入wsdl文件
 * 2. 解析wsdl文件获得相关binding信息
 * 3. 根据选择的binding生成webservice请求模板
 * 4. 用户测试webservice渲染结果
 * 5. 保存模板binding等配置信息，生成soap2rest插件(binding_from=system)，绑定插件（立即生效）
 *
 * @author wupenghuai 2021-02-04
 */
public interface IEnvoyWebServiceService {
    /**
     * 检查并上传wsdl文件
     *
     * @param gwId      网关id
     * @param serviceId 绑定的服务
     * @param wsdlFile  wsdl文件
     * @return wsdl检验结果
     */
    ErrorCode checkUploadWsdlFile(long gwId, long serviceId, MultipartFile wsdlFile);

    /**
     * 获取服务下绑定的wsdl解析后的信息
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return 服务下的wsdl信息
     */
    EnvoyServiceWsdlInfo getServiceWsdlInfo(long gwId, long serviceId);

    /**
     * 删除服务下的wsdl解析信息
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return 删除结果
     */
    ErrorCode deleteServiceWsdlInfo(long gwId, long serviceId);

    /**
     * 获取路由下的webservice绑定信息
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @param routeId   路由id
     * @return 结果状态
     */
    EnvoyRouteWsParamInfo getRouteProxyWsParam(long gwId, long serviceId, long routeId);

    /**
     * 绑定路由与webservice信息
     *
     * @param wsParamInfo webservice配置参数
     * @return 结果状态
     */
    ErrorCode updateRouteProxyWsParam(EnvoyRouteWsParamInfo wsParamInfo);

    /**
     * 删除路由下的webservice绑定信息
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @param routeId   路由id
     * @return 结果状态
     */
    ErrorCode deleteRouteProxyWsParam(long gwId, long serviceId, long routeId);

    /**
     * 生成webservice请求模板
     *
     * @param gwId        网关id
     * @param serviceId   服务id
     * @param wsPortType  webservice的portType
     * @param wsOperation webservice的operation
     * @param wsBinding   webservice的binding
     * @return webservice请求模板 + errorCode
     */
    Map<String, Object> createWsRequestTemplate(long gwId, long serviceId, String wsPortType, String wsOperation, String wsBinding);


    /**
     * 使用context渲染template模板,引擎为jinja
     *
     * @param template 模板
     * @param context  上下文
     * @return 渲染结果 + errorCode
     */
    Map<String, Object> renderWsRequestTemplate(String template, Map<String, Object> context);
}
