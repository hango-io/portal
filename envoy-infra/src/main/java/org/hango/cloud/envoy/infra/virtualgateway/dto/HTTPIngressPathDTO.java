package org.hango.cloud.envoy.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author zhufengwei
 * @Date 2023/5/25
 */
@Getter
@Setter
public class HTTPIngressPathDTO implements Serializable {
    private static final long serialVersionUID = 6733875048689349756L;
    /**
     * path
     */
    @JSONField(name = "Path")
    private String path;

    /**
     * 名称
     */
    @JSONField(name = "PathType")
    private String pathType;

    /**
     * 名称
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * 名称
     */
    @JSONField(name = "ServicePort")
    private Integer servicePort;

}
