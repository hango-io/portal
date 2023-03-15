package org.hango.cloud.envoy.infra.virtualgateway.dto;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPBackendRef;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteFilter;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteMatch;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteRule;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description  HTTPRoute转DTO类，供前端展示
 * @Author xianyanglin
 * @Date 2022/12/14
 */
@Data
public class KubernetesGatewayHttpRouteDTO {

    private List<String> routeHosts = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private String routeName;

    private String yamlStr;


    @Data
    public static class Rule {
        private List<BackendRef> backendRefs = new ArrayList<>();
        private List<Filter> filters = new ArrayList<>();
        private List<Match> matches = new ArrayList<>();
    }

    @Data
    public static class Match {
        private String type;
        private String value;
    }

    @Data
    public static class BackendRef {
        private String name;
        private int port;
        private int weight;
    }

    @Data
    public static class Filter {
        private String type;
    }

}
