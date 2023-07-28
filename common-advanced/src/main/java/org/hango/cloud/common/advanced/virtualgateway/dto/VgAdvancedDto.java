package org.hango.cloud.common.advanced.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/1
 */
public class VgAdvancedDto extends VirtualGatewayDto {

    /**
     * 项目名称
     */
    @JSONField(name = "ProjectNames")
    private List<String> projectNames;

    public VgAdvancedDto cast(VirtualGatewayDto virtualGatewayDto) {
        if (virtualGatewayDto == null) {
            return null;
        }
        VgAdvancedDto vgAdvancedDto = new VgAdvancedDto();
        vgAdvancedDto.setId(virtualGatewayDto.getId());
        vgAdvancedDto.setGwId(virtualGatewayDto.getGwId());
        vgAdvancedDto.setDomainInfos(virtualGatewayDto.getDomainInfos());
        vgAdvancedDto.setName(virtualGatewayDto.getName());
        vgAdvancedDto.setGwName(virtualGatewayDto.getGwName());
        vgAdvancedDto.setCode(virtualGatewayDto.getCode());
        vgAdvancedDto.setAddr(virtualGatewayDto.getAddr());
        vgAdvancedDto.setListenerAddr(virtualGatewayDto.getListenerAddr());
        vgAdvancedDto.setProjectIdList(virtualGatewayDto.getProjectIdList());
        vgAdvancedDto.setDescription(virtualGatewayDto.getDescription());
        vgAdvancedDto.setEnvId(virtualGatewayDto.getEnvId());
        vgAdvancedDto.setType(virtualGatewayDto.getType());
        vgAdvancedDto.setProtocol(virtualGatewayDto.getProtocol());
        vgAdvancedDto.setPort(virtualGatewayDto.getPort());
        vgAdvancedDto.setConfAddr(virtualGatewayDto.getConfAddr());
        vgAdvancedDto.setGwClusterName(virtualGatewayDto.getGwClusterName());
        vgAdvancedDto.setGwType(virtualGatewayDto.getGwType());
        vgAdvancedDto.setCreateTime(virtualGatewayDto.getCreateTime());
        vgAdvancedDto.setModifyTime(virtualGatewayDto.getModifyTime());
        vgAdvancedDto.setExtension(virtualGatewayDto.getExtension());
        return vgAdvancedDto;
    }



    public List<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = projectNames;
    }
}
