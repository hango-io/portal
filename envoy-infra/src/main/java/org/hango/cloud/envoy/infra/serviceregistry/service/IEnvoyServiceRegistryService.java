package org.hango.cloud.envoy.infra.serviceregistry.service;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/11/14
 */
public interface IEnvoyServiceRegistryService {


    /**
     * 获取注册中心列表
     */
    List<String> getRegistryTypeList(long virtualGwId, String serviceType);


}
