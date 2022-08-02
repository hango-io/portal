package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

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
     * 保存注册中心信息
     *
     * @param registryCenter
     */
    void saveRegistryCenter(RegistryCenterDto registryCenter);


    /**
     * 删除注册中心信息
     *
     * @param id
     */
    void deleteRegistryCenter(long id);


    /**
     * 获取所有注册中心信息
     *
     * @return
     */
    List<RegistryCenterDto> findAll();

    /**
     * 通过类型获取注册中心信息
     *
     * @param registryType 注册中心类型
     * @param gwId         网关id
     * @return
     */
    List<RegistryCenterDto> findByType(String registryType, long gwId);


    /**
     * 通过地址和类型获取注册中心信息
     * 注: 目前注册中心由底层配置，并且未进行项目隔离，因此不同项目通过同一registryType&registryAddr 获取的注册中心信息一致。
     * 所以该接口暂不需携带项目隔离信息
     *
     * @param registryType
     * @param registryAddr
     * @return
     */
    RegistryCenterDto findByTypeAndAddr(String registryType, String registryAddr);

    /**
     * 通过类型获取项目下注册中心信息
     *
     * @param registryType
     * @param projectId
     * @return
     */
    List<RegistryCenterDto> findByTypeAndProject(String registryType, long projectId);

    /**
     * 通过ID获取注册中心信息
     *
     * @param id
     * @return
     */
    RegistryCenterDto getRegistryCenter(long id);


    /**
     * 参数校验
     *
     * @param registryCenter
     * @return
     */
    ErrorCode checkParam(RegistryCenterDto registryCenter);

    /**
     * 从Consul获取应用
     *
     * @param registryCenter
     * @return
     */
    List<String> getApplicationsFromConsul(String registryCenter);

    /**
     * 根据服务类型查询支持的注册中心类型
     * @param serviceType
     * @return
     */
    List<String> describeRegistryTypesByServiceType(String serviceType);
}
