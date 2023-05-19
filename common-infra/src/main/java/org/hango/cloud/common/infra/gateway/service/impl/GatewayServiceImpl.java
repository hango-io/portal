package org.hango.cloud.common.infra.gateway.service.impl;

import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.gateway.dao.IGatewayDao;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.meta.Gateway;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 网关信息表
 * @date 2022/10/25
 */
@Service
public class GatewayServiceImpl implements IGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceImpl.class);

    @Autowired
    private IGatewayDao gatewayDao;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Override
    public long create(GatewayDto gatewayDto) {
        Gateway info = toMeta(gatewayDto);
        info.setCreateTime(System.currentTimeMillis());
        info.setModifyTime(System.currentTimeMillis());
        long id = gatewayDao.add(info);
        gatewayDto.setId(id);
        return id;
    }

    @Override
    public long update(GatewayDto gatewayDto) {
        Gateway gateway = gatewayDao.get(gatewayDto.getId());
        gateway.setModifyTime(System.currentTimeMillis());
        gateway.setName(gatewayDto.getName());
        gateway.setDescription(gatewayDto.getDescription());
        gateway.setConfAddr(gatewayDto.getConfAddr());
        gateway.setGwClusterName(gatewayDto.getGwClusterName());
        return gatewayDao.update(gateway);
    }


    @Override
    public void delete(GatewayDto gatewayDto) {
        gatewayDao.delete(toMeta(gatewayDto));
    }

    @Override
    public List<GatewayDto> findAll() {
        List<Gateway> gatewayList = gatewayDao.findAll();
        if (CollectionUtils.isEmpty(gatewayList)) {
            return Collections.emptyList();
        }
        return gatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        Map<String, Object> params = Maps.newHashMap();
        return gatewayDao.getCountByFields(params);
    }

    /**
     * @see org.hango.cloud.common.infra.gateway.hooker.AbstractGatewayHooker#doFindMultiEnhancement(List)
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<GatewayDto> findAll(long offset, long limit) {
        List<Gateway> gatewayList = gatewayDao.getRecordsByField(Maps.newHashMap(), offset, limit);
        if (CollectionUtils.isEmpty(gatewayList)) {
            return Collections.emptyList();
        }
        return gatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public GatewayDto get(long id) {
        return toView(gatewayDao.get(id));
    }

    @Override
    public List<GatewayDto> findByName(String name, long offset, long limit) {
        List<Gateway> gatewayList = gatewayDao.getByName(name, offset, limit);
        if (CollectionUtils.isEmpty(gatewayList)) {
            return Collections.emptyList();
        }
        return gatewayList.stream().map(this::toView).collect(Collectors.toList());
    }


    @Override
    public int countByName(String name) {
        return gatewayDao.countByName(name);
    }


    @Override
    public GatewayDto toView(Gateway gateway) {
        if (gateway == null) {
            return null;
        }
        GatewayDto gatewayDto = new GatewayDto();
        gatewayDto.setGwClusterName(gateway.getGwClusterName());
        gatewayDto.setModifyTime(gateway.getModifyTime());
        gatewayDto.setCreateTime(gateway.getCreateTime());
        gatewayDto.setName(gateway.getName());
        gatewayDto.setDescription(gateway.getDescription());
        gatewayDto.setSvcType(gateway.getSvcType());
        gatewayDto.setSvcName(gateway.getSvcName());
        gatewayDto.setEnvId(gateway.getEnvId());
        gatewayDto.setId(gateway.getId());
        gatewayDto.setConfAddr(gateway.getConfAddr());
        gatewayDto.setType(gateway.getType());
        return gatewayDto;
    }

    @Override
    public Gateway toMeta(GatewayDto gatewayDto) {
        if (gatewayDto == null) {
            return null;
        }
        Gateway gateway = new Gateway();
        gateway.setGwClusterName(gatewayDto.getGwClusterName());
        gateway.setModifyTime(gatewayDto.getModifyTime());
        gateway.setCreateTime(gatewayDto.getCreateTime());
        gateway.setName(gatewayDto.getName());
        gateway.setDescription(gatewayDto.getDescription());
        gateway.setSvcType(gatewayDto.getSvcType());
        gateway.setSvcName(gatewayDto.getSvcName());
        gateway.setEnvId(gatewayDto.getEnvId());
        gateway.setId(gatewayDto.getId());
        gateway.setConfAddr(gatewayDto.getConfAddr());
        gateway.setType(gatewayDto.getType());
        return gateway;
    }

    @Override
    public ErrorCode checkCreateParam(GatewayDto gatewayDto) {
        List<GatewayDto> gatewayDtoList = findAll();
        if (CollectionUtils.isEmpty(gatewayDtoList)) {
            return CommonErrorCode.SUCCESS;
        }
        Optional<GatewayDto> nameCheck = gatewayDtoList.stream().filter(g -> g.getName().equals(gatewayDto.getName())).findFirst();
        if (nameCheck.isPresent()) {
            return CommonErrorCode.GW_NAME_ALREADY_EXIST;
        }
        Optional<GatewayDto> gwClusterNameCheck = gatewayDtoList.stream().filter(g -> g.getGwClusterName().equals(gatewayDto.getGwClusterName())).findFirst();
        if (gwClusterNameCheck.isPresent()) {
            return CommonErrorCode.GW_CLUSTER_NAME_ALREADY_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(GatewayDto gatewayDto) {
        List<GatewayDto> gatewayDtoList = findAll();
        Optional<GatewayDto> existCheck = gatewayDtoList.stream().filter(g -> g.getId() == gatewayDto.getId()).findFirst();
        if (!existCheck.isPresent()) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        Optional<GatewayDto> nameCheck = gatewayDtoList.stream().filter(g -> g.getId() != gatewayDto.getId() && g.getName().equals(gatewayDto.getName())).findFirst();
        if (nameCheck.isPresent()) {
            return CommonErrorCode.GW_NAME_ALREADY_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(GatewayDto gatewayDto) {
        List<VirtualGatewayDto> virtualGatewayList = virtualGatewayInfoService.getVirtualGatewayList(Collections.singletonList(gatewayDto.getId()));
        if ( CollectionUtils.isEmpty(virtualGatewayList)) {
            return CommonErrorCode.CANNOT_DELETE_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }
}