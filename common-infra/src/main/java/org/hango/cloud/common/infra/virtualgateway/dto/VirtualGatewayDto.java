package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.RegexConst.*;

/**
 * 与前端交互的网关dto
 */
@JsonIgnoreProperties({"ConfAddr"})
@Getter
@Setter
public class VirtualGatewayDto extends CommonExtensionDto implements Serializable {

    private static final Long serialVersionUID = -289652590295163660L;

    /**
     * 虚拟网关ID
     */
    @JSONField(name = "VirtualGwId")
    private long id;


    /**
     * 网关ID
     */
    @NotNull
    @JSONField(name = "GwId")
    private long gwId;


    /**
     * 虚拟网关名称
     */
    @NotBlank
    @Pattern(regexp = REGEX_VIRTUAL_GATEWAY_NAME)
    @JSONField(name = "Name")
    private String name;

    /**
     * 网关名称
     */
    @JSONField(name = "GwName")
    private String gwName;


    /**
     * 虚拟网关标识
     */
    @NotBlank
    @Pattern(regexp = REGEX_NAME)
    @JSONField(name = "Code")
    private String code;


    /**
     * 虚拟网关访问地址
     */
    @Pattern(regexp = REGEX_DESCRIPTION)
    @JSONField(name = "Addr")
    private String addr;

    /**
     * 虚拟网关监听地址，实时获取envoy proxy address
     */
    @JSONField(name = "ListenerAddr")
    private List<String> ListenerAddr;


    /**
     * 网关所属项目id
     */
    @JSONField(name = "ProjectIds")
    private List<Long> projectIdList = new ArrayList<>();


    /**
     * 虚拟网关描述
     */

    @Pattern(regexp = REGEX_DESCRIPTION)
    @JSONField(name = "Description")
    private String description;


    /**
     * 所属环境
     */
    @JSONField(name = "EnvId")
    private String envId;


    /**
     * 虚拟网关类型
     */
    @NotBlank
    @JSONField(name = "Type")
    private String type;


    /**
     * 监听协议类型
     */
    @NotBlank
    @Pattern(regexp = BaseConst.VIRTUAL_GATEWAY_PROTOCOL_SCHEME_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE)
    @JSONField(name = "Protocol")
    private String protocol;


    /**
     * 监听端口
     */
    @NotNull
    @JSONField(name = "Port")
    @Range(min = 80, max = 10000)
    private int port;

    /**
     * 配置下发地址
     */
    @JSONField(name = "ConfAddr",serialize = false)
    private String confAddr;

    /**
     * 网关集群名称
     */
    @JSONField(name = "GwClusterName")
    private String gwClusterName;

    /**
     * 网关类型
     */
    @JSONField(name = "GwType")
    private String gwType;


    /**
     * 创建时间
     */

    @JSONField(name = "CreateTime")
    private long createTime;


    /**
     * 修改时间
     */

    @JSONField(name = "ModifyTime")
    private long modifyTime;


    /**
     * 域名列表
     */
    @JSONField(name = "DomainInfos")
    private List<DomainInfoDTO> domainInfos;

    /**
     * 该虚拟网关下已发布服务的数量
     */
    @JSONField(name = "PublishServiceCount")
    private Long publishServiceCount;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
