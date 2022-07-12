package org.hango.cloud.dashboard.apiserver.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dao.IDubboMetaDao;
import org.hango.cloud.dashboard.apiserver.dto.DubboMetaDto;
import org.hango.cloud.dashboard.apiserver.meta.DubboMeta;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IDubboMetaService;
import org.hango.cloud.dashboard.apiserver.service.IRedisService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.ZkClientUtils;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
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
    private ApiServerConfig apiServerConfig;

    @Autowired
    private IGetFromApiPlaneService getFromApiPlaneService;

    @Autowired
    private IRedisService redisService;

    @Override
    public long createDubboMeta(DubboMetaDto dubboMetaDto) {
        DubboMeta info = toMeta(dubboMetaDto);
        info.setCreateTime(System.currentTimeMillis());
        return dubboMetaDao.add(info);
    }

    @Override
    public long updateDubboMeta(DubboMetaDto dubboMetaDto) {
        DubboMeta info = toMeta(dubboMetaDto);
        return dubboMetaDao.update(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDubboMeta(Long id) {
        DubboMetaDto dubboMetaDto = get(id);
        if (dubboMetaDto == null) {
            logger.info("未找到对应数据，id ={}", id);
            return;
        }
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
        Map<String, Object> params = new HashMap<>();
        return dubboMetaDao.getCountByFields(params);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchCreateDubboMeta(List<DubboMetaDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DubboMetaDto dubboMetaDto : list) {
            createDubboMeta(dubboMetaDto);
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
    public List<DubboMetaDto> findByInterfaceNameAndApplicationName(long gwId, String interfaceName, String applicationName, long offset, long limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("interfaceName", interfaceName);
        params.put("applicationName", applicationName);
        params.put("gwId", gwId);

        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params, offset, limit);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DubboMetaDto> findByInterfaceNameAndApplicationName(long gwId, String interfaceName, String applicationName) {
        Map<String, Object> params = new HashMap<>();
        params.put("interfaceName", interfaceName);
        params.put("applicationName", applicationName);
        params.put("gwId", gwId);

        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params);
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public int countByInterfaceNameAndApplicationName(long gwId, String interfaceName, String applicationName) {
        Map<String, Object> params = new HashMap<>();
        params.put("interfaceName", interfaceName);
        params.put("applicationName", applicationName);
        params.put("gwId", gwId);

        return dubboMetaDao.getCountByFields(params);
    }


    @Override
    public List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String applicationName, String group, String version, String method, long offset, long limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwId", gwId);
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
    public List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String applicationName, String group, String version, String method) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwId", gwId);
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
    public int countByCondition(long gwId, String interfaceName, String applicationName, String group, String version, String method) {
        Map<String, Object> params = new HashMap<>();
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
        params.put("gwId", gwId);
        return dubboMetaDao.getCountByFields(params);
    }


    @Override
    public List<DubboMetaDto> findByApplicationName(String applicationName, long offset, long limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationName", applicationName);
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params, offset, limit);
        if (CollectionUtils.isEmpty(dubboMetaList)) {
            return Collections.emptyList();
        }
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DubboMetaDto> findByApplicationName(String applicationName) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationName", applicationName);
        List<DubboMeta> dubboMetaList = dubboMetaDao.getRecordsByField(params);
        return dubboMetaList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public int countByApplicationName(String applicationName) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationName", applicationName);
        return dubboMetaDao.getCountByFields(params);
    }


    @Override
    public List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String group, String version, String method, long offset, long limit) {
        return findByCondition(gwId, interfaceName, StringUtils.EMPTY, group, version, method, offset, limit);
    }

    @Override
    public List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String group, String version, String method) {
        return findByCondition(gwId, interfaceName, StringUtils.EMPTY, group, version, method);

    }

    @Override
    public List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String group, String version) {
        return findByCondition(gwId, interfaceName, group, version, StringUtils.EMPTY);

    }

    @Override
    public int countByCondition(long gwId, String interfaceName, String group, String version, String method) {
        return countByCondition(gwId, interfaceName, StringUtils.EMPTY, group, version, method);
    }


    @Override
    public List<DubboMetaDto> findByIgv(long gwId, String igv) {
        String[] igvs = ZkClientUtils.splitIgv(igv);
        return findByCondition(gwId, igvs[0], igvs[1], igvs[2]).stream().sorted(DubboMetaDto::compareTo).collect(Collectors.toList());
    }

    @Override
    public void batchDeleteByCondition(long gwId, String igv) {
        String[] igvArray = ZkClientUtils.splitIgv(igv);
        dubboMetaDao.batchDeleteByCondition(gwId, igvArray[0], igvArray[1], igvArray[2]);
    }

    @Override
    public DubboMetaDto toView(DubboMeta dubboMeta) {
        if (dubboMeta == null) {
            return null;
        }
        DubboMetaDto dubboMetaDto = new DubboMetaDto();
        dubboMetaDto.setGwId(dubboMeta.getGwId());
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
        dubboMeta.setGwId(dubboMetaDto.getGwId());
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
    public synchronized void saveDubboMeta(long gwId, String igv, List<DubboMetaDto> dubboMetaDtoList) {
        String redisKey = String.format(Const.DUBBO_META_REFRESH_KEY_TEMPLATE, gwId, igv);
        logger.info("dubbo meta redis key is {}", redisKey);
        if (null == dubboMetaDtoList) {
            logger.warn("调用api-plane 失败，查询条件为 : 网关ID = {} , IGV = {}  无返回 ", gwId, igv);
            return;
        }
        //删除数据
        logger.info("删除 gwId = {} 对应网关下 dubbo 接口 : {}", gwId, igv);
        batchDeleteByCondition(gwId, igv);
        if (NumberUtils.INTEGER_ZERO == dubboMetaDtoList.size()) {
            logger.info("调用api-plane 成功，查询条件为 : 网关ID = {} , IGV = {}  dubbo 元数据信息返回空 ，删除对应数据", gwId, igv);
            return;
        }
        //批量新增数据
        batchCreateDubboMeta(dubboMetaDtoList);
        redisService.setValue(redisKey, true, apiServerConfig.getMetaRefreshInterval());
    }

    @Override
    public List<DubboMetaDto> refreshDubboMeta(long gwId, String igv) {
        if (NumberUtils.LONG_ZERO == gwId || StringUtils.isBlank(igv)) {
            logger.warn("查询条件存在空值，查询条件为 : 网关ID = {} , IGV = {} ", gwId, igv);
            return Collections.emptyList();
        }
        String redisKey = String.format(Const.DUBBO_META_REFRESH_KEY_TEMPLATE, gwId, igv);
        logger.info("dubbo meta redis key is {}", redisKey);
        if (redisService.hasKey(redisKey)) {
            logger.info("距离上次更新不足 {} 毫秒，本次不再更新 ", apiServerConfig.getMetaRefreshInterval());
            return findByIgv(gwId, igv);
        }
        List<DubboMetaDto> dubboMetaList = getFromApiPlaneService.getDubboMetaListByApIPlane(gwId, igv, StringUtils.EMPTY, StringUtils.EMPTY);
        saveDubboMeta(gwId, igv, dubboMetaList);
        return findByIgv(gwId, igv);
    }


    @Override
    public ErrorCode checkCreateParam(DubboMetaDto dubboMetaDto) {
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkUpdateParam(DubboMetaDto dubboMetaDto) {
        return CommonErrorCode.Success;
    }


    @Override
    public ErrorCode checkDeleteParam(DubboMetaDto dubboMetaDto) {
        return CommonErrorCode.Success;
    }
}