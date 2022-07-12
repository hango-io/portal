package org.hango.cloud.dashboard.apiserver.web.controller.health;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供给K8S master dashboard使用，用于检查自身服务的健康
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/3/22 11:03.
 */
@RestController
public class HealthCheckController extends AbstractController {

    /**
     * 探活接口
     */
    @RequestMapping(method = RequestMethod.GET, path = "/healthcheck")
    public Object return200() {
        return apiReturn(CommonErrorCode.Success);
    }

}
