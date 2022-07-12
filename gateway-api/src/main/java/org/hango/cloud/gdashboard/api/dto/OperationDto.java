package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class OperationDto {
    @JSONField(name = "AccountId")
    private String accountId;

    @JSONField(name = "CreateDate")
    private long createDate;

    @JSONField(name = "Operation")
    private String operation;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
