package org.hango.cloud.common.infra.serviceregistry.service;


import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/12
 */
public interface IRegistryCenterService {


    /**
     * 根据服务类型查询支持的注册中心类型
     *
     * @param serviceType
     * @return
     */
    List<String> getRegistryByServiceType(long virtualGwId, String serviceType);

}
