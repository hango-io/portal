package org.hango.cloud.common.infra.routeproxy.meta;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.*;
import org.hango.cloud.common.infra.base.handler.DestinationInfoListTypeHandler;
import org.hango.cloud.common.infra.route.common.DestinationInfo;
import org.hango.cloud.common.infra.route.pojo.RouteRuleMatchInfoPO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "hango_route_rule_proxy", autoResultMap = true)
public class RouteRuleProxyPO extends RouteRuleMatchInfoPO {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 发布时指定的路由规则id
     */
    private Long routeRuleId;

    /**
     * 路由规则发布指定网关id
     */
    private Long virtualGwId;

    /**
     * 路由规则发布指定的网关类型
     */
    private String gwType;


    /**
     * 路由规则发布所属项目id
     */
    private Long projectId;

    /**
     * 路由规则所发布的服务id，用于已发布路由规则搜索
     */
    private Long serviceId;

    /**
     * 域名列表
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> hosts;

    /**
     * 使能状态
     */
    private String enableState;


    /**
     * 路由超时时间
     */
    private Long timeout;


    /**
     * HttpRetry 路由重试
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private HttpRetryPO httpRetry;

    /**
     * 路由规则发布指定目标服务
     */
    @TableField(typeHandler = DestinationInfoListTypeHandler.class)
    private List<DestinationInfo> destinationServices;

    /**
     * 流量镜像服务id
     */
    private Long mirrorServiceId;


    /**
     * 流量镜像配置
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = FastjsonTypeHandler.class)
    private DestinationInfo mirrorTraffic;

    /**
     * 版本号
     */
    private Long version;

}
