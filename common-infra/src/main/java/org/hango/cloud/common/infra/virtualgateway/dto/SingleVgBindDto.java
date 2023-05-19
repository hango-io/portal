package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 单条网关关联项目显示Dto
 * @date 2022/10/27
 */
public class SingleVgBindDto extends CommonExtensionDto {

    /**
     * 项目ID
     */
    @JSONField(name = "ProjectId")
    private long projectId;

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 域名列表，用于前端显示
     */
    @JSONField(name = "DomainList")
    private List<String> domainList;


    public SingleVgBindDto() {
    }

    public SingleVgBindDto(long projectId, long virtualGwId, List<String> domainList) {
        this.projectId = projectId;
        this.virtualGwId = virtualGwId;
        this.domainList = domainList;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public List<String> getDomainList() {
        return domainList;
    }

    public void setDomainList(List<String> domainList) {
        this.domainList = domainList;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }
}
