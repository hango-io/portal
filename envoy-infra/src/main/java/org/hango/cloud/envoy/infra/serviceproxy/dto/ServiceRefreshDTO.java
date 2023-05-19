package org.hango.cloud.envoy.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/31
 */
@Getter
@Setter
public class ServiceRefreshDTO implements Serializable {
    private static final long serialVersionUID = -4764497053779766388L;

    /**
     * 服务id
     */
    @NotEmpty
    @JSONField(name = "ServiceIds")
    List<Long> serviceIds;

    /**
     * 需要新增的域名
     */
    @JSONField(name = "AddHosts")
    List<String> addHosts;


    /**
     * 需要删除的域名
     */
    @JSONField(name = "DeleteHosts")
    List<String> deleteHosts;
}
