package org.hango.cloud.envoy.infra.grpc.remote;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.grpc.dto.GrpcEnvoyFilterDto;
import org.hango.cloud.envoy.infra.grpc.service.impl.EnvoyGrpcProtobufServiceImpl;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    IVirtualGatewayInfoService virtualGatewayInfoService;

    public boolean publishGrpcEnvoyFilterToAPIPlane(Long vgId, String protoDescriptorBin, List<String> services) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "PublishGrpcEnvoyFilter");
        params.put("Version", "2019-07-25");
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgId);
        GrpcEnvoyFilterDto grpcEnvoyFilterDto = GrpcEnvoyFilterDto.builder()
                .name(getGrpcName(virtualGatewayDto))
                .gwCluster(virtualGatewayDto.getGwClusterName())
                .portNumber(virtualGatewayDto.getPort())
                .services(services)
                .protoDescriptorBin(protoDescriptorBin).build();
        String body = JSON.toJSONString(grpcEnvoyFilterDto);

        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/api", body, params, null, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane发布grpc EnvoyFilter接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    public boolean deleteGrpcEnvoyFilterToAPIPlane(Long vgId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DeleteGrpcEnvoyFilter");
        params.put("Version", "2019-07-25");
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgId);

        GrpcEnvoyFilterDto grpcEnvoyFilterDto = GrpcEnvoyFilterDto.builder()
                .name(getGrpcName(virtualGatewayDto))
                .gwCluster(virtualGatewayDto.getGwClusterName())
                .portNumber(virtualGatewayDto.getPort())
                .build();
        String body = JSON.toJSONString(grpcEnvoyFilterDto);

        HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/api", body, params, null, MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane下线grpc EnvoyFilter接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    private String getGrpcName(VirtualGatewayDto virtualGatewayDto){
        return "grpc-" + virtualGatewayDto.getGwClusterName() + "-" + virtualGatewayDto.getCode();
    }
}
