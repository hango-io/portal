package org.hango.cloud.envoy.infra.base.util;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

@Slf4j
public class YamlUtil<T> {
    public String obj2yaml(T object) {
        try {
            return new YAMLMapper().writeValueAsString(object);
        } catch (Exception e) {
            log.error("obj2yaml error", e);
        }
        return Strings.EMPTY;
    }
}
