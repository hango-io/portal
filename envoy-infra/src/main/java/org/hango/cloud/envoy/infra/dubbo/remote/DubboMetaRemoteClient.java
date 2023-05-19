package org.hango.cloud.envoy.infra.dubbo.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/1/4
 */
@Component
public class DubboMetaRemoteClient {

    public static final Logger logger = LoggerFactory.getLogger(DubboMetaRemoteClient.class);

    /**
     * 通过api-plane 下获取Dubbo Meta元数据信息
     *
     * @param virtualGatewayDto 虚拟网关
     * @param igv               接口+版本+分组 {interface:group:version}
     * @param applicationName   应用名称
     * @param method            dubbo方法
     * @return
     */
    public List<DubboMetaDto> getDubboMetaList(VirtualGatewayDto virtualGatewayDto, String igv, String applicationName, String method) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetDubboMeta");
        params.put("Version", "2019-07-25");

        params.put("Igv", igv);
        params.put("ApplicationName", applicationName);
        params.put("Method", method);
        HttpClientResponse response = HttpClientUtil.getRequest(virtualGatewayDto.getConfAddr() + "/api", params, EnvoyConst.MODULE_API_PLANE);
        if (null == response) {
            return null;
        }
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane查询dubbo Meta元信息，返回http status code非2xx, httpStatuCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
        JSONArray services = jsonResult.getJSONArray("Result");
        List<DubboMetaDto> dubboMetaList = JSONObject.parseArray(services.toJSONString(), DubboMetaDto.class);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return dubboMetaList;
        }
        dubboMetaList.stream().forEach(meta -> meta.setVirtualGwId(virtualGatewayDto.getId()));
        return dubboMetaList;
    }
}
