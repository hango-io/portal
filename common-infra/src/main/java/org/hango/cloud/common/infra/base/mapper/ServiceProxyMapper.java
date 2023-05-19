package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/3/16
 */
@Mapper
public interface ServiceProxyMapper extends BaseMapper<ServiceProxyInfo> {
}
