package org.hango.cloud.common.infra.base.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/11
 */
@Getter
@Setter
public class TimeQueryDto {

    /**
     * 起止时间
     */
    @JSONField(name = "StartTime")
    private long startTime;

    /**
     * 截止时间
     */
    @JSONField(name = "EndTime")
    private long endTime;
}
