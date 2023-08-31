package org.hango.cloud.envoy.advanced.manager.controller;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.envoy.advanced.manager.service.ICleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author zhufengwei
 * @Date 2023/8/30
 */
@Slf4j
@RestController
@RequestMapping(value = ApiConst.HANGO_CLEANUP)
public class ResourceCleanupController extends AbstractController {
    @Autowired
    ICleanupService cleanupService;

    @GetMapping(params = {"Action=Gateway"})
    public String clearup(@RequestParam(value = "name") String name) {
        log.info("clearup start, name: {}", name);
        ErrorCode checkResult = cleanupService.checkCleanupParam(name);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        cleanupService.cleanup(name);
        return apiReturn(new Result(null));
    }
}
