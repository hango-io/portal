package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/9
 */
@JSONType(ignores = "name")
public class EnvoyGatewaySettingDto implements Serializable {

    private static final long serialVersionUID = 1949653022969532468L;


    @JSONField(name = "GwId")
    @NotNull
    private long gwId;

    private String name;

    /**
     * 自定义Ip地址获取方式
     */
    @JSONField(name = "CustomIpAddressHeader")
    private String customIpAddrHeader;

    /**
     * 配置记录XFF右起第几跳IP(默认为1)
     */
    @JSONField(name = "XffNumTrustedHops")
    @Min(value = 1)
    private int xffNumTrustedHops = 1;

    /**
     * 配置是否记录上一代理的地址(默认false)
     */
    @JSONField(name = "UseRemoteAddress")
    private boolean useRemoteAddress;


    public String getCustomIpAddrHeader() {
        return customIpAddrHeader;
    }

    public void setCustomIpAddrHeader(String customIpAddrHeader) {
        this.customIpAddrHeader = customIpAddrHeader;
    }

    public int getXffNumTrustedHops() {
        return xffNumTrustedHops;
    }

    public void setXffNumTrustedHops(int xffNumTrustedHops) {
        this.xffNumTrustedHops = xffNumTrustedHops;
    }

    public boolean getUseRemoteAddress() {
        return useRemoteAddress;
    }

    public void setUseRemoteAddress(boolean useRemoteAddress) {
        this.useRemoteAddress = useRemoteAddress;
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
}
