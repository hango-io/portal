package org.hango.cloud.dashboard.apiserver.meta.audit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.BasePageInfo;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/3/30
 */
public class AuditQuery extends BasePageInfo {

    private String serviceTag;

    private String requestId;

    private long startTime;

    private long endTime;

    @JSONField(serialize = false)
    private boolean forceCount;

    private long apiId;

    private String action;

    private String version;

    private String errorType;

    private String respCode;

    private String account;

    private int duration;

    private int minDuration;

    private int maxDuration;

    private String userIp;


    /**
     * 统计趋势图显示间隔
     */
    private int step;

    /**
     * 后端转发实例
     */
    private String upstreamHost;

    /**
     * 响应标识
     */
    private String responseFlag;


    private String scrollId;

    private String uri;

    /**
     * 是否区分项目
     */
    private boolean projectDivided;

    private String originPath;

    private String originHost;

    private String hostName;

    private String podName;

    public AuditQuery() {
    }

    public AuditQuery(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AuditQuery(String serviceTag, long startTime, long endTime) {
        this.serviceTag = serviceTag;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AuditQuery(String serviceTag, long startTime, long endTime, String action, String version) {
        this.serviceTag = serviceTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.action = action;
        this.version = version;
    }

    public AuditQuery(String serviceTag, long startTime, long endTime, int limit, int offset, String requestId, long apiId) {
        this.serviceTag = serviceTag;
        this.requestId = requestId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.apiId = apiId;
        this.limit = limit;
        this.offset = offset;
    }

    public AuditQuery(String serviceTag, long startTime, long endTime, int limit, int offset, String requestId, String action, String version, int duration) {
        this.serviceTag = serviceTag;
        this.requestId = requestId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.action = action;
        this.version = version;
        this.limit = limit;
        this.offset = offset;
        this.duration = duration;
    }

    public AuditQuery(String serviceTag, long startTime, long endTime, String errorType, int limit, int offset) {
        this.serviceTag = serviceTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.errorType = errorType;
        this.limit = limit;
        this.offset = offset;
    }

    public AuditQuery(String serviceTag, String account, long startTime, long endTime, boolean forceCount, long apiId) {
        this.serviceTag = serviceTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.forceCount = forceCount;
        this.apiId = apiId;
        this.account = account;
    }

    public AuditQuery(String serviceTag, long startTime, long endTime, boolean forceCount, long apiId) {
        this.serviceTag = serviceTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.forceCount = forceCount;
        this.apiId = apiId;
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    public String getOriginHost() {
        return originHost;
    }

    public void setOriginHost(String originHost) {
        this.originHost = originHost;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isForceCount() {
        return forceCount;
    }

    public void setForceCount(boolean forceCount) {
        this.forceCount = forceCount;
    }

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUpstreamHost() {
        return upstreamHost;
    }

    public void setUpstreamHost(String upstreamHost) {
        this.upstreamHost = upstreamHost;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getResponseFlag() {
        return responseFlag;
    }

    public void setResponseFlag(String responseFlag) {
        this.responseFlag = responseFlag;
    }


    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }


    public boolean getProjectDivided() {
        return projectDivided;
    }

    public void setProjectDivided(boolean projectDivided) {
        this.projectDivided = projectDivided;
    }

    public Map<String, Object> toMap(Map<String, String> propertyMap, String... ignoreProperty) {
        Map<String, Object> map = toMap(ignoreProperty);
        if (!CollectionUtils.isEmpty(propertyMap)) {
            //由于某些表查询字段与该bean字段不一但意义相同，因此需要转换查询key
            Iterator<Map.Entry<String, String>> iterator = propertyMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                if (map.containsKey(next.getKey())) {
                    map.put(next.getValue(), map.get(next.getKey()));
                    map.remove(next.getKey());
                }
            }
        }
        return map;
    }


    public Map<String, Object> toMap(String... ignoreProperty) {
        serviceTag = StringUtils.EMPTY.equals(serviceTag) ? null : serviceTag;
        requestId = StringUtils.EMPTY.equals(requestId) ? null : requestId;
        action = StringUtils.EMPTY.equals(action) ? null : action;
        version = StringUtils.EMPTY.equals(version) ? null : version;
        errorType = StringUtils.EMPTY.equals(errorType) ? null : errorType;
        account = StringUtils.EMPTY.equals(account) ? null : account;
        upstreamHost = StringUtils.EMPTY.equals(upstreamHost) ? null : account;

        String jsonString = JSON.toJSONString(this, SerializerFeature.NotWriteDefaultValue);
        Map<String, Object> result = JSON.parseObject(jsonString, Map.class);
        if (!ArrayUtils.isEmpty(ignoreProperty)) {
            for (String key : ignoreProperty) {
                result.remove(key);
            }
        }
        return result;
    }

    public CallStatisticsInfo convertStatisticsInfo() {
        CallStatisticsInfo callStatisticsInfo = new CallStatisticsInfo();

        callStatisticsInfo.setServiceTag(getServiceTag());
        callStatisticsInfo.setStartTime(getStartTime());
        callStatisticsInfo.setEndTime(getEndTime());
        callStatisticsInfo.setApiId(getApiId());
        callStatisticsInfo.setTime(System.currentTimeMillis());

        return callStatisticsInfo;
    }

}
