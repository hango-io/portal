package org.hango.cloud.common.infra.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/31
 */
@Getter
@Setter
public class DomainBindDTO implements Serializable {
    private static final long serialVersionUID = 8603626818199567320L;

    /**
     * 虚拟网关ID
     */
    @NotNull
    @JSONField(name = "VirtualGwId")
    private Long virtualGwId;

    /**
     * 新增域名列表
     */
    @NotEmpty
    @JSONField(name = "DomainIds")
    private List<Long> domainIds;

}
