package com.netease.cloud.nsf.record;

public class ExceptionRecord {
    private String recordId;

    private String integrationId;

    private String stepId;

    private String failureEndpoint;

    private String messageHistoryStacktrace;

    private String exception;

    private String exceptionStacktrace;

    private String exceptionMessage;

    private String exceptionClass;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getFailureEndpoint() {
        return failureEndpoint;
    }

    public void setFailureEndpoint(String failureEndpoint) {
        this.failureEndpoint = failureEndpoint;
    }

    public String getMessageHistoryStacktrace() {
        return messageHistoryStacktrace;
    }

    public void setMessageHistoryStacktrace(String messageHistoryStacktrace) {
        this.messageHistoryStacktrace = messageHistoryStacktrace;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }
}
