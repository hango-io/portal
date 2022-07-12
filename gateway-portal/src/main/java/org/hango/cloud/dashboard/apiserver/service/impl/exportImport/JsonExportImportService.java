package org.hango.cloud.dashboard.apiserver.service.impl.exportImport;

import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.dto.exportImport.ExportImportDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.gdashboard.api.dto.ApiExportDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.service.IApiExportService;
import org.hango.cloud.gdashboard.api.service.IApiModelExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关导入导出相关service，生成导入或导出的相关数据
 * 导出：将现有网关平台相关元数据导出，形成json文件
 * 导入：根据json文件导入至网关平台
 */
@Service
public class JsonExportImportService extends BaseJsonConvertService<List<ExportImportDto>> {
    private static Logger logger = LoggerFactory.getLogger(JsonExportImportService.class);

    @Autowired
    private ServiceExportService serviceExport;
    @Autowired
    private IApiModelExportService apiModelExport;
    @Autowired
    private IApiExportService apiExport;

    /**
     * 根据服务ID，导出相关元数据
     *
     * @param serviceId
     * @return
     */
    public List<ExportImportDto> exportImportDtoList(String serviceId) {
        List<ServiceInfoDto> serviceInfoDtos = serviceExport.getServiceInfo(serviceId);
        if (CollectionUtils.isEmpty(serviceInfoDtos)) return new ArrayList<>();

        List<ExportImportDto> exportImportDtos = new ArrayList<>();
        for (ServiceInfoDto serviceInfoDto : serviceInfoDtos) {
            List<CreateApiModelDto> apiModels = apiModelExport.getApiModels(serviceInfoDto.getId());
            List<ApiExportDto> apiInfo = apiExport.getApiInfo(serviceInfoDto.getId(), serviceInfoDto.getServiceType());
            exportImportDtos.add(new ExportImportDto(serviceInfoDto, apiModels, apiInfo));
        }
        return exportImportDtos;
    }


    /**
     * 导入相关元数据
     *
     * @param exportImportDtos
     * @param projectId
     */
    @Transactional(rollbackFor = Exception.class)
    public void importDataFromJson(List<ExportImportDto> exportImportDtos, long projectId) {
        serviceExport.addServiceInfo(exportImportDtos, projectId);
        if (CollectionUtils.isEmpty(exportImportDtos)) return;

        exportImportDtos.forEach(exportImportDto -> {
            if (exportImportDto.getServiceInfoDto() != null && exportImportDto.getServiceInfoDto().getId() != 0) {
                if (exportImportDto.getApiModelDtoList() != null) {
                    apiModelExport.addApiModel(exportImportDto.getApiModelDtoList(), exportImportDto.getServiceInfoDto().getId());
                }
                if (exportImportDto.getApiExportDtos() != null) {
                    apiExport.addApiInfos(exportImportDto.getApiExportDtos(), exportImportDto.getServiceInfoDto().getId(),
                            exportImportDto.getServiceInfoDto().getServiceType(), projectId);
                }
            }
        });
    }

    public ErrorCode checkJsonFile(MultipartFile file) {
        if (file.isEmpty() || file == null) {
            return CommonErrorCode.FileIsEmpty;
        }
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        //文件格式校验
        if (!".json".equals(fileType)) {
            return CommonErrorCode.IllegalFileFormat;
        }
        return CommonErrorCode.Success;
    }

}
