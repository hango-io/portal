package org.hango.cloud.common.advanced.serviceproxy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.advanced.base.config.CommonAdvanceConfig;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.advanced.serviceproxy.dto.MetaServiceDto;
import org.hango.cloud.common.advanced.serviceproxy.service.IMetaService;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.ActionPair;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/5/24
 */
@Service
@RequiredArgsConstructor
public class MetaServiceImpl implements IMetaService, CommandLineRunner {

    private final CommonAdvanceConfig commonAdvanceConfig;

    private static final Logger logger = LoggerFactory.getLogger(MetaServiceImpl.class);

    public static final String BASE_PATH = "/api/open";
    private static final String META_VERSION = "2023-03-23";
    private static final ActionPair GET_SERVICE = ActionPair.builder().action("ListService").version(META_VERSION).build();
    private static final ActionPair CREATE_SERVICE = ActionPair.builder().action("CreateService").version(META_VERSION).build();
    private static final ActionPair DELETE_SERVICE = ActionPair.builder().action("BindService").version(META_VERSION).build();

    private final IServiceProxyService serviceProxyService;

    @Override
    public void run(String... args) {
        try {
            compensate();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void compensate() {
        List<? extends ServiceProxyDto> allService = serviceProxyService.findAll();
        Map<Long, Map<String,List<ServiceProxyDto>>> serviceMap = allService.stream().collect(
                Collectors.groupingBy(ServiceProxyDto::getProjectId,
                        Collectors.groupingBy(ServiceProxyDto::getName)));
        Set<ServiceProxyDto> diffSet = Sets.newHashSet();
        for (Map.Entry<Long, Map<String, List<ServiceProxyDto>>> entry : serviceMap.entrySet()) {
            Set<String> metaNames = listAllMetaService(entry.getKey(), NumberUtils.LONG_ZERO).stream()
                    .map(MetaServiceDto::getName).collect(Collectors.toSet());
            Map<String, List<ServiceProxyDto>> value = entry.getValue();
            Sets.SetView<String> diff = Sets.difference(value.keySet(),metaNames);
            logger.info("需要将如下服务同步到服务目录 {}", JSON.toJSONString(diff));
            //服务目录目前只支持单协议，因此选择该项目下第一个服务对应的协议
           diff.forEach(d->diffSet.add(value.get(d).get(0)));
        }
        diffSet.forEach(this::addMetaService);
    }

    private List<MetaServiceDto> listAllMetaService(long projectId, long offset) {
        int maxSearchCount = 2000;
        PageResult<List<MetaServiceDto>> listPageResult = listMetaService(StringUtils.EMPTY, projectId, offset, maxSearchCount);
        List<MetaServiceDto> result = listPageResult.getResult();
        if (listPageResult.getTotal() > offset) {
            result.addAll(listAllMetaService(projectId, offset + maxSearchCount));
        }
        return CollectionUtils.isEmpty(result) ? Collections.emptyList() : result;
    }

    @Override
    public PageResult<List<MetaServiceDto>> listMetaService(String serviceName, long offset, long limit) {
        return listMetaService(serviceName, ProjectTraceHolder.getProId(), offset, limit);
    }

    public PageResult<List<MetaServiceDto>> listMetaService(String serviceName, long projectId, long offset, long limit) {
        String metaServiceAddr = commonAdvanceConfig.getMetaServiceAddr();
        if (StringUtils.isBlank(metaServiceAddr)) {
            return PageResult.ofEmpty();
        }
        Map<String, Object> param = HttpClientUtil.defaultQuery(GET_SERVICE);
        param.put("Offset", offset);
        param.put("Limit", limit);
        param.put("ProjectId", projectId);
        if (StringUtils.isNotBlank(serviceName)) {
            param.put("ServiceName", serviceName);
        }
        HttpClientResponse response = HttpClientUtil.getRequest(metaServiceAddr + BASE_PATH,
                param, defaultHeader(), AdvancedConst.MODULE_META_SERVICE);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            logger.error("listMetaService error, response:{}", response.getResponseBody());
            PageResult pageResult = PageResult.ofEmpty();
            pageResult.setErrorCode(CommonErrorCode.INTERNAL_SERVER_ERROR);
            return pageResult;
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        Integer total = jsonObject.getInteger(BaseConst.TOTAL);
        if (NumberUtils.INTEGER_ZERO.equals(total)) {
            return PageResult.ofEmpty();
        }
        List<MetaServiceDto> data = jsonObject.getObject(BaseConst.RESULT, new TypeReference<List<MetaServiceDto>>() {
        });
        data.forEach(m -> m.setServiceType(MetaServiceType.protocolOf(m.getServiceType())));
        return new PageResult<>(data, total);
    }

    @Override
    public ErrorCode addMetaService(ServiceProxyDto serviceProxyDto) {
        String metaServiceAddr = commonAdvanceConfig.getMetaServiceAddr();
        if (StringUtils.isBlank(metaServiceAddr)) {
            return CommonErrorCode.SUCCESS;
        }
        Map<String, Object> param = HttpClientUtil.defaultQuery(CREATE_SERVICE);
        HttpClientResponse response = HttpClientUtil.postRequest(metaServiceAddr + BASE_PATH,
                JSON.toJSONString(trans(serviceProxyDto)), param, defaultHeader(), AdvancedConst.MODULE_META_SERVICE);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            logger.error("addMetaService error, response:{}", response.getResponseBody());
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode deleteMetaService(ServiceProxyDto serviceProxyDto) {
        String metaServiceAddr = commonAdvanceConfig.getMetaServiceAddr();
        if (StringUtils.isBlank(metaServiceAddr)) {
            return CommonErrorCode.SUCCESS;
        }
        List<ServiceProxyDto> serviceProxy = serviceProxyService.getServiceProxy(ServiceProxyQuery.builder()
                .pattern(serviceProxyDto.getName()).projectId(NumberUtils.LONG_ZERO).build());
        if (serviceProxy.size() > NumberUtils.INTEGER_ONE) {
            return CommonErrorCode.SUCCESS;
        }
        Map<String, Object> param = HttpClientUtil.defaultQuery(DELETE_SERVICE);
        param.put("ServiceName", serviceProxyDto.getName());
        param.put("ProjectId", serviceProxyDto.getProjectId());
        param.put("OperateType", "UNBIND");
        HttpClientResponse response = HttpClientUtil.postRequest(metaServiceAddr + BASE_PATH,
                param, defaultHeader(), AdvancedConst.MODULE_META_SERVICE);
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            logger.error("deleteMetaService error, response:{}", response.getResponseBody());
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 服务类型枚举
     */

    @Getter
    @AllArgsConstructor
    @SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
    enum MetaServiceType {
        HTTP("1","http"),
        DUBBO("2","dubbo"),
        GRPC("4","grpc"),
        WEBSERVICE("8","webservice"),
        UNSUPPORTED("","UnSupported");
        /**
         * 服务协议类型code
         */
        private final String code;

        private final String protocol;

        public static String protocolOf(String code) {
            for (MetaServiceType type : MetaServiceType.values()) {
                if (type.getCode().equals(code)) {
                    return type.protocol;
                }
            }
            return UNSUPPORTED.protocol;
        }

        public static String codeOf(String protocol) {
            for (MetaServiceType type : MetaServiceType.values()) {
                if (type.protocol.equals(protocol)) {
                    return type.code;
                }
            }
            return StringUtils.EMPTY;
        }
    }

    /**
     * 数据转换
     **/
    public MetaServiceDto trans(ServiceProxyDto serviceProxy) {
        if (serviceProxy == null) {
            return null;
        }
        MetaServiceDto metaServiceDto = new MetaServiceDto();
        metaServiceDto.setName(serviceProxy.getName());
        metaServiceDto.setProjectId(String.valueOf(serviceProxy.getProjectId()));
        metaServiceDto.setServiceType(MetaServiceType.codeOf(serviceProxy.getProtocol()));
        return metaServiceDto;
    }

    public HttpHeaders defaultHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-nsf-from", "API_GW");
        return headers;
    }


}
