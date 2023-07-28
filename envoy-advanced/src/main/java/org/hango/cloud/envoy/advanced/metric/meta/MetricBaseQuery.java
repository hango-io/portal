package org.hango.cloud.envoy.advanced.metric.meta;

import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.TimeQuery;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/5
 */
@Getter
@Setter
public class MetricBaseQuery extends TimeQuery {

    /**
     * 监控地址, 一定不为空
     */
    private String metricAddress;

    /**
     * 集群名称, 一定存在
     */
    private String clusterName;

    /**
     * 虚拟网关名称,除查询网关指标外一定存在
     */
    private String virtualGwCode;


    /**
     * 监控类型
     *
     * @see MetricTypeEnum
     */
    private String[] metricTypes;

    /**
     * 虚拟网关标识
     */
    private Integer virtualGatewayPort;

    /**
     * 项目id , 除查询网关、虚拟网关指标外一定存在
     */
    private long projectId = ProjectTraceHolder.getProId();

}
