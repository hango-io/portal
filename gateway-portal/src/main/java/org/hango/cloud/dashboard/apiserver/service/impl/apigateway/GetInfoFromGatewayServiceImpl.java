package org.hango.cloud.dashboard.apiserver.service.impl.apigateway;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayAddrConfigInfo;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.ResultEntity;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.apigateway.IGetInfoFromGatewayService;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class GetInfoFromGatewayServiceImpl implements IGetInfoFromGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(GetInfoFromGatewayServiceImpl.class);
    private static String gatewayPath = "/ngw";
    @Autowired
    IGatewayInfoService gatewayInfoService;

    @Override
    public Map<String, String> getDataSourceAddr(String gwAddr) {
        Map<String, String> result = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        params.put("Action", "GetDatasourceAddr");
        params.put("Version", "2017-11-16");
        try {
            HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(gwAddr + gatewayPath, params, null, null, Const.GET_METHOD);
            if (httpClientResponse != null && httpClientResponse.getStatusCode() == 200) {
                JSONObject jsonObject = JSONObject.parseObject(httpClientResponse.getResponseBody());
                result.put(Const.AUDIT_DATASOURCE_MONGO, jsonObject.getString("MongoAddr"));
                result.put(Const.AUDIT_DATASOURCE_SWITCH, jsonObject.getString("AuditDatasourceSwitch"));
                result.put(Const.AUDIT_DATASOURCE_MYSQL, jsonObject.getString("MysqlAddr"));
                result.put(Const.METRIC_URL, jsonObject.getString(Const.METRIC_URL));
                return result;
            }
            return null;
        } catch (Exception e) {
            logger.info("调用网关GetMongoAddr，异常,{}", e);
            return null;
        }
    }

    @Override
    public Map<String, String> getAuthEnv(String gwAddr) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "GetAuthAddrAndEnv");
        params.put("Version", "2017-11-16");
        try {
            HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(gwAddr + gatewayPath, params, null, null, Const.GET_METHOD);
            if (httpClientResponse != null && httpClientResponse.getStatusCode() == 200) {
                JSONObject jsonObject = JSONObject.parseObject(httpClientResponse.getResponseBody());
                Map<String, String> result = new HashMap<>();
                result.put("AuthAddr", jsonObject.getString("AuthAddr"));
                result.put("EnvId", jsonObject.getString("EnvId"));
                result.put("GwUniId", jsonObject.getString("GwUniId"));
                return result;
            }
            return null;
        } catch (Exception e) {
            logger.info("调用网关GetMongoAddr，异常,{}", e);
            return null;
        }
    }

    @Override
    public int getHealthFromGateway(String gwAddr, String healthAddr) {
        String healthCheckUrl;
        HttpClientResponse httpClientResponse;

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", HttpClientUtil.DEFAULT_CONTENT_TYPE);
        if (StringUtils.isNotBlank(healthAddr) && healthAddr.startsWith("/")) {
            healthCheckUrl = gwAddr + healthAddr;
        } else {
            healthCheckUrl = gwAddr + "/" + healthAddr;
        }

        //发送真实curl请求
        try {
            httpClientResponse = HttpClientUtil.httpRequest(Const.GET_METHOD, healthCheckUrl, null, headerMap);
            if (httpClientResponse.getStatusCode() < 300) {
                return 1;
            } else {
                logger.warn("探活{}网关时，返回的status code为{}，非200，暂时处理为服务运行不正常", gwAddr);
                return 0;
            }
        } catch (Exception e) {
            logger.warn("获取网关心跳健康异常：e:{}", e);
            return 0;
        }
    }

    @Override
    public ResultEntity<ErrorCode, GatewayAddrConfigInfo> checkAndUpdateFromGateway(long gwId, String gwAddr, String healthAddr) {
        //参数校验
        if (StringUtils.isBlank(gwAddr)) {
            logger.info("验证网关，GwAddr为空");
            return new ResultEntity<>(CommonErrorCode.MissingParameter("GwAddr"), null);
        }
        if (StringUtils.isBlank(healthAddr)) {
            logger.info("验证网关，HealthInterfacePath为空");
            return new ResultEntity<>(CommonErrorCode.MissingParameter("HealthInterfacePath为空"), null);
        }
        if (getHealthFromGateway(gwAddr, healthAddr) != 1) {
            return new ResultEntity<>(CommonErrorCode.HostUnreachable, null);
        } else {
            GatewayAddrConfigInfo gatewayAddrConfigInfo = new GatewayAddrConfigInfo();
            Map<String, String> dataSourceAddr = getDataSourceAddr(gwAddr);
            Map<String, String> authMap = getAuthEnv(gwAddr);
            //认证中心地址以及mongo地址均同步成功
            if (!CollectionUtils.isEmpty(authMap) && !CollectionUtils.isEmpty(dataSourceAddr)) {
                gatewayAddrConfigInfo.setAuditDatasourceSwitch(StringUtils.trimToEmpty(dataSourceAddr.get(Const.AUDIT_DATASOURCE_SWITCH)));
                if (Const.AUDIT_DATASOURCE_MONGO.equals(gatewayAddrConfigInfo.getAuditDatasourceSwitch())) {
                    gatewayAddrConfigInfo.setAuditDbConfig(StringUtils.trimToEmpty(dataSourceAddr.get(Const.AUDIT_DATASOURCE_MONGO)));
                }
                if (Const.AUDIT_DATASOURCE_MYSQL.equals(gatewayAddrConfigInfo.getAuditDatasourceSwitch())) {
                    gatewayAddrConfigInfo.setAuditDbConfig(StringUtils.trimToEmpty(dataSourceAddr.get(Const.AUDIT_DATASOURCE_MYSQL)));
                }
                gatewayAddrConfigInfo.setAuthAddr(StringUtils.trimToEmpty(authMap.get("AuthAddr")));
                gatewayAddrConfigInfo.setEnvId(StringUtils.trimToEmpty(authMap.get("EnvId")));
                gatewayAddrConfigInfo.setGwUniId(StringUtils.trimToEmpty(authMap.get("GwUniId")));
                gatewayAddrConfigInfo.setMetricUrl(StringUtils.trimToEmpty(dataSourceAddr.get(Const.METRIC_URL)));

                //GwUniId校验
                GatewayInfo gwByUniId = gatewayInfoService.getGwByUniId(gatewayAddrConfigInfo.getGwUniId());
                if (gwId == 0 && gwByUniId != null) {
                    return new ResultEntity<>(CommonErrorCode.GwAlreadyExist, null);
                }
                if (gwId != 0 && gwByUniId != null && gwByUniId.getId() != gwId) {
                    return new ResultEntity<>(CommonErrorCode.GwAlreadyExist, null);
                }

                return new ResultEntity<>(CommonErrorCode.Success, gatewayAddrConfigInfo);
            }
            return new ResultEntity<>(CommonErrorCode.SynchronizedConfigError, null);
        }
    }

    @Override
    public boolean checkAuthConfig(String gwAddr) {
        Map<String, String> params = new HashMap<>();
        params.put("Action", "CheckAuthConfig");
        params.put("Version", "2017-11-16");
        try {
            HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(gwAddr + gatewayPath, params, null, null, Const.GET_METHOD);
            if (httpClientResponse.getStatusCode() == 200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.info("调用网关CheckAuthConfig，异常,{}", e);
            return false;
        }
    }

}
