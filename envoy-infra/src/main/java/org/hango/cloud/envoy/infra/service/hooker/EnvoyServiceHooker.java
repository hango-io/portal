package org.hango.cloud.envoy.infra.service.hooker;

import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.hooker.AbstractServiceHooker;
import org.hango.cloud.common.infra.service.meta.ServiceInfo;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Xin Li
 * @date 2023/2/17 10:59
 */
@Component
public class EnvoyServiceHooker extends AbstractServiceHooker<ServiceInfo, ServiceDto> {

    @Autowired
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected void preDeleteHook(ServiceDto serviceDto) {
        envoyGrpcProtobufService.deleteServiceProtobuf(serviceDto.getId());
    }
}
