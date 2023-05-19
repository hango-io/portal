package org.hango.cloud.common.infra.operationaudit.recorder;

import org.hango.cloud.common.infra.operationaudit.meta.OperationAudit;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/2
 */
public abstract class AbstractRecorder {

    private AbstractRecorder nextRecorder;

    public void setNextRecorder(AbstractRecorder recorder) {
        this.nextRecorder = recorder;
    }

    public void doRecord(OperationAudit audit, HttpServletRequest request) {
        if (nextRecorder != null) {
            nextRecorder.doRecord(audit, request);
        }
        record(audit, request);
    }

    /**
     * 记录操作审计
     *
     * @param audit
     * @param request
     */
    protected abstract void record(OperationAudit audit, HttpServletRequest request);
}
