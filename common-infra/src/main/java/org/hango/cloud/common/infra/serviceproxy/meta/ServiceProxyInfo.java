package org.hango.cloud.common.infra.serviceproxy.meta;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;
/**
 * @Author zhufengwei
 * @Date 2023/1/10
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@TableName(value = "hango_service_proxy", autoResultMap = true)
public class ServiceProxyInfo extends CommonExtension implements Serializable {

    private static final long serialVersionUID = 2134478317506914517L;

    /**
     * 主键
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;


    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;


    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;


    /**
     * 服务名称
     */
    private String name;


    /**
     * 服务发布所属虚拟网关id
     */
    private Long virtualGwId;


    /**
     * 发布所属项目id
     */
    private Long projectId;


    /**
     * 服务别名
     */
    private String alias;


    /**
     * 域名
     */
    private String hosts;


    /**
     * 服务协议
     */
    private String protocol;


    /**
     * 发布关联真实网关服务
     */
    private String backendService;


    /**
     * 发布策略，STATIC/DYNAMIC
     */
    private String publishType;


    /**
     * 负载均衡
     */
    private String loadBalancer;


    /**
     * 版本集合
     */
    private String subsets;


    /**
     * 注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes
     */
    private String registryCenterType;


    /**
     * 负载均衡和连接池配置
     */
    private String trafficPolicy;


    /**
     * 网关类型
     */
    private String gwType;


    /**
     * 版本号
     */
    private long version;


    /**
     * 备注
     */
    private String description;
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
