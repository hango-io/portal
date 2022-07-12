package org.hango.cloud.dashboard.apiserver.service.sdk;

import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRestfulSdkService {
    String generateApiSdk(String apiId, String UUID, boolean singleApiFlag);


    String generateServiceSdk(String serviceId, String UUID);

    boolean generateClient(List<ApiInfo> apiInfoList, String serviceName, String uuid);

    boolean generateModel(String serviceId, String serviceName, String uuid);

    void deleteTemp(String UUID);

    ResponseEntity<InputStreamResource> supplyDownload(String filePath, String fileName) throws Exception;
}
