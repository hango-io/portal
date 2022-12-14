package org.hango.cloud.trafficmark;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.envoy.infra.trafficmark.dto.TrafficMarkDto;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.envoy.infra.trafficmark.service.impl.TrafficMarkServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
public class TrafficMarkServiceImplTest extends BaseServiceImplTest {

    @Autowired
    private TrafficMarkServiceImpl trafficMarkService;

    @Test
    public void test_describe_with_order() {
        ProjectTraceHolder.setProId(1);
        String trafficMarkDtoJson1 = "{\"TrafficMatch\":\"Header\",\"RouteRuleNames\":\"e2e1\",\"ServiceName\":\"e2e1\",\"EnableStatus\":0,\"ColorTag\":\"testordercolor1\",\"TrafficParam\":[{\"Type\":\"regex\",\"ParamName\":\"testorderquery1\",\"Value1\":\"val1\",\"_formTableKey\":1671021985650}],\"RouteRuleIds\":\"761\",\"TrafficColorName\":\"testorder1\",\"Protocol\":\"http\",\"GwName\":\"gateway-proxy\",\"GwId\":1}";
        String trafficMarkDtoJson2 = "{\"TrafficMatch\":\"Header\",\"RouteRuleNames\":\"e2e2\",\"ServiceName\":\"e2e2\",\"EnableStatus\":0,\"ColorTag\":\"testordercolor2\",\"TrafficParam\":[{\"Type\":\"regex\",\"ParamName\":\"testorderquery2\",\"Value2\":\"val2\",\"_formTableKey\":1671021985650}],\"RouteRuleIds\":\"760\",\"TrafficColorName\":\"testorder2\",\"Protocol\":\"http\",\"GwName\":\"gateway-proxy\",\"GwId\":1}";
        TrafficMarkDto trafficMarkDto1 = JSON.parseObject(trafficMarkDtoJson1, TrafficMarkDto.class);
        TrafficMarkDto trafficMarkDto2 = JSON.parseObject(trafficMarkDtoJson2, TrafficMarkDto.class);
        trafficMarkService.addTrafficColorInfo(trafficMarkDto1);
        trafficMarkService.addTrafficColorInfo(trafficMarkDto2);


        List<TrafficMarkInfo> createTimeDescList = trafficMarkService.getTrafficColorByTagLimit(null, "create_time", "desc", 0, 2);
        Assert.isTrue(2 == createTimeDescList.size());
        Assert.isTrue(createTimeDescList.get(0).getCreateTime() > createTimeDescList.get(1).getCreateTime());

        List<TrafficMarkInfo> createTimeAscList = trafficMarkService.getTrafficColorByTagLimit(null, "create_time", "asc", 0, 2);
        Assert.isTrue(2 == createTimeAscList.size());
        Assert.isTrue(createTimeAscList.get(0).getCreateTime() < createTimeAscList.get(1).getCreateTime());


        List<TrafficMarkInfo> routeRuleIdsAscList = trafficMarkService.getTrafficColorByTagLimit(null, "route_rule_ids", "asc", 0, 2);
        Assert.isTrue(2 == routeRuleIdsAscList.size());
        Assert.isTrue(Long.parseLong(routeRuleIdsAscList.get(0).getRouteRuleIds()) < Long.parseLong(routeRuleIdsAscList.get(1).getRouteRuleIds()));

    }
}
