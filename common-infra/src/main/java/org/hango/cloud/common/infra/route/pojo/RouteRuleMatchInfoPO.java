package org.hango.cloud.common.infra.route.pojo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.handler.RouteMatchInfoListTypeHandler;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.route.common.RouteRuleMapMatchInfo;
import org.hango.cloud.common.infra.route.common.RouteStringMatchInfo;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/10
 */
@Getter
@Setter
public class RouteRuleMatchInfoPO extends CommonExtension {
    /**
     * uri
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = FastjsonTypeHandler.class)
    private RouteStringMatchInfo uri;

    /**
     * method
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = FastjsonTypeHandler.class)
    private RouteStringMatchInfo method;

    /**
     * host
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = FastjsonTypeHandler.class)
    private RouteStringMatchInfo host;

    /**
     * header
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = RouteMatchInfoListTypeHandler.class)
    private List<RouteRuleMapMatchInfo> header;

    /**
     * queryParam
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = RouteMatchInfoListTypeHandler.class)
    private List<RouteRuleMapMatchInfo> queryParam;

    /**
     * 路由规则优先级
     */
    private Long priority;

    /**
     * 路由规则orders，发送至api-plane
     * orders = priority * 100000 + isExact * 20000 + pathLength * 20 + routeNumber
     */
    private Long orders;
}
