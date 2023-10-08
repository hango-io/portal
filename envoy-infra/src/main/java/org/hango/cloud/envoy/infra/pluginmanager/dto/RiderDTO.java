package org.hango.cloud.envoy.infra.pluginmanager.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author zhufengwei
 * @Date 2023/9/22
 */
@Getter
@Setter
public class RiderDTO {
    private String pluginName;

    private String imagePullSecretName;

    private String url;

    private Object settings;
}
