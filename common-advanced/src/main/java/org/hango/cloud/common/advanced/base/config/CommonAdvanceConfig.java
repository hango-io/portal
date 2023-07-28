package org.hango.cloud.common.advanced.base.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/5/5
 */
@Component
@Getter
public class CommonAdvanceConfig {

    /**
     * 操作审计服务地址
     */
    @Value("${nsf.audit.url}")
    private String auditUrl;


    /**
     * 轻舟认证平台地址
     */
    @Value("${skiffAuthorityAddr}")
    private String skiffAuthorityAddr;

    /**
     * 超级管理员账号
     */
    @Value("${permissionScopeAccount:admin}")
    private String permissionScopeAccount = "admin";



    /**
     * 配置变更审计开关，默认为false
     */
    @Value("${configUpdateAudit:false}")
    private Boolean configUpdateAudit = false;

    /**
     * ElasticSearch 索引生成格式
     */
    @Value("${elasticsearch.index.pattern:yyyy.MM.dd.HH}")
    private String indexPattern;

    /**
     * Elasticsearch scroll 保存时长
     */
    @Value("${elasticsearch.search.scroll:5m}")
    private String searchScroll;

    /**
     * ElasticSearch 索引检索格式
     */
    @Value("${elasticsearch.index.format:envoy_gateway_audit_%s-%s}")
    private String indexFormat;


    @Value("${closeCertification:false}")
    private Boolean closeCertification = false;

    /**
     * 服务目录地址，如果为空，则代表不依赖服务目录
     */
    @Value("${metaServiceAddr:#{null}}")
    private String metaServiceAddr;
}
