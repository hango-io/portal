package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;

/**
 * @Author zhufengwei
 * @Date 2023/7/27
 */
@Mapper
public interface PluginTemplateInfoMapper extends BaseMapper<PluginTemplateInfo> {
}
