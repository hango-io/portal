package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 对应VirtualService中的HTTPRetry
 * https://istio.io/docs/reference/config/networking/virtual-service/#HTTPRetry
 *
 * @author hanjiahao
 */
@Getter
@Setter
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
    @Max(10)
    private int attempts = 2;
    /**
     * 重试超时时间，默认为ms
     */
    @JSONField(name = "PerTryTimeout")
    @Min(1)
    @Max(1024000)
    private long perTryTimeout = 60000;

    /**
     * 重试条件，，分割
     * 5xx,gateway-error,refused-stream,connect-failure
     */
    @JSONField(name = "RetryOn")
    private String retryOn;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
