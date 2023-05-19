package org.hango.cloud.envoy.infra.virtualgateway.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayDTO;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayHttpRouteDTO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/12/6
 */
public interface IKubernetesGatewayService {

    /**
     * @Description: 刷新gateway api/ingress 配置
     * @param
     * @return
     * @author xianyanglin
     * @date 2022/12/24 20:56
     */
    ErrorCode refreshK8sGateway();


    /**
     * @Description: 查询kubernetesGateway
     * @param virtualGatewayId 虚拟网关ID
     * @return 前端展示的Gateway结构体
     * @author xianyanglin
     * @date 2022/12/24 20:52
     */
    List<KubernetesGatewayDTO> getKubernetesGatewayList(Long virtualGatewayId);

    /**
     * @Description: 查询gateway.yaml配置
     * @param virtualGatewayId 虚拟网关ID
     * @return Yaml格式的字符串
     * @author xianyanglin
     * @date 2022/12/24 20:58
     */
    String getKubernetesGatewayYaml(Long virtualGatewayId);
    /**
     * @Description: 根据kubernetesGateway查询HTTPRoute
     * @param virtualGatewayId 虚拟网关ID
     * @return 前端展示的HttpRoute结构体
     * @author xianyanglin
     * @date 2022/12/24 20:58
     */
    List<KubernetesGatewayHttpRouteDTO> getKubernetesGatewayHTTPRouteList(Long virtualGatewayId);
}
