package org.hango.cloud.dashboard.apiserver.dto.auditdto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/28
 */
public class ServiceRankDto {

    @JSONField(name = "Name")
    private String name;

    @JSONField(name = "Count")
    private int count;

    public ServiceRankDto() {
    }

    public ServiceRankDto(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
