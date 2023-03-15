package org.hango.cloud.envoy.infra.trafficmark.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * 流量染色相关dto
 *
 * @author qilu
 */
public class TrafficMarkDto {
    /**
     * 流量染色参数
     */
    @JSONField(name = "TrafficParam")
    @Valid
    List<TrafficMarkParamDto> trafficMarkParams;
    /**
     * 数据库主键自增id
     */
    @JSONField(name = "Id")
    private long id;
    /**
     * 路由规则发布指定的网关id
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;
    /**
     * 网关名
     */
    @JSONField(name = "VirtualGwName")
    private String virtualGwName;
    /**
     * 协议
     */
    @JSONField(name = "Protocol")
    private String protocol = "http";
    /**
     * 流量染色规则名称
     */
    @JSONField(name = "TrafficColorName")
    @Pattern(regexp = Const.REGEX_TRAFFIC_MARK_RULE_NAME)
    private String trafficColorName;
    /**
     * 创建时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;
    /**
     * 更新时间
     */
    @JSONField(name = "UpdateTime")
    private long updateTime;
    /**
     * 流量染色的路由规则id列表，英文逗号(,)分割
     */
    @JSONField(name = "RouteRuleIds")
    private String routeRuleIds;
    /**
     * 流量染色开启状态：0表示关闭；1表示开启
     */
    @JSONField(name = "EnableStatus")
    private int enableStatus;
    /**
     * 流量匹配 当前仅支持Header匹配
     */
    @JSONField(name = "TrafficMatch")
    private String trafficMatch = "Header";
    /**
     * 染色标识
     */
    @JSONField(name = "ColorTag")
    private String colorTag;
    /**
     * 服务名称
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * 路由名称列表，英文逗号(,)分割
     */
    @JSONField(name = "RouteRuleNames")
    private String routeRuleNames;

    public static TrafficMarkInfo toMeta(TrafficMarkDto trafficMarkDto) {
        TrafficMarkInfo trafficMarkInfo = new TrafficMarkInfo();
        trafficMarkInfo.setId(trafficMarkDto.getId());
        trafficMarkInfo.setVirtualGwId(trafficMarkDto.getVirtualGwId());
        trafficMarkInfo.setVirtualGwName(trafficMarkDto.getVirtualGwName());
        trafficMarkInfo.setProtocol(trafficMarkDto.getProtocol());
        trafficMarkInfo.setCreateTime(trafficMarkDto.getCreateTime());
        trafficMarkInfo.setUpdateTime(trafficMarkDto.getUpdateTime());
        trafficMarkInfo.setRouteRuleIds(trafficMarkDto.getRouteRuleIds());
        trafficMarkInfo.setEnableStatus(trafficMarkDto.getEnableStatus());
        trafficMarkInfo.setTrafficMatch(trafficMarkDto.getTrafficMatch());
        trafficMarkInfo.setColorTag(trafficMarkDto.getColorTag());
        trafficMarkInfo.setTrafficParam(!CollectionUtils.isEmpty(trafficMarkDto.getTrafficMarkParams()) ?
                JSON.toJSONString(trafficMarkDto.getTrafficMarkParams()) : null);
        trafficMarkInfo.setTrafficColorName(trafficMarkDto.getTrafficColorName());
        trafficMarkInfo.setServiceName(trafficMarkDto.getServiceName());
        trafficMarkInfo.setRouteRuleNames(trafficMarkDto.getRouteRuleNames());
        return trafficMarkInfo;
    }

    public static TrafficMarkDto toDto(TrafficMarkInfo trafficMarkInfo) {
        TrafficMarkDto trafficMarkDto = new TrafficMarkDto();
        trafficMarkDto.setId(trafficMarkInfo.getId());
        trafficMarkDto.setVirtualGwId(trafficMarkInfo.getVirtualGwId());
        trafficMarkDto.setVirtualGwName(trafficMarkInfo.getVirtualGwName());
        trafficMarkDto.setProtocol(trafficMarkInfo.getProtocol());
        trafficMarkDto.setCreateTime(trafficMarkInfo.getCreateTime());
        trafficMarkDto.setUpdateTime(trafficMarkInfo.getUpdateTime());
        trafficMarkDto.setRouteRuleIds(trafficMarkInfo.getRouteRuleIds());
        trafficMarkDto.setEnableStatus(trafficMarkInfo.getEnableStatus());
        trafficMarkDto.setTrafficMatch(trafficMarkInfo.getTrafficMatch());
        trafficMarkDto.setColorTag(trafficMarkInfo.getColorTag());
        trafficMarkDto.setTrafficMarkParams(setTrafficColorParamForDto(trafficMarkInfo));
        trafficMarkDto.setTrafficColorName(trafficMarkInfo.getTrafficColorName());
        trafficMarkDto.setServiceName(trafficMarkInfo.getServiceName());
        trafficMarkDto.setRouteRuleNames(trafficMarkInfo.getRouteRuleNames());
        return trafficMarkDto;
    }

    /**
     * 为dto增加流量染色参数信息，因为db中存储的是字符串，dto中是Object，不能直接用BeanUtil.copy来赋值
     * 用于前端展示
     *
     * @param trafficMarkInfo 流量染色参数
     * @return 流量染色参数信息
     */
    public static List<TrafficMarkParamDto> setTrafficColorParamForDto(TrafficMarkInfo trafficMarkInfo) {
        //增加流量染色参数信息
        if (StringUtils.isNotBlank(trafficMarkInfo.getTrafficParam())) {
            List<JSONObject> trafficColorParamObject = JSON.parseObject(trafficMarkInfo.getTrafficParam(), List.class);
            List<TrafficMarkParamDto> trafficMarkParams = new ArrayList<>();
            for (JSONObject trafficColorParamObjectTemp : trafficColorParamObject) {
                trafficMarkParams.add(JSONObject.toJavaObject(trafficColorParamObjectTemp, TrafficMarkParamDto.class));
            }
            return trafficMarkParams;
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getVirtualGwName() {
        return virtualGwName;
    }

    public void setVirtualGwName(String virtualGwName) {
        this.virtualGwName = virtualGwName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getRouteRuleIds() {
        return routeRuleIds;
    }

    public void setRouteRuleIds(String routeRuleIds) {
        this.routeRuleIds = routeRuleIds;
    }

    public int getEnableStatus() {
        return enableStatus;
    }

    public void setEnableStatus(int enableStatus) {
        this.enableStatus = enableStatus;
    }

    public String getTrafficMatch() {
        return trafficMatch;
    }

    public void setTrafficMatch(String trafficMatch) {
        this.trafficMatch = trafficMatch;
    }

    public String getColorTag() {
        return colorTag;
    }

    public void setColorTag(String colorTag) {
        this.colorTag = colorTag;
    }

    public List<TrafficMarkParamDto> getTrafficMarkParams() {
        return trafficMarkParams;
    }

    public void setTrafficMarkParams(List<TrafficMarkParamDto> trafficMarkParams) {
        this.trafficMarkParams = trafficMarkParams;
    }

    public String getTrafficColorName() {
        return trafficColorName;
    }

    public void setTrafficColorName(String trafficColorName) {
        this.trafficColorName = trafficColorName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRouteRuleNames() {
        return routeRuleNames;
    }

    public void setRouteRuleNames(String routeRuleNames) {
        this.routeRuleNames = routeRuleNames;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
