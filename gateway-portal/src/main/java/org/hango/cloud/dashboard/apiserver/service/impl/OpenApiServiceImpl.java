package org.hango.cloud.dashboard.apiserver.service.impl;

import org.hango.cloud.dashboard.apiserver.dto.PublishedServiceInfoForSkiffDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IOpenApiService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenApiServiceImpl implements IOpenApiService {

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Override
    public List<PublishedServiceInfoForSkiffDto> getPublishedServiceInfoByAccountId(String accountId, HttpServletRequest request) {

        GatewayInfo gatewayInfo;
        PublishedServiceInfoForSkiffDto publishedServiceInfoForSkiffDto;
        List<ServiceInfo> serviceInfoList = serviceInfoService.findAll();

        List<PublishedServiceInfoForSkiffDto> list = new ArrayList<>();

        return list;
    }

}
