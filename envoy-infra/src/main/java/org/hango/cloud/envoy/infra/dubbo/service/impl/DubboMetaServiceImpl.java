package org.hango.cloud.envoy.infra.dubbo.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.cache.ICacheService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.config.EnvoyConfig;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.util.EnvoyCommonUtil;
import org.hango.cloud.envoy.infra.dubbo.dao.IDubboMetaDao;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboMeta;
import org.hango.cloud.envoy.infra.dubbo.remote.DubboMetaRemoteClient;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
@Service
public class DubboMetaServiceImpl implements IDubboMetaService {

    private static final Logger logger = LoggerFactory.getLogger(DubboMetaServiceImpl.class);

    @Autowired
    private IDubboMetaDao dubboMetaDao;

    @Autowired
    private EnvoyConfig envoyConfig;

    @Autowired
    private DubboMetaRemoteClient dubboMetaRemoteClient;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private ICacheService cacheService;

    @Override
    public long create(DubboMetaDto dubboMetaDto) {
        DubboMeta info = toMeta(dubboMetaDto);
        info.setCreateTime(System.currentTimeMillis());
        return dubboMetaDao.add(info);
    }

    @Override
    public long update(DubboMetaDto dubboMetaDto) {
        DubboMeta info = toMeta(dubboMetaDto);
        return dubboMetaDao.update(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DubboMetaDto dubboMetaDto) {
        dubboMetaDao.delete(toMeta(dubboMetaDto));
    }

    @Override
    public List<DubboMetaDto> findAll() {
        List<DubboMeta> dubboMetaList = dubboMetaDao.findAll();
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        Map<String, Object> params = Maps.newHashMap();
        return dubboMetaDao.getCountByFields(params);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchCreateDubboMeta(List<DubboMetaDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DubboMetaDto dubboMetaDto : list) {
            create(dubboMetaDto);
        }
    }

    @Override
    public List<DubboMetaDto> findAll(long offset, long limit) {
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(Maps.newHashMap(), offset, limit);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public DubboMetaDto get(long id) {
        return toView(dubboMetaDao.get(id));
    }

    @Override
    public List<DubboMetaDto> findByInterfaceNameAndApplicationName(long virtualGwId, String interfaceName, String applicationName, long offset, long limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("interfaceName", interfaceName);
        params.put("applicationName", applicationName);
        params.put("virtualGwId", virtualGwId);

        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params, offset, limit);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DubboMetaDto> findByInterfaceNameAndApplicationName(long virtualGwId, String interfaceName, String applicationName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("interfaceName", interfaceName);
        params.put("applicationName", applicationName);
        params.put("virtualGwId", virtualGwId);

        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params);
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public int countByInterfaceNameAndApplicationName(long virtualGwId, String interfaceName, String applicationName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("interfaceName", interfaceName);
        params.put("applicationName", applicationName);
        params.put("virtualGwId", virtualGwId);

        return dubboMetaDao.getCountByFields(params);
    }


    @Override
    public List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String applicationName, String group, String version, String method, long offset, long limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("virtualGwId", virtualGwId);
        if (StringUtils.isNotBlank(interfaceName)) {
            params.put("interfaceName", interfaceName);
            params.put("dubboGroup", group);
            params.put("dubboVersion", version);
        }
        if (StringUtils.isNotBlank(applicationName)) {
            params.put("applicationName", applicationName);
        }
        if (StringUtils.isNotBlank(method)) {
            params.put("method", method);
        }
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params, offset, limit);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String applicationName, String group, String version, String method) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("virtualGwId", virtualGwId);
        if (StringUtils.isNotBlank(interfaceName)) {
            params.put("interfaceName", interfaceName);
            params.put("dubboGroup", group);
            params.put("dubboVersion", version);
        }
        if (StringUtils.isNotBlank(applicationName)) {
            params.put("applicationName", applicationName);
        }
        if (StringUtils.isNotBlank(method)) {
            params.put("method", method);
        }
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params);
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public int countByCondition(long virtualGwId, String interfaceName, String applicationName, String group, String version, String method) {
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtils.isNotBlank(group)) {
            params.put("dubboGroup", group);
        }
        if (StringUtils.isNotBlank(interfaceName)) {
            params.put("interfaceName", interfaceName);
        }
        if (StringUtils.isNotBlank(applicationName)) {
            params.put("applicationName", applicationName);
        }
        if (StringUtils.isNotBlank(method)) {
            params.put("method", method);
        }
        if (StringUtils.isNotBlank(version)) {
            params.put("dubboVersion", version);
        }
        params.put("virtualGwId", virtualGwId);
        return dubboMetaDao.getCountByFields(params);
    }


    @Override
    public List<DubboMetaDto> findByApplicationName(String applicationName, long offset, long limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("applicationName", applicationName);
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params, offset, limit);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DubboMetaDto> findByApplicationName(String applicationName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("applicationName", applicationName);
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params);
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public int countByApplicationName(String applicationName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("applicationName", applicationName);
        return dubboMetaDao.getCountByFields(params);
    }


    @Override
    public List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String group, String version, String method, long offset, long limit) {
        return findByCondition(virtualGwId, interfaceName, StringUtils.EMPTY, group, version, method, offset, limit);
    }

    @Override
    public List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String group, String version, String method) {
        return findByCondition(virtualGwId, interfaceName, StringUtils.EMPTY, group, version, method);

    }

    @Override
    public List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String group, String version) {
        return findByCondition(virtualGwId, interfaceName, group, version, StringUtils.EMPTY);

    }

    @Override
    public int countByCondition(long virtualGwId, String interfaceName, String group, String version, String method) {
        return countByCondition(virtualGwId, interfaceName, StringUtils.EMPTY, group, version, method);
    }


    @Override
    public List<DubboMetaDto> findByIgv(long virtualGwId, String igv) {
        String[] igvs = EnvoyCommonUtil.splitIgv(igv);
        return findByCondition(virtualGwId, igvs[0], igvs[1], igvs[2]).stream().sorted(DubboMetaDto::compareTo).collect(Collectors.toList());
    }

    @Override
    public void batchDeleteByCondition(long virtualGwId, String igv) {
        String[] igvArray = EnvoyCommonUtil.splitIgv(igv);
        dubboMetaDao.batchDeleteByCondition(virtualGwId, igvArray[0], igvArray[1], igvArray[2]);
    }

    @Override
    public DubboMetaDto toView(DubboMeta dubboMeta) {
        if (dubboMeta == null) {
            return null;
        }
        DubboMetaDto dubboMetaDto = new DubboMetaDto();
        dubboMetaDto.setVirtualGwId(dubboMeta.getVirtualGwId());
        dubboMetaDto.setMethod(dubboMeta.getMethod());
        dubboMetaDto.setCreateTime(dubboMeta.getCreateTime());
        dubboMetaDto.setReturns(dubboMeta.getDubboReturns());
        dubboMetaDto.setProtocolVersion(dubboMeta.getProtocolVersion());
        dubboMetaDto.setInterfaceName(dubboMeta.getInterfaceName());
        dubboMetaDto.setId(dubboMeta.getId());
        String[] elements = StringUtils.splitPreserveAllTokens(dubboMeta.getDubboParams(), ",");
        if (!ArrayUtils.isEmpty(elements)) {
            dubboMetaDto.setParams(Lists.newArrayList(elements));
        }

        dubboMetaDto.setVersion(dubboMeta.getDubboVersion());
        dubboMetaDto.setApplicationName(dubboMeta.getApplicationName());
        dubboMetaDto.setGroup(dubboMeta.getDubboGroup());
        return dubboMetaDto;
    }

    @Override
    public DubboMeta toMeta(DubboMetaDto dubboMetaDto) {
        if (dubboMetaDto == null) {
            return null;
        }
        DubboMeta dubboMeta = new DubboMeta();
        dubboMeta.setMethod(dubboMetaDto.getMethod());
        dubboMeta.setVirtualGwId(dubboMetaDto.getVirtualGwId());
        dubboMeta.setCreateTime(dubboMetaDto.getCreateTime());
        dubboMeta.setDubboReturns(dubboMetaDto.getReturns());
        dubboMeta.setProtocolVersion(dubboMetaDto.getProtocolVersion());
        dubboMeta.setInterfaceName(dubboMetaDto.getInterfaceName());
        dubboMeta.setId(dubboMetaDto.getId());
        dubboMeta.setDubboParams(StringUtils.join(dubboMetaDto.getParams(), ","));
        dubboMeta.setDubboVersion(dubboMetaDto.getVersion());
        dubboMeta.setApplicationName(dubboMetaDto.getApplicationName());
        dubboMeta.setDubboGroup(dubboMetaDto.getGroup());
        return dubboMeta;
    }


    @Transactional(rollbackFor = Exception.class)
    public synchronized void saveDubboMeta(long virtualGwId, String igv, List<DubboMetaDto> dubboMetaDtoList) {
        String cacheKey = String.format(EnvoyConst.DUBBO_META_REFRESH_KEY_TEMPLATE, virtualGwId, igv);
        logger.info("dubbo meta cacheKey key is {}", cacheKey);
        if (null == dubboMetaDtoList) {
            logger.warn("调用api-plane 失败，查询条件为 : 网关ID = {} , IGV = {}  无返回 ", virtualGwId, igv);
            return;
        }
        //删除数据
        logger.info("删除 virtualGwId = {} 对应网关下 dubbo 接口 : {}", virtualGwId, igv);
        batchDeleteByCondition(virtualGwId, igv);
        if (NumberUtils.INTEGER_ZERO == dubboMetaDtoList.size()) {
            logger.info("调用api-plane 成功，查询条件为 : 网关ID = {} , IGV = {}  dubbo 元数据信息返回空 ，删除对应数据", virtualGwId, igv);
            return;
        }
        //批量新增数据
        batchCreateDubboMeta(dubboMetaDtoList);
        cacheService.setValue(cacheKey, "", envoyConfig.getMetaRefreshInterval());
    }

    @Override
    public List<DubboMetaDto> refreshDubboMeta(long virtualGwId, String igv) {
        if (NumberUtils.LONG_ZERO == virtualGwId || StringUtils.isBlank(igv)) {
            logger.warn("查询条件存在空值，查询条件为 : 网关ID = {} , IGV = {} ", virtualGwId, igv);
            return Collections.emptyList();
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            logger.warn("虚拟网关不存在，查询条件为 : 网关ID = {} , IGV = {} ", virtualGwId, igv);
            return Collections.emptyList();
        }
        String redisKey = String.format(EnvoyConst.DUBBO_META_REFRESH_KEY_TEMPLATE, virtualGwId, igv);
        logger.info("dubbo meta cache key is {}", redisKey);
        if (cacheService.hasKey(redisKey)) {
            logger.info("距离上次更新不足 {} 毫秒，本次不再更新 ", envoyConfig.getMetaRefreshInterval());
            return findByIgv(virtualGwId, igv);
        }
        List<DubboMetaDto> dubboMetaList = dubboMetaRemoteClient.getDubboMetaList(virtualGatewayDto, igv, StringUtils.EMPTY, StringUtils.EMPTY);
        saveDubboMeta(virtualGwId, igv, dubboMetaList);
        return findByIgv(virtualGwId, igv);
    }


    @Override
    public ErrorCode checkCreateParam(DubboMetaDto dubboMetaDto) {
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(DubboMetaDto dubboMetaDto) {
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public ErrorCode checkDeleteParam(DubboMetaDto dubboMetaDto) {
        return CommonErrorCode.SUCCESS;
    }
}