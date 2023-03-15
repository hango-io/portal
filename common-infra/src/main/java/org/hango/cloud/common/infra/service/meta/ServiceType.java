package org.hango.cloud.common.infra.service.meta;

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
    serviceMesh,
    webservice,
    grpc;


    /**
     * 获取服务类别（无默认值）
     *
     * @param serviceType
     * @return
     */
    public static ServiceType getServiceTypeByName(String serviceType) {
        for (ServiceType type : ServiceType.values()) {
            if (type.name().equalsIgnoreCase(serviceType)) {
                return type;
            }
        }
        return null;
    }
}
