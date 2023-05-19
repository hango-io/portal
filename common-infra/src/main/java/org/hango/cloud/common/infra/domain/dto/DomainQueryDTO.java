package org.hango.cloud.common.infra.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.io.Serializable;

/**
 * @Author zhufengwei
 * @Date 2023/3/30
 */
@Getter
@Setter
public class DomainQueryDTO extends PageQuery implements Serializable {

    private static final long serialVersionUID = -8200778140249138305L;

    /**
     * 虚拟网关id
     */
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    /**
     * 域名
     */
    @JSONField(name = "Pattern")
    private String pattern;

    /**
     * 项目id
     */
    @JSONField(name = "ProjectId")
    private Long projectId;

    /**
     * 协议
     */
    @JSONField(name = "Protocol")
    private String protocol;
}
