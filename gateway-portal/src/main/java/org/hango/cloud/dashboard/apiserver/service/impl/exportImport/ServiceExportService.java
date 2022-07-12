package org.hango.cloud.dashboard.apiserver.service.impl.exportImport;

import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.dto.exportImport.ExportImportDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceExportService {
    private static Logger logger = LoggerFactory.getLogger(ServiceExportService.class);

    @Autowired
    private IServiceInfoService serviceInfoService;

    public List<ServiceInfoDto> getServiceInfo(String serviceId) {
        String[] serviceIds = serviceId.split(",");
        List<ServiceInfo> serviceInfos = new ArrayList<>();
        for (String id : serviceIds) {
            ServiceInfo serviceInfo = serviceInfoService.getServiceById(id);
            if (serviceInfo != null) {
                serviceInfos.add(serviceInfo);
            }
        }
        if (CollectionUtils.isEmpty(serviceInfos)) return new ArrayList<>();

        List<ServiceInfoDto> serviceInfoDtos = new ArrayList<>();
        for (ServiceInfo serviceInfo : serviceInfos) {
            ServiceInfoDto serviceInfoDto = BeanUtil.copy(serviceInfo, ServiceInfoDto.class);
            serviceInfoDtos.add(serviceInfoDto);
        }
        return serviceInfoDtos;
    }

    public boolean addServiceInfo(List<ExportImportDto> exportImportDtos, long projectId) {
        if (CollectionUtils.isEmpty(exportImportDtos)) return true;
        for (ExportImportDto exportImportDto : exportImportDtos) {
            ServiceInfoDto serviceInfoDto = exportImportDto.getServiceInfoDto();
            if (serviceInfoDto == null) {
                logger.info("服务导入，ServiceInfo不存在，不导入此列表下信息");
                continue;
            }
            //获取服务标识下的服务
            ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceName(serviceInfoDto.getServiceName());
            ServiceInfo displayServiceInfo = serviceInfoService.describeDisplayName(serviceInfoDto.getDisplayName(), projectId);
            //服务标识下的服务和该项目下同名服务均不存在，创建新的服务
            if (serviceInfo == null && displayServiceInfo == null) {
                serviceInfo = serviceInfoService.addServiceInfo(serviceInfoDto, projectId);
            } else {
                //该项目下存在服务名称相同的服务，覆盖该项目下服务名称相同的服务
                if (displayServiceInfo != null) {
                    serviceInfo = displayServiceInfo;
                }
                if (serviceInfo != null) {
                    if (!serviceInfo.getServiceType().equals(serviceInfoDto.getServiceType())) {
                        logger.info("导入服务，相同服务标识的服务类型不统一，不进行导入，原类型：serviceType:{},导入类型:serviceType:{}",
                                serviceInfo.getServiceType(), serviceInfoDto.getServiceType());
                        exportImportDto.getServiceInfoDto().setId(0);
                        continue;
                    }
                    //更新服务信息,认为新服务覆盖老服务
                    serviceInfo.setDisplayName(serviceInfoDto.getDisplayName());
                    serviceInfo.setDescription(serviceInfoDto.getDescription());
                    serviceInfo.setModifyDate(System.currentTimeMillis());
                    serviceInfoService.updateService(serviceInfo);
                }
            }
            if (serviceInfo != null) {
                exportImportDto.getServiceInfoDto().setId(serviceInfo.getId());
            }
        }
        return true;
    }

}
