package org.hango.cloud.common.infra.base.handler;

import com.alibaba.fastjson.TypeReference;
import org.apache.ibatis.type.MappedTypes;
import org.hango.cloud.common.infra.route.pojo.RouteMapMatchInfo;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/12
 */
@MappedTypes({List.class})
public class RouteMatchInfoListTypeHandler extends  ListTypeHandler<RouteMapMatchInfo> {
    @Override
    protected TypeReference<List<RouteMapMatchInfo>> specificType() {
        return new TypeReference<List<RouteMapMatchInfo>>() {
        };
    }
}