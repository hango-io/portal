package org.hango.cloud.common.infra.base.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/11
 */
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
