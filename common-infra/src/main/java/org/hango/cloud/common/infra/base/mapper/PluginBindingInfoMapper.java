package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;

/**
 * @Author zhufengwei
 * @Date 2023/7/25
 */
@Mapper
public interface PluginBindingInfoMapper extends BaseMapper<PluginBindingInfo> {

}
