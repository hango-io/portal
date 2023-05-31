package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/26
 */
public class QueryVirtualGatewayDto extends PageQuery implements Serializable {

    private static final long serialVersionUID = 6163983923024228367L;

    /**
     * 查询的项目范围
     */
    @JSONField(name = "ProjectIdList")
    private List<Long> projectIdList;

    /**
     * 服务id
     */
    @JSONField(name = "ServiceId")
    private Long serviceId;

    /**
     * 模糊匹配条件
     */
    @JSONField(name = "Pattern")
    private String pattern;

    /**
     * 虚拟网关类型
     */
    @JSONField(name = "Type")
    private String type;


    /**
     * 网关id
     */
    @JSONField(name = "GwId")
    private Long gwId;

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 是否被管理（Kubernetes虚拟网关不进行管理）
     */
    @JSONField(name = "Managed")
    private Boolean managed;

    @JSONField(serialize = false)
    private Long domainId;

    public List<Long> getProjectIdList() {
        return projectIdList;
    }

    public void setProjectIdList(List<Long> projectIdList) {
        this.projectIdList = projectIdList;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Boolean getManaged() {
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getGwId() {
        return gwId;
    }

    public void setGwId(Long gwId) {
        this.gwId = gwId;
    }
}
