package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/31
 */
@Getter
@Setter
public class IngressViewDTO implements Serializable {
    private static final long serialVersionUID = -5463145440972157934L;
    /**
     * 名称
     */
    @JSONField(name = "Name")
    private String name;

    /**
     * ingress路由规则
     */
    @JSONField(name = "Rule")
    private List<IngressRuleViewDTO> rules;

    /**
     * ingress yaml配置
     */
    @JSONField(name = "Content")
    private String content;
}
