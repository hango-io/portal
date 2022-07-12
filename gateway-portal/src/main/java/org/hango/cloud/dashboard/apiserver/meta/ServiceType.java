package org.hango.cloud.dashboard.apiserver.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/11/6
 */
public enum ServiceType {

    http,
    dubbo,
    serviceMesh,
    webservice,
    grpc;


    /**
     * 获取服务类别（默认HTTP服务）
     *
     * @param serviceType
     * @return
     */
    public static String getByName(String serviceType) {
        for (ServiceType type : ServiceType.values()) {
            if (type.name().equalsIgnoreCase(serviceType)) {
                return type.name();
            }
        }
        return http.name();
    }
}
