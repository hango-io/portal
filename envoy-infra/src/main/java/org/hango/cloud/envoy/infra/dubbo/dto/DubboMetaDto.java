package org.hango.cloud.envoy.infra.dubbo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
public class DubboMetaDto extends CommonExtensionDto implements Comparable<DubboMetaDto> {

    /**
     * 主键ID
     */
    @NotNull
    @JSONField(name = "Id")
    private long id;

    /**
     * 网关ID
     */
    @NotNull
    @JSONField(name = "Id")
    private Long virtualGwId;


    /**
     * 应用名称
     */
    @NotBlank
    @JSONField(name = "ApplicationName")
    private String applicationName;


    /**
     * dubbo协议版本
     */
    @NotBlank
    @JSONField(name = "ProtocolVersion")
    private String protocolVersion;


    /**
     * 接口名称
     */
    @NotBlank
    @JSONField(name = "InterfaceName")
    private String interfaceName;


    /**
     * 分组
     */

    @JSONField(name = "Group")
    private String group = StringUtils.EMPTY;
    ;


    /**
     * 版本
     */

    @JSONField(name = "Version")
    private String version = StringUtils.EMPTY;
    ;


    /**
     * 方法名称
     */
    @NotBlank
    @JSONField(name = "Method")
    private String method;


    /**
     * 参数列表
     */

    @JSONField(name = "RequestParams")
    private List<String> params;


    /**
     * 返回类型
     */

    @JSONField(name = "ResponseReturn")
    private String returns;


    /**
     * 创建时间
     */

    @JSONField(name = "CreateTime")
    private long createTime;

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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(Long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getIgv() {
        return CommonUtil.removeEnd(":", StringUtils.joinWith(":", interfaceName, group, version));
    }

    @Override
    public int compareTo(DubboMetaDto o) {
        int c1 = StringUtils.compare(this.interfaceName, o.getInterfaceName());
        if (c1 != 0) {
            return c1;
        }

        int c2 = StringUtils.compare(this.group, o.getGroup());
        if (c2 != 0) {
            return c2;
        }

        int c3 = StringUtils.compare(this.version, o.getVersion());
        if (c3 != 0) {
            return c3;
        }

        int c4 = StringUtils.compare(this.method, o.getMethod());
        if (c4 != 0) {
            return c4;
        }
        int o1 = this.params == null ? 0 : this.params.size();
        int o2 = o.getParams() == null ? 0 : o.getParams().size();
        int i = o1 - o2;
        if (i != 0) {
            return i;
        }
        if (o1 == 0 && o2 == 0) {
            return 0;
        }
        int c5 = 0;
        for (int i1 = 0; i1 < this.params.size(); i1++) {
            c5 = StringUtils.compare(this.params.get(i), o.getParams().get(i));
            if (c5 == 0) {
                continue;
            }
        }
        return c5;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}