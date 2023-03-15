package org.hango.cloud.common.infra.serviceregistry.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.serviceregistry.service.IRegistryCenterService;
import org.hango.cloud.common.infra.service.meta.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/1/14
 */
@RestController
@RequestMapping(value ={ BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class RegistryCenterController extends AbstractController {
    @Autowired
    private IRegistryCenterService registryCenterService;

    @RequestMapping(params = {"Action=DescribeRegistryTypes"}, method = RequestMethod.GET)
    public String describeRegistryTypes(@RequestParam(value = "ServiceType") String serviceType, @RequestParam(value = "VirtualGwId") long virtualGwId) {
        if (ServiceType.getServiceTypeByName(serviceType) == null) {
            return apiReturn(CommonErrorCode.SERVICE_TYPE_INVALID);
        }

        Map<String, Object> result = Maps.newHashMap();
        List<String> registryTypes = registryCenterService.getRegistryByServiceType(virtualGwId, serviceType);
        result.put("RegistryTypes", registryTypes);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

}
