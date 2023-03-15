package org.hango.cloud.common.infra.gateway.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 网关信息表
 * @date 2022/10/25
 */
public class Gateway extends CommonExtension implements Serializable {

    private static final long serialVersionUID = 6913444445012869099L;
    /**
     * 网关ID
     */
    private long id;


    /**
     * 网关名称
     */
    private String name;


    /**
     * 所属环境
     */
    private String envId;


    /**
     * 网关service类型， ClusterIP/NodePort
     */
    private String svcType;

    /**
     * 网关service名称
     */
    private String svcName;


    /**
     * 网关类型
     */
    private String type;


    /**
     * 网关集群名称
     */
    private String gwClusterName;


    /**
     * 配置下发地址
     */
    private String confAddr;


    /**
     * 备注
     */
    private String description;


    /**
     * 创建时间
     */
    private long createTime;


    /**
     * 修改时间
     */
    private long modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getSvcType() {
        return svcType;
    }

    public void setSvcType(String svcType) {
        this.svcType = svcType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGwClusterName() {
        return gwClusterName;
    }

    public void setGwClusterName(String gwClusterName) {
        this.gwClusterName = gwClusterName;
    }

    public String getConfAddr() {
        return confAddr;
    }

    public void setConfAddr(String confAddr) {
        this.confAddr = confAddr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getSvcName() {
        return svcName;
    }

    public void setSvcName(String svcName) {
        this.svcName = svcName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}