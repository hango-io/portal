package org.hango.cloud.common.advanced.serviceproxy.hooker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.common.advanced.authentication.holder.ActionInfoHolder;
import org.hango.cloud.common.advanced.authentication.holder.UserPermissionHolder;
import org.hango.cloud.common.advanced.configaudit.audit.meta.AuditMetaData;
import org.hango.cloud.common.advanced.configaudit.audit.service.IAuditConfigService;
import org.hango.cloud.common.advanced.serviceproxy.service.IMetaService;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.hooker.AbstractServiceProxyHooker;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
@Component
public class ServiceProxyAdvancedHooker extends AbstractServiceProxyHooker<ServiceProxyInfo, ServiceProxyDto> {

    @Autowired
    private IAuditConfigService auditConfigService;

    @Autowired
    private IMetaService metaService;

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    protected void preCreateHook(ServiceProxyDto serviceProxyDto) {
        ErrorCode errorCode = metaService.addMetaService(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            throw new ErrorCodeException(errorCode);
        }
    }

    @Override
    protected Object postCreateHook(Object returnData) {
        //配置审计
        auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(MethodAroundHolder.getParam()))));
        return super.postCreateHook(returnData);
    }

    @Override
    protected void preDeleteHook(ServiceProxyDto serviceProxyDto) {
        ErrorCode errorCode = metaService.deleteMetaService(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            throw new ErrorCodeException(errorCode);
        }
    }
}
