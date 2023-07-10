package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @ClassName PluginUpdateDto
 * @Description 修改已经上传的自定义插件DTO
 * @Author xianyanglin
 * @Date 2023/6/30 16:13
 */
public class PluginUpdateDto {
    /**
     * 数据库自增id
     */
    @NotNull
    @JSONField(name = "Id")
    private Long id;
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
     * 脚本内容 base
     */
    @JSONField(name = "SourceContent")
    private MultipartFile sourceContent;

    /**
     * 插件作用域:route(路由)，global(全局),gateway(网关)
     */
    //todo yl 可以支持list
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public MultipartFile getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(MultipartFile sourceContent) {
        this.sourceContent = sourceContent;
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

    @Override
    public String toString() {
        return "PluginUpdateDto{" +
                "id=" + id +
                ", pluginName='" + pluginName + '\'' +
                ", description='" + description + '\'' +
                ", sourceContent=" + sourceContent +
                ", pluginScope=" + pluginScope +
                ", schemaContent='" + schemaContent + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
