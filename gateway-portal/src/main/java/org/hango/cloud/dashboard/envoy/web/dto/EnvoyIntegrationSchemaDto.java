package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 集成schema的dto
 */
public class EnvoyIntegrationSchemaDto {

    /**
     * 表的主键
     */
    @JSONField(name = "Id")
    private long id;

    /**
     * schema大类类型
     */
    @JSONField(name = "Category")
    private String category;

    /**
     * schema小类类型
     */
    @JSONField(name = "Kind")
    private String kind;

    /**
     * schema名称
     */
    @JSONField(name = "Name")
    private String name;

    /**
     * schema描述信息
     */
    @JSONField(name = "Description")
    private String description;

    /**
     * schema
     */
    @JSONField(name = "Schema")
    private String schema;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
