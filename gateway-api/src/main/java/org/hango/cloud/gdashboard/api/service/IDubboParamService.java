package org.hango.cloud.gdashboard.api.service;


import org.hango.cloud.gdashboard.api.meta.DubboParamInfo;
import org.hango.cloud.gdashboard.api.meta.ServiceType;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/11/27
 */
public interface IDubboParamService {

    void batchAdd(List<DubboParamInfo> dubboParamInfos);


    void delete(Long apiId);


    List<DubboParamInfo> getDubboParamByApiId(Long apiId);


    Map<String, List<DubboParamInfo>> getDubboByApiIdAsMap(Long apiId);

    ApiErrorCode checkAndCompleteParam(List<DubboParamInfo> dubboParamInfos, Long apiId, ServiceType serviceType);
}
