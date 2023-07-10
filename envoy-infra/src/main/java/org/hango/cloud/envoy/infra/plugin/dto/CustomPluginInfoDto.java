package org.hango.cloud.envoy.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @ClassName PluginImportDTO
 * @Description 插入自定义插件DTO
 * @Author xianyanglin
 * @Date 2023/6/30 15:50
 */

public class CustomPluginInfoDto {
    /**
     * 插件名称
     * 以小写字母或数字开头和结尾，支持符号：-，2-63个字符
     */
    @JSONField(name = "PluginType")
    @Pattern(regexp = "^[a-z0-9]([a-z0-9\\-]{0,61}[a-z0-9])?$")
    private String pluginType;
    /**
     * 插件中文名称
     */
    @JSONField(name = "PluginName")
    private String pluginName;
    /**
     * 插件描述
     */
    @JSONField(name = "Description")
    private String description;
    /**
     * 插件语言
     */
    @JSONField(name = "Language")
    @Pattern(regexp = "lua|wasm")
    private String language;
    /**
     * 脚本类型
     */
    @JSONField(name = "SourceType")
    @Pattern(regexp = "file|oci")
    private String sourceType;
    /**
     * 脚本内容 base 64
     */
    @JSONField(name = "SourceContent")
    private MultipartFile sourceContent;
    /**
     * 脚本类型
     * 执行阶段 trafficPolicy（流量管理）、auth(认证鉴权)  security(安全)、dataFormat（数据转换）
     */
    @JSONField(name = "PluginCategory")
    @Pattern(regexp = "security|auth|dataFormat|trafficPolicy")
    private String pluginCategory;
    /**
     * 插件作用域:routeRule(路由)，global(全局) 多选，可两者多选
     */
    @JSONField(name = "PluginScope")
    @NotEmpty
    private String pluginScope;
    /**
     * schema表单
     */
    @JSONField(name = "SchemaContent")
    @NotEmpty
    private String schemaContent;

    /**
     * 插件联系人
     */
    @JSONField(name = "Author")
    private String author;

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public MultipartFile getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(MultipartFile sourceContent) {
        this.sourceContent = sourceContent;
    }

    public String getPluginCategory() {
        return pluginCategory;
    }

    public void setPluginCategory(String pluginCategory) {
        this.pluginCategory = pluginCategory;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getSchemaContent() {
        return schemaContent;
    }

    public void setSchemaContent(String schemaContent) {
        this.schemaContent = schemaContent;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
