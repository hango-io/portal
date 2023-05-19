package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;

/**
 * @Author zhufengwei
 * @Date 2023/3/28
 */
@Mapper
public interface VirtualGatewayInfoMapper extends BaseMapper<VirtualGateway> {
}
