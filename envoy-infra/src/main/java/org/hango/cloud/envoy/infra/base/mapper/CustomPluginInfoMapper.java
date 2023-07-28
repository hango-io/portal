package org.hango.cloud.envoy.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;

/**
 * @Author zhufengwei
 * @Date 2023/7/5
 */
@Mapper
public interface CustomPluginInfoMapper extends BaseMapper<CustomPluginInfo> {
}
