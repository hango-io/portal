package org.hango.cloud.common.infra.route.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.handler.DestinationInfoListTypeHandler;
import org.hango.cloud.common.infra.base.handler.LongListTypeHandler;

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
public class RoutePO extends RouteMatchInfoPO {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 路由规则别名
     */
    private String alias;


    private String description;

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
     * 路由规则发布指定网关id
     */
    private Long virtualGwId;

    /**
     * 路由规则发布指定的网关类型
     */
    private String gwType;

    /**
     * 路有优先级
     */
    private Long priority;

    /**
     * 匹配条件优先级
     */
    private Long orders;

    /**
     * 路由规则发布所属项目id
     */
    private Long projectId;

    /**
     * 路由规则所关联的服务id组
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> serviceIds;

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
     * 路由匹配 path
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private RouteStringMatchInfo uri;

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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
