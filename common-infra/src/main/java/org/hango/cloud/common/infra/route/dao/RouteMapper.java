package org.hango.cloud.common.infra.route.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.route.pojo.RoutePO;

/**
 * @Author zhufengwei
 * @Date 2023/1/10
 */
@Mapper
public interface RouteMapper extends BaseMapper<RoutePO> {

}