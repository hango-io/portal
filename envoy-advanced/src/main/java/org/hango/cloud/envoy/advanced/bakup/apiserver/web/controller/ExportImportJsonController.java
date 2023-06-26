package org.hango.cloud.envoy.advanced.bakup.apiserver.web.controller;


import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.envoy.advanced.bakup.apiserver.dto.exportImport.ExportImportDto;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.advanced.bakup.apiserver.service.JsonConvertService;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 元数据导入导出
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class ExportImportJsonController extends AbstractController {
    @Autowired
    private JsonConvertService jsonExportImportService;

    /**
     * 导出元数据
     *
     * @return
     */
    @RequestMapping(params = {"Action=ExportData"})
    public void exportData(@RequestParam(value = "ServiceId") String serviceId, HttpServletResponse response) {
        List<ExportImportDto> exportImportDtos = jsonExportImportService.exportImportDtoList(serviceId);
        String jsonString = jsonExportImportService.exportToJson(exportImportDtos);
        jsonExportImportService.downloadJsonFile(response, jsonString, "");
    }

    /**
     * 导入元数据
     *
     * @param file
     * @return
     */
    @RequestMapping(params = {"Action=ImportByFile"}, method = RequestMethod.POST)
    public String importData(@RequestParam("file") MultipartFile file) {
        String jsonStringFromUploadedFile = jsonExportImportService.getJsonStringFromUploadedFile(file);
        List<ExportImportDto> exportImportDtos = null;
        try {
            exportImportDtos = jsonExportImportService.importFromJsonArray(jsonStringFromUploadedFile, ExportImportDto.class);
        } catch (Exception e) {
            logger.info("json解析错误，e:{}", e);
            return apiReturn(CommonErrorCode.ILLEGAL_FILE_FORMAT);
        }

        ErrorCode errorCode = jsonExportImportService.checkJsonFile(file);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        jsonExportImportService.importDataFromJson(exportImportDtos, ProjectTraceHolder.getProId());
        return apiReturn(CommonErrorCode.SUCCESS);
    }

}
