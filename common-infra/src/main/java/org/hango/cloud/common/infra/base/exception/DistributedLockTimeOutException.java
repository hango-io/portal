package org.hango.cloud.common.infra.base.exception;

/**
 * @author yutao04
 * @date 2021/12/16
 */
public class DistributedLockTimeOutException extends RuntimeException {
    public DistributedLockTimeOutException(String msg) {
        super(msg);
    }
}