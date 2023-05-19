package org.hango.cloud.common.infra.route.pojo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.handler.RouteMatchInfoListTypeHandler;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/10
 */
@Getter
@Setter
public class RouteMatchInfoPO extends CommonExtension {
    /**
     * uri
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = FastjsonTypeHandler.class)
    private RouteStringMatchInfo uri;

    /**
     * method
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = FastjsonTypeHandler.class)
    private List<String> method;

    /**
     * header
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = RouteMatchInfoListTypeHandler.class)
    private List<RouteMapMatchInfo> header;

    /**
     * queryParam
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED, typeHandler = RouteMatchInfoListTypeHandler.class)
    private List<RouteMapMatchInfo> queryParam;

    /**
     * 路由规则优先级
     */
    private Long priority;

    /**
     * 路由规则orders，发送至api-plane
     * orders = priority * 100000 + isExact * 20000 + pathLength * 20 + routeNumber
     */
    private Long orders;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
