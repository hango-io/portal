package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/26
 */
public class QueryVirtualGatewayDto implements Serializable {

    private static final long serialVersionUID = 6163983923024228367L;
    /**
     * 偏移量
     */

    @JSONField(name = "Offset")
    private long offset = 0;

    /**
     * 最大返回数量
     */
    @JSONField(name = "Limit")
    private long limit = 20;

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
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 是否被管理（Kubernetes虚拟网关不进行管理）
     */
    @JSONField(name = "Managed")
    private boolean managed = false;



    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

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

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }
}
