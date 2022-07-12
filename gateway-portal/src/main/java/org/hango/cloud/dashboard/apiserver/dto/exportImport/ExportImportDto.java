package org.hango.cloud.dashboard.apiserver.dto.exportImport;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.gdashboard.api.dto.ApiExportDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;

import java.util.List;

public class ExportImportDto {
    @JSONField(name = "ServiceInfo")
    private ServiceInfoDto serviceInfoDto;
    @JSONField(name = "ApiModelList")
    private List<CreateApiModelDto> apiModelDtoList;
    @JSONField(name = "ApiInfoList")
    private List<ApiExportDto> apiExportDtos;

    public ExportImportDto(ServiceInfoDto serviceInfoDto, List<CreateApiModelDto> apiModelDtoList, List<ApiExportDto> apiExportDtos) {
        this.serviceInfoDto = serviceInfoDto;
        this.apiModelDtoList = apiModelDtoList;
        this.apiExportDtos = apiExportDtos;
    }

    public ExportImportDto() {
        super();
    }

    public ServiceInfoDto getServiceInfoDto() {
        return serviceInfoDto;
    }

    public void setServiceInfoDto(ServiceInfoDto serviceInfoDto) {
        this.serviceInfoDto = serviceInfoDto;
    }

    public List<CreateApiModelDto> getApiModelDtoList() {
        return apiModelDtoList;
    }

    public void setApiModelDtoList(List<CreateApiModelDto> apiModelDtoList) {
        this.apiModelDtoList = apiModelDtoList;
    }

    public List<ApiExportDto> getApiExportDtos() {
        return apiExportDtos;
    }

    public void setApiExportDtos(List<ApiExportDto> apiExportDtos) {
        this.apiExportDtos = apiExportDtos;
    }
}
