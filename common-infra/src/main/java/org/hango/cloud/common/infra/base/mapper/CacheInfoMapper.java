package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.cache.meta.CacheInfo;

/**
 * @Author zhufengwei
 * @Date 2023/6/26
 */
@Mapper
public interface CacheInfoMapper extends BaseMapper<CacheInfo> {

}
