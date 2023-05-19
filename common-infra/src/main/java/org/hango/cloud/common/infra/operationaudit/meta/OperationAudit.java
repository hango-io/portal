package org.hango.cloud.common.infra.operationaudit.meta;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import java.util.List;

/**
 * @author zhangbaojun
 * @version $Id: AuditDto.java, v 1.0 2018年08月25日 14:39
 */
@Getter
@Setter
public class OperationAudit extends CommonExtensionDto {

    /**
     * 事件 UUID
     */
    @JSONField(name = "EventId")
    private String eventId;
    /**
     * 事件发生的时间戳
     */
    @JSONField(name = "EventTime")
    private Long eventTime;
    /**
     * 事件格式版本
     */
    @JSONField(name = "EventVersion")
    private String eventVersion;
    /**
     * 处理事件的服务
     */
    @JSONField(name = "EventSource")
    private String eventSource;
    /**
     * 请求的操作
     */
    @JSONField(name = "EventName")
    private String eventName;

    /**
     * 请求描述
     */
    @JSONField(name = "Description")
    private String description;
    /**
     * 源 IP
     */
    @JSONField(name = "SourceIpAddress")
    private String sourceIpAddress;
    /**
     * 请求发起方
     */
    @JSONField(name = "UserAgent")
    private String userAgent;


    /**
     * 请求ID
     */
    @JSONField(name = "RequestId")
    private String requestId;

    /**
     * 请求方式
     */
    @JSONField(name = "RequestMethod")
    private String requestMethod;
    /**
     * 请求参数
     */
    @JSONField(name = "RequestParameters")
    private String requestParameters;

    /**
     * 响应状态
     */
    @JSONField(name = "ResponseStatus")
    private Integer responseStatus;

    /**
     * 响应
     */
    @JSONField(name = "ResponseElements")
    private String responseElements;
    /**
     * 事件类型
     */
    @JSONField(name = "EventType")
    private String eventType;

    /**
     * 事件中访问的资源列表
     */
    @JSONField(name = "ResourceReports")
    private List<ResourceDataDto> resources;
    /**
     * 请求错误详述
     */
    @JSONField(name = "ApiErrorCode")
    private String errorCode;
    /**
     * 请求错误详述
     */
    @JSONField(name = "ErrorMessage")
    private String errorMessage;
    /**
     * 请求连接
     */
    @JSONField(name = "Url")
    private String url;
    /**
     * 用户身份
     */
    @JSONField(name = "UserIdentity")
    private UserIdentityEntity userIdentity;

    /**
     * 项目ID
     */
    @JSONField(name = "ProjectId")
    private String projectId;

    /**
     * 项目名称
     */
    @JSONField(name = "ProjectName")
    private String projectName;
    /**
     * 租户ID
     */
    @JSONField(name = "TenantId")
    private String tenantId;

    /**
     * 租户名称
     */
    @JSONField(name = "TenantName")
    private String tenantName;
    /**
     * OpenAPI操作
     */
    @JSONField(name = "ApiAction")
    private String apiAction;
    /**
     * OpenAPI 版本号
     */
    @JSONField(name = "ApiVersion")
    private String apiVersion;

    /**
     * 扩展信息
     */
    @JSONField(name = "Extension")
    private String extension;

    /**
     * 环境标识
     */
    private String envId;
}
