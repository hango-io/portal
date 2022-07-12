package org.hango.cloud.dashboard.envoy.meta;

/**
 * 集成schema表info
 */
public class EnvoyIntegrationSchemaInfo {

    /**
     * 表的主键
     */
    private long id;

    /**
     * schema大类类型
     */
    private String category;

    /**
     * schema小类类型
     */
    private String kind;

    /**
     * schema名称
     */
    private String name;

    /**
     * schema描述信息
     */
    private String description;

    /**
     * schema
     */
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
