package org.hango.cloud.envoy.infra.pluginmanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginOrderItemDto {

    private Boolean enable;

    private String name;

    private String subName;

    private Integer port;

    private Object inline;

    private RiderDTO rider;

    private RiderDTO wasm;

}
