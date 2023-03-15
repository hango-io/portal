package org.hango.cloud.envoy.infra.grpc.remote;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.envoy.infra.grpc.dto.GrpcEnvoyFilterDto;
import org.hango.cloud.envoy.infra.grpc.service.impl.EnvoyGrpcProtobufServiceImpl;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.MODULE_API_PLANE;

/**
 * @author Xin Li
 * @date 2023/1/11 10:08
 */
@Component
public class GrpcProtobufRemoteClient {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcProtobufServiceImpl.class);

    public boolean publishGrpcEnvoyFilterToAPIPlane(int listenerPort, String apiPlaneAddr, String gwClusterName, String protoDescriptorBin, List<String> services) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "PublishGrpcEnvoyFilter");
        params.put("Version", "2019-07-25");

        GrpcEnvoyFilterDto grpcEnvoyFilterDto = new GrpcEnvoyFilterDto(gwClusterName, listenerPort, protoDescriptorBin, services);
        String body = JSON.toJSONString(grpcEnvoyFilterDto);

        HttpClientResponse response = HttpClientUtil.postRequest(apiPlaneAddr + "/api", body, params, null, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane发布grpc EnvoyFilter接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    public boolean deleteGrpcEnvoyFilterToAPIPlane(int listenerPort, String apiPlaneAddr, String gwClusterName) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteGrpcEnvoyFilter");
        params.put("Version", "2019-07-25");

        GrpcEnvoyFilterDto grpcEnvoyFilterDto = new GrpcEnvoyFilterDto(gwClusterName, listenerPort, null, null);
        String body = JSON.toJSONString(grpcEnvoyFilterDto);

        HttpClientResponse response = HttpClientUtil.postRequest(apiPlaneAddr + "/api", body, params, null, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane下线grpc EnvoyFilter接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }
}
