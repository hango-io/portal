package org.hango.cloud.dashboard.apiserver.web.controller;

import org.hango.cloud.dashboard.apiserver.dto.exportImport.ExportImportDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.impl.exportImport.JsonExportImportService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
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
    private JsonExportImportService jsonExportImportService;

    /**
     * 导出元数据
     *
     * @return
     */
    @MethodReentrantLock
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
    @MethodReentrantLock
    @RequestMapping(params = {"Action=ImportByFile"}, method = RequestMethod.POST)
    public String importData(@RequestParam("file") MultipartFile file) {
        String jsonStringFromUploadedFile = jsonExportImportService.getJsonStringFromUploadedFile(file);
        List<ExportImportDto> exportImportDtos = null;
        try {
            exportImportDtos = jsonExportImportService.importFromJsonArray(jsonStringFromUploadedFile, ExportImportDto.class);
        } catch (Exception e) {
            logger.info("json解析错误，e:{}", e);
            return apiReturn(CommonErrorCode.IllegalFileFormat);
        }

        ErrorCode errorCode = jsonExportImportService.checkJsonFile(file);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        jsonExportImportService.importDataFromJson(exportImportDtos, ProjectTraceHolder.getProId());
        return apiReturn(CommonErrorCode.Success);
    }

}
