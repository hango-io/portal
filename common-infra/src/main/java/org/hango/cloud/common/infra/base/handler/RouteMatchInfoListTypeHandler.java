package org.hango.cloud.common.infra.base.handler;

import com.alibaba.fastjson.TypeReference;
import org.apache.ibatis.type.MappedTypes;
import org.hango.cloud.common.infra.route.common.RouteRuleMapMatchInfo;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/12
 */
@MappedTypes({List.class})
public class RouteMatchInfoListTypeHandler extends  ListTypeHandler<RouteRuleMapMatchInfo> {
    @Override
    protected TypeReference<List<RouteRuleMapMatchInfo>> specificType() {
        return new TypeReference<List<RouteRuleMapMatchInfo>>() {
        };
    }
}