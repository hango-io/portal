package org.hango.cloud.envoy.infra.serviceproxy.hooker;

import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.hooker.AbstractServiceProxyHooker;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.envoy.infra.healthcheck.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.envoy.infra.healthcheck.dto.HealthStatusEnum;
import org.hango.cloud.envoy.infra.healthcheck.service.IEnvoyHealthCheckService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
@Component
public class EnvoyServiceProxyHooker extends AbstractServiceProxyHooker<ServiceProxyInfo, ServiceProxyDto> {

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;


    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected void preCreateHook(ServiceProxyDto serviceProxyDto) {
        if (!envoyServiceProxyService.publishToGateway(serviceProxyDto)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected void preUpdateHook(ServiceProxyDto serviceProxyDto) {
        if (!envoyServiceProxyService.refreshRouteHost(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getId(), serviceProxyDto.getHosts())){
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (!envoyServiceProxyService.updateToGateway(serviceProxyDto)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected void preDeleteHook(ServiceProxyDto serviceProxyDto) {
        if (!envoyServiceProxyService.offlineToGateway(serviceProxyDto)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        envoyServiceProxyService.deleteService(serviceProxyDto);
    }

    @Override
    protected ErrorCode checkDeleteParam(ErrorCode returnCode) {
        if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
            return returnCode;
        }
        ServiceProxyDto nextParam = MethodAroundHolder.getNextParam(ServiceProxyDto.class);
        return envoyServiceProxyService.checkDeleteParam(nextParam);
    }


    @Override
    protected List<? extends BackendServiceWithPortDto> postGetBackendServicesHook(List l) {
        Long virtualGwId = (Long)MethodAroundHolder.getNextParam();
        String name = (String)MethodAroundHolder.getNextParam();
        String registryCenterType = (String)MethodAroundHolder.getNextParam();
        return envoyServiceProxyService.getServiceListFromApiPlane(virtualGwId, name, registryCenterType);
    }

    @Override
    protected void preGetHealthStatusHook(ServiceProxyDto serviceProxyDto) {
        List<EnvoyServiceInstanceDto> serviceInstanceDtos = envoyHealthCheckService.getServiceInstanceList(serviceProxyDto);
        for (EnvoyServiceInstanceDto serviceInstanceDto : serviceInstanceDtos) {
            if (HealthStatusEnum.UNHEALTHY.getValue().equals(serviceInstanceDto.getStatus())){
                serviceProxyDto.setHealthyStatus(HealthStatusEnum.UNHEALTHY.getValue());
                return;
            }
        }
        serviceProxyDto.setHealthyStatus(HealthStatusEnum.HEALTHY.getValue());
    }
}
