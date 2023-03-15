package org.hango.cloud.envoy.infra.serviceproxy.hooker;

import jodd.typeconverter.Converter;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.hooker.AbstractServiceProxyHooker;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
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
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;

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
        if (!envoyServiceProxyService.updateToGateway(serviceProxyDto)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected void preDeleteHook(ServiceProxyDto serviceProxyDto) {
        if (!envoyServiceProxyService.offlineToGateway(serviceProxyDto)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected ErrorCode checkDeleteParam(ErrorCode returnCode) {
        ServiceProxyDto serviceProxyDto = MethodAroundHolder.getNextParam(ServiceProxyDto.class);
        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = envoyGrpcProtobufService.getServiceProtobufProxy(serviceProxyDto.getServiceId(), serviceProxyDto.getVirtualGwId());
        if (envoyServiceProtobufProxy != null) {
            return EnvoyErrorCode.COULD_NOT_OFFLINE_SERVICE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    protected Object findSingleEnhancement(Object o) {
        if (!(o instanceof ServiceProxyDto)) {
            return o;
        }
        ((ServiceProxyDto) o).setPort(envoyServiceProxyService.getBackendServicePorts(((ServiceProxyDto) o)));
        return o;
    }

    @Override
    protected List<? extends BackendServiceWithPortDto> postGetBackendServicesHook(List l) {
        Converter converter = new Converter();
        Long virtualGwId = converter.toLong(MethodAroundHolder.getNextParam());
        String name = converter.toString(MethodAroundHolder.getNextParam());
        String registryCenterType = converter.toString(MethodAroundHolder.getNextParam());
        return envoyServiceProxyService.getServiceListFromApiPlane(virtualGwId, name, registryCenterType);
    }

}
