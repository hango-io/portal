package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Min;

/**
 * 对应VirtualService中的HTTPRetry
 * https://istio.io/docs/reference/config/networking/virtual-service/#HTTPRetry
 *
 * @author hanjiahao
 */
public class HttpRetryDto {

    /**
     * 是否需要重试
     */
    @JSONField(name = "IsRetry")
    private boolean retry;

    /**
     * 重试次数
     */
    @JSONField(name = "Attempts")
    @Min(1)
    private int attempts;
    /**
     * 重试超时时间，默认为ms
     */
    @JSONField(name = "PerTryTimeout")
    @Min(1)
    private long perTryTimeout;

    /**
     * 重试条件，，分割
     * 5xx,gateway-error,refused-stream,connect-failure
     */
    @JSONField(name = "RetryOn")
    private String retryOn;

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public long getPerTryTimeout() {
        return perTryTimeout;
    }

    public void setPerTryTimeout(long perTryTimeout) {
        this.perTryTimeout = perTryTimeout;
    }

    public String getRetryOn() {
        return retryOn;
    }

    public void setRetryOn(String retryOn) {
        this.retryOn = retryOn;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
