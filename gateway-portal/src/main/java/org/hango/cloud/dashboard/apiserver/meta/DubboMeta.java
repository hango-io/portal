package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
public class DubboMeta implements Serializable {

    private static final long serialVersionUID = -7113890606504373140L;
    /**
     * 主键ID
     */
    private long id;


    /**
     * 应用名称
     */
    private String applicationName;


    /**
     * dubbo协议版本
     */
    private String protocolVersion;


    /**
     * 接口名称
     */
    private String interfaceName;


    /**
     * 分组
     */
    private String dubboGroup;


    /**
     * 版本
     */
    private String dubboVersion;


    /**
     * 方法名称
     */
    private String method;


    /**
     * 参数列表
     */
    private String dubboParams;


    /**
     * 返回类型
     */
    private String dubboReturns;


    /**
     * 创建时间
     */
    private long createTime;


    /**
     * 网关ID
     */
    private long gwId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getDubboGroup() {
        return dubboGroup;
    }

    public void setDubboGroup(String dubboGroup) {
        this.dubboGroup = dubboGroup;
    }

    public String getDubboVersion() {
        return dubboVersion;
    }

    public void setDubboVersion(String dubboVersion) {
        this.dubboVersion = dubboVersion;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDubboParams() {
        return dubboParams;
    }

    public void setDubboParams(String dubboParams) {
        this.dubboParams = dubboParams;
    }

    public String getDubboReturns() {
        return dubboReturns;
    }

    public void setDubboReturns(String dubboReturns) {
        this.dubboReturns = dubboReturns;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}