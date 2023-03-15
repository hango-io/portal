package org.hango.cloud.common.infra.virtualgateway.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.base.util.CommonUtil;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储各个环境的信息
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Time: 创建时间: 2018/1/17 下午5:22.
 */
public class VirtualGateway extends CommonExtension implements Serializable {

    private static final Long serialVersionUID = 7147341067988626279L;


    /**
     * 虚拟网关ID
     */
    private long id;


    /**
     * 网关ID
     */
    private long gwId;


    /**
     * 虚拟网关名称
     */
    private String name;


    /**
     * 虚拟网关标识
     */
    private String code;


    /**
     * 虚拟网关访问地址
     */
    private String addr;


    /**
     * 基于项目隔离，项目id
     */
    private String projectId;


    /**
     * 虚拟网关描述
     */
    private String description;


    /**
     * 虚拟网关类型
     */
    private String type;


    /**
     * 监听协议类型
     */
    private String protocol;


    /**
     * 监听端口
     */
    private int port;


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

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    /**
     * 将项目ID字符串转化为项目ID集合
     * 主要用于转化从数据库中取出的ID集合字符串
     *
     * @return 拆分的项目ID集合
     */
    public List<Long> getProjectIdList() {
        return CommonUtil.splitStringToLongList(projectId, ",").stream().distinct().collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
