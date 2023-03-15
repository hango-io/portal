package org.hango.cloud.common.infra.base.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/16
 */
public class PageTimeQueryDto extends TimeQueryDto{


    @JSONField(name = "Limit")
    private int limit = 20;

    @JSONField(name = "Offset")
    private int offset = 0;


    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
