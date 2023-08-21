package org.hango.cloud.common.advanced.operationaudit;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.advanced.base.config.CommonAdvanceConfig;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.infra.base.meta.ActionPair;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.operationaudit.meta.OperationAudit;
import org.hango.cloud.common.infra.operationaudit.meta.UserIdentityEntity;
import org.hango.cloud.common.infra.operationaudit.recorder.AbstractRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/27
 */
@Component
public class PlatformAuditRecorder extends AbstractRecorder {

    public static final ActionPair AUDIT_REPORT = new ActionPair("EventReport", "2018-07-20");
    private static final Logger logger = LoggerFactory.getLogger(PlatformAuditRecorder.class);
    @Autowired
    private CommonAdvanceConfig advanceConfig;

    @Override
    protected void record(OperationAudit audit, HttpServletRequest request) {
        audit.setUserIdentity(UserIdentityEntity.builder().accountId(String.valueOf(request.getAttribute(AdvancedConst.USER_ACCOUNT_ID))).build());
        HttpClientResponse httpResponse = HttpClientUtil.postRequest(advanceConfig.getAuditUrl() + AdvancedConst.AUDIT_PREFIX, JSON.toJSONString(audit), HttpClientUtil.defaultQuery(AUDIT_REPORT), "audit");
        if (HttpStatus.SC_OK != httpResponse.getStatusCode()) {
            logger.warn("record operation audit to platform audit service failed ,errorMeg = {}", httpResponse.getResponseBody());
        }
    }
}
