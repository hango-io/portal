package org.hango.cloud.common.advanced.serviceproxy.service;

import org.hango.cloud.common.advanced.serviceproxy.dto.MetaServiceDto;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/5/24
 */
public interface IMetaService {

    /**
     * 获取元数据
     *
     * @param serviceName
     * @param offset
     * @param limit
     * @return
     */
    PageResult<List<MetaServiceDto>> listMetaService(String serviceName, long offset, long limit);


    /**
     * 创建服务
     *
     * @param serviceProxyDto
     */
    ErrorCode addMetaService(ServiceProxyDto serviceProxyDto);

    /**
     * 删除服务
     *
     * @param serviceProxyDto
     */
    ErrorCode deleteMetaService(ServiceProxyDto serviceProxyDto);
}
