package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/27
 */
public class VirtualGatewayBindDto extends CommonExtensionDto {

    /**
     * 虚拟网关ID
     */
    @Min(1)
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 绑定对象列表
     */
    @JSONField(name = "ProjectIdList")
    private List<Long> projectIdList;


    public VirtualGatewayBindDto(long virtualGwId, List<Long> projectIdList) {
        this.virtualGwId = virtualGwId;
        this.projectIdList = projectIdList;
    }

    public VirtualGatewayBindDto() {
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public List<Long> getProjectIdList() {
        return projectIdList;
    }

    public void setProjectIdList(List<Long> projectIdList) {
        this.projectIdList = projectIdList;
    }

}
