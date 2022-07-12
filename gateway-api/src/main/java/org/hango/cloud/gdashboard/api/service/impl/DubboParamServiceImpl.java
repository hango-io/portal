package org.hango.cloud.gdashboard.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.dao.DubboParamDao;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodeBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodesDto;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.DubboParamInfo;
import org.hango.cloud.gdashboard.api.meta.DubboType;
import org.hango.cloud.gdashboard.api.meta.ServiceType;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IDubboParamService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/11/27
 */
@Service
public class DubboParamServiceImpl implements IDubboParamService {

    public static final Logger LOGGER = LoggerFactory.getLogger(DubboParamServiceImpl.class);

    @Autowired
    private DubboParamDao dubboParamDao;

    @Autowired
    private IApiInfoService apiInfoService;

    @Autowired
    private IApiBodyService apiBodyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<DubboParamInfo> dubboParamInfos) {
        if (CollectionUtils.isEmpty(dubboParamInfos)) {
            return;
        }
        delete(dubboParamInfos.get(0).getApiId());
        dubboParamDao.batchAdd(dubboParamInfos);
        Map<String, List<DubboParamInfo>> map = aggregateByDubboType(dubboParamInfos, null);
        if (map.containsKey(DubboType.DubboParam.name())) {
            List<DubboParamInfo> paramInfoList = map.get(DubboType.DubboParam.name());
            apiBodyService.addBody(DubboParamInfo.castToApiBody(paramInfoList));
        }
        if (map.containsKey(DubboType.DubboResponse.name())) {
            List<DubboParamInfo> paramInfoList = map.get(DubboType.DubboResponse.name());
            apiBodyService.addBody(DubboParamInfo.castToApiBody(paramInfoList));
        }
        //生成默认statusCode
        //创建默认dubbo statusCode
        ApiStatusCodesDto apiStatusCodesDto = new ApiStatusCodesDto();
        apiStatusCodesDto.setId(dubboParamInfos.get(0).getApiId());
        List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtos = new ArrayList<>();

        ApiStatusCodeBasicDto apiStatusCodeBasicDto = new ApiStatusCodeBasicDto();
        apiStatusCodeBasicDto.setStatusCode(200);
        apiStatusCodeBasicDto.setDescription("路由成功");
        apiStatusCodeBasicDtos.add(apiStatusCodeBasicDto);

        ApiStatusCodeBasicDto apiStatusCodeBasicDto1 = new ApiStatusCodeBasicDto();
        apiStatusCodeBasicDto1.setStatusCode(500);
        apiStatusCodeBasicDto1.setDescription("路由失败");
        apiStatusCodeBasicDtos.add(apiStatusCodeBasicDto1);

        apiStatusCodesDto.setApiStatusCodeBasicDtoList(apiStatusCodeBasicDtos);
        List<ApiStatusCode> apiStatusCodes = apiBodyService.generateApiStatusCodeFromCodeList(apiStatusCodesDto);
        apiBodyService.addStatusCodes(apiStatusCodes, apiStatusCodesDto.getId(), Const.API);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long apiId) {
        dubboParamDao.deleteDubboParam(apiId);
        apiBodyService.deleteBody(apiId, Const.REQUEST_PARAM_TYPE);
        apiBodyService.deleteBody(apiId, Const.RESPONSE_PARAM_TYPE);
    }

    @Override
    public List<DubboParamInfo> getDubboParamByApiId(Long apiId) {
        return dubboParamDao.getDubboInfo(apiId);
    }

    @Override
    public Map<String, List<DubboParamInfo>> getDubboByApiIdAsMap(Long apiId) {
        return aggregateByDubboType(getDubboParamByApiId(apiId), apiId);
    }

    @Override
    public ApiErrorCode checkAndCompleteParam(List<DubboParamInfo> dubboParamInfos, Long apiId, ServiceType serviceType) {
        if (CollectionUtils.isEmpty(dubboParamInfos)) {
            return CommonApiErrorCode.ParameterNull;
        }
        ApiInfo api = apiInfoService.getApiById(apiId);
        if (api == null) {
            return CommonApiErrorCode.NoSuchApiInterface;
        }
        //FIXME 因为要做拆分，这里判断能否删除，和宝军确认，减少依赖
//        ServiceInfo serviceById = serviceInfoService.getServiceByServiceId(api.getServiceId());
//        if (serviceById == null){
//            return CommonApiErrorCode.NoSuchService;
//        }
        if (!ServiceType.dubbo.name().equals(serviceType.name())) {
            return CommonApiErrorCode.DubboServiceParamLimit;
        }
        Map<String, List<DubboParamInfo>> byDubboType = aggregateByDubboType(dubboParamInfos, apiId);

        for (String s : byDubboType.keySet()) {
            if (StringUtils.isBlank(DubboType.getDubboType(s))) {
                return CommonApiErrorCode.InvalidParameterValue(s, "DubboType");
            }
        }
        if (!byDubboType.containsKey(DubboType.DubboInterface.name())) {
            return CommonApiErrorCode.MissingParameter("DubboInterface");
        }
        if (!byDubboType.containsKey(DubboType.DubboMethod.name())) {
            return CommonApiErrorCode.MissingParameter("DubboMethod");
        }
        if (byDubboType.containsKey(byDubboType.get(DubboType.DubboParam.name()))) {
            List<DubboParamInfo> paramList = byDubboType.get(DubboType.DubboParam.name());
            for (DubboParamInfo dubboParamInfo : paramList) {
                if (StringUtils.isBlank(dubboParamInfo.getParamName())) {
                    return CommonApiErrorCode.MissingParameter("ParamName");
                }
                if (StringUtils.isBlank(dubboParamInfo.getParamAlias())) {
                    return CommonApiErrorCode.MissingParameter("ParamAlias");
                }
                if (dubboParamInfo.getParamSort() == null) {
                    return CommonApiErrorCode.MissingParameter("ParamSort");
                }
            }
        }
        return CommonApiErrorCode.Success;
    }

    private Map<String, List<DubboParamInfo>> aggregateByDubboType(List<DubboParamInfo> infoList, Long apiId) {
        Map<String, List<DubboParamInfo>> dubboMap = new HashMap<>();
        if (CollectionUtils.isEmpty(infoList)) {
            return dubboMap;
        }
        for (DubboParamInfo dubboParamInfo : infoList) {
            if (dubboParamInfo.getApiId() == null) {
                dubboParamInfo.setApiId(apiId);
                dubboParamInfo.setCreateDate(System.currentTimeMillis());
            }
            if (dubboMap.containsKey(dubboParamInfo.getDubboType())) {
                dubboMap.get(dubboParamInfo.getDubboType()).add(dubboParamInfo);
            } else {
                List<DubboParamInfo> childList = new ArrayList<>();
                childList.add(dubboParamInfo);
                dubboMap.put(dubboParamInfo.getDubboType(), childList);
            }
        }
        return dubboMap;
    }
}
