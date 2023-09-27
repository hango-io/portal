package org.hango.cloud.envoy.advanced.manager.controller;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.envoy.advanced.manager.dto.RepublishResult;
import org.hango.cloud.envoy.advanced.manager.service.IRepublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/4/18
 */
@Slf4j
@RestController
@RequestMapping(value = ApiConst.HANGO_REPUBLISH)
public class RepublishController extends AbstractController {

    @Autowired
    IRepublishService republishService;

    @GetMapping(params = {"Action=All"})
    public String republish(@RequestParam(value = "VgIds") List<Long> vgIds) {
        log.info("republish start");
        ErrorCode checkResult = republishService.checkRepublishParam(vgIds);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        List<RepublishResult> resultDTO = republishService.republish(vgIds);
        return apiReturn(new Result(resultDTO));
    }


    @GetMapping(params = {"Action=ResortPluginManager"})
    public String resort(@RequestParam(value = "GwCluster") String gwCluster) {
        log.info("resort start gwcluster:{}", gwCluster);
        ErrorCode errorCode = republishService.resortPluginManager(gwCluster);
        return apiReturn(errorCode);
    }
}
