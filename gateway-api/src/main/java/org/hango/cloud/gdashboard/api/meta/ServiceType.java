package org.hango.cloud.gdashboard.api.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/11/6
 */
@SuppressWarnings("java:S115")
public enum ServiceType {

    http,
    dubbo,
    webservice;

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
