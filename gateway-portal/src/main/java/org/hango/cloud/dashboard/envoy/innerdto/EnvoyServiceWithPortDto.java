package org.hango.cloud.dashboard.envoy.innerdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.util.Const;

import java.util.List;
import java.util.Map;

/**
 * 获取带有port的服务信息
 */
public class EnvoyServiceWithPortDto {

    @JSONField(name = "Name")
    private String name;
    @JSONField(name = "Port")
    private List<Integer> ports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }


    public String getName(String registryCenterType, Map<String, String> serviceFilters) {
        if (RegistryCenterEnum.Zookeeper.getType().equals(registryCenterType)) {
            return StringUtils.removeEnd(name, Const.DUBBO_SERVICE_SUFFIX);
        }
        // 若开启eureka服务项目隔离，需要去除后缀(例如".nsf.projectCode")
        String projectCode = serviceFilters.get(Const.PREFIX_LABEL + Const.PROJECT_CODE);
        if (StringUtils.isNotEmpty(projectCode) && RegistryCenterEnum.Eureka.getType().equals(registryCenterType)) {
            return StringUtils.removeEnd(name, Const.EUREKA_NSF_TAG + projectCode);
        }
        return name;
    }
}
