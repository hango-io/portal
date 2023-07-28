package org.hango.cloud.common.advanced.serviceproxy.controller;

import lombok.RequiredArgsConstructor;
import org.hango.cloud.common.advanced.serviceproxy.service.IMetaService;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/5/24
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/meta/service")
public class MetaServiceController extends AbstractController {

    private final IMetaService metaService;

    /**
     * 获取服务元数据列表
     */
    @GetMapping(params = "Action=ListService")
    public String getMetaServiceList(@RequestParam(name = "ServiceName", required = false) String serviceName,
                                     @RequestParam(name = "Offset", defaultValue = "0") long offset,
                                     @RequestParam(name = "Limit", defaultValue = "20") long limit) {
        return apiReturn(metaService.listMetaService(serviceName, offset, limit));
    }

}
