package org.hango.cloud.common.infra.operationaudit.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author zhufengwei
 * @Date 2023/8/21
 */
@Getter
@Setter
public class AuditViewInfo {
    private String resourceType;
    private String description;
    private String eventName;
}
