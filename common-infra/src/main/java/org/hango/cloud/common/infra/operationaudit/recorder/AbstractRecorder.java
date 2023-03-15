package org.hango.cloud.common.infra.operationaudit.recorder;

import org.hango.cloud.common.infra.operationaudit.meta.OperationAudit;

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

    public void doRecord(OperationAudit audit) {
        if (nextRecorder != null) {
            nextRecorder.doRecord(audit);
        }
        record(audit);
    }

    /**
     * 记录操作审计
     *
     * @param audit
     */
    protected abstract void record(OperationAudit audit);
}
