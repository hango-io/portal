package org.hango.cloud.gdashboard.api.meta;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * API 文档状态，包含：开发中、联调中、提测、已上线
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/6/27 11:30.
 */
public class ApiDocumentStatus implements Serializable {

    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "Status")
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
