package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/5/25
 */
@Getter
@Setter
public class IngressRuleDTO implements Serializable {
    private static final long serialVersionUID = -5921613461450865607L;
    /**
     * 域名
     */
    @JSONField(name = "Host")
    private String host;


    /**
     * 域名
     */
    @JSONField(name = "HTTPRules")
    private List<HTTPIngressPathDTO> httpRuleValueDTOS;

}
