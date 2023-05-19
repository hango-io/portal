package org.hango.cloud.envoy.infra.virtualgateway.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.virtualgateway.dto.GatewaySettingDTO;
import org.hango.cloud.envoy.infra.virtualgateway.service.IEnvoyVgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author zhufengwei
 * @Date 2023/5/11
 */
@Slf4j
@RestController
@RequestMapping(value = ApiConst.HANGO_VIRTUAL_GATEWAY_V1_PREFIX)
public class EnvoyVirtualGatewayController extends AbstractController {

    @Autowired
    IEnvoyVgService envoyVgService;

    /**
     * 更新虚网关高级配置
     */
    @PostMapping(params = {"Action=UpdateVirtualGatewaySetting"})
    public Object updateGatewaySetting(@RequestBody @Validated GatewaySettingDTO setting) {
        logger.info("更新网关高级配置, setting = {}", JSON.toJSONString(setting));
        ErrorCode errorCode = envoyVgService.updateEnvoyGatewaySetting(setting);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        return apiReturn(new Result(errorCode));
    }

    /**
     * 获取网关高级配置
     *
     * @return
     */
    @GetMapping(params = {"Action=GetVirtualGatewaySetting"})
    public Object getGatewaySetting(@RequestParam(name = "VirtualGwId") Long virtualGwId) {
        GatewaySettingDTO gatewaySetting = envoyVgService.getEnvoyGatewaySetting(virtualGwId);
        return apiReturnSuccess(gatewaySetting);
    }
}
