package org.hango.cloud.common.infra.routeproxy.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author zhufengwei
 * @Date 2023/1/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpRetryPO {
    /**
     * 是否需要重试
     */
    private Boolean isRetry;

    /**
     * 重试次数
     */
    private int attempts;

    /**
     * 重试超时时间，默认为ms
     */
    private long perTryTimeout;

    /**
     * 重试条件，，分割
     * 5xx,gateway-error,refused-stream,connect-failure
     */
    private String retryOn;
}
