package org.hango.cloud.common.infra.base.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/5
 */
@Getter
@Setter
public class TimeQuery {

    /**
     * 起止时间
     */
    private long startTime;

    /**
     * 截止时间
     */
    private long endTime;
}
