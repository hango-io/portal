package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @ClassName PluginUpdateDto
 * @Description 修改已经上传的自定义插件DTO
 * @Author xianyanglin
 * @Date 2023/6/30 16:13
 */
@Getter
@Setter
public class PluginUpdateDto {
    /**
     * 数据库自增id
     */
    @NotNull
    @JSONField(name = "Id")
    private Long id;

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
    @Length(max = 20, message = "插件中文名称不能超过20个字符")
    private String pluginName;
    /**
     * 插件描述
     */
    @JSONField(name = "Description")
    @Length(max = 32, message = "插件描述不能超过32个字符")
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
     * 脚本类型
     */
    @JSONField(name = "SourceUrl")
    private String sourceUrl;

    /**
     * secretName
     */
    @JSONField(name = "SecretName")
    private String secretName;

    /**
     * 脚本内容 base
     */
    @JSONField(name = "SourceContent")
    private MultipartFile sourceContent;

    /**
     * 插件作用域:route(路由)，global(全局),gateway(网关)
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
}
