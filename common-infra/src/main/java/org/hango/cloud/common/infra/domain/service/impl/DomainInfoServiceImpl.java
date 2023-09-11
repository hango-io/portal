package org.hango.cloud.common.infra.domain.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.domain.dao.IDomainInfoDao;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainQueryDTO;
import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.meta.DomainInfoQuery;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SCHEME_HTTPS;

@Slf4j
@Service
public class DomainInfoServiceImpl implements IDomainInfoService {

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private IDomainInfoDao domainInfoDao;

    @Autowired
    private CertificateInfoMapper certificateInfoMapper;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IPluginInfoService pluginInfoService;


    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;


    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IServiceProxyService serviceProxyService;


    @Override
    public long create(DomainInfoDTO domainInfoDTO){
        //设置域名状态
        if (StringUtils.isBlank(domainInfoDTO.getStatus())){
            domainInfoDTO.setStatus(DomainStatusEnum.Managed.name());
        }
        //创建域名
        DomainInfo domainInfoPO = toMeta(domainInfoDTO);
        if (domainInfoPO == null){
            return -1L;
        }
        domainInfoDao.add(domainInfoPO);
        domainInfoDTO.setId(domainInfoPO.getId());
        return domainInfoPO.getId();
    }



    @Override
    public long update(DomainInfoDTO domainInfoDTO){
        DomainInfo domainInfoPO = toMeta(domainInfoDTO);
        if (domainInfoPO == null){
            return -1L;
        }
        //更新域名信息
        domainInfoDao.update(domainInfoPO);
        return domainInfoPO.getId();
    }


    @Override
    public void delete(DomainInfoDTO domainInfoDTO){
        domainInfoMapper.deleteById(domainInfoDTO.getId());
    }

    @Override
    public List<DomainInfoDTO> getDomainInfoList(DomainQueryDTO queryDTO) {
        DomainInfoQuery query = toMeta(queryDTO);
        if (query == null){
            return new ArrayList<>();
        }
        List<DomainInfo> domainInfoPOS = domainInfoDao.getDomainInfoList(query);
        return domainInfoPOS.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public Page<DomainInfo> getDomainInfoPage(DomainQueryDTO queryDTO) {
        DomainInfoQuery query = toMeta(queryDTO);
        if (query == null){
            return Page.of(0,0);
        }
        return domainInfoDao.getDomainInfoPage(query);
    }

    private DomainInfoQuery toMeta(DomainQueryDTO queryDTO){
        DomainInfoQuery query = DomainInfoQuery.builder()
                .pattern(queryDTO.getPattern())
                .protocol(queryDTO.getProtocol())
                .projectIds(Collections.singletonList(queryDTO.getProjectId()))
                .build();
        if (queryDTO.getVirtualGwId() != null){
            VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(queryDTO.getVirtualGwId());
            if (virtualGateway == null || CollectionUtils.isEmpty(virtualGateway.getDomainInfos())){
                return null;
            }
            query.setIds(virtualGateway.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toList()));
            query.setProtocol(virtualGateway.getProtocol());
        }
        query.setLimit(queryDTO.getLimit());
        query.setOffset(queryDTO.getOffset());
        return query;
    }

    @Override
    public DomainInfoDTO get(long id) {
        DomainInfo domainInfoPO = domainInfoDao.get(id);
        return toView(domainInfoPO);
    }


    @Override
    public List<String> getHosts(long projectId, long virtualGatewayId) {
        DomainQueryDTO queryDTO = new DomainQueryDTO();
        queryDTO.setVirtualGwId(virtualGatewayId);
        queryDTO.setProjectId(projectId);
        return getDomainInfoList(queryDTO).stream().map(DomainInfoDTO::getHost).collect(Collectors.toList());
    }

    @Override
    public List<DomainInfoDTO> getDomainInfos(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<DomainInfo> domainInfos = domainInfoDao.getByIds(ids);
        return domainInfos.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DomainInfoDTO> getBindDomainInfoList(DomainQueryDTO queryDTO) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(queryDTO.getVirtualGwId());
        if (virtualGatewayDto == null){
            return new ArrayList<>();
        }
        Set<Long> bindDoaminIds = virtualGatewayDto.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toSet());
        //查询全量的域名
        DomainInfoQuery query = DomainInfoQuery.builder()
                .projectIds(Collections.singletonList(queryDTO.getProjectId()))
                .protocol(virtualGatewayDto.getProtocol())
                .build();
        List<DomainInfo> domainInfoList = domainInfoDao.getDomainInfoList(query);
        //过滤已绑定的域名
        return domainInfoList.stream().filter(o -> !bindDoaminIds.contains(o.getId())).map(this::toView).collect(Collectors.toList());

    }

    @Override
    public ErrorCode checkCreateParam(DomainInfoDTO domainInfoDTO){
        ErrorCode errorCode = paramCheck(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        String host = domainInfoDTO.getHost();
        if (host.startsWith("*")){
            return CommonErrorCode.invalidParameter(  "不支持泛域名 " + host);
        }
        DomainInfoQuery query = DomainInfoQuery.builder().host(host).build();
        List<DomainInfo> domainInfoList = domainInfoDao.getDomainInfoList(query);
        if (CollectionUtils.isNotEmpty(domainInfoList)){
            return CommonErrorCode.invalidParameter("域名已存在，不允许重复创建");
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(DomainInfoDTO domainInfoDTO){
        ErrorCode errorCode = paramCheck(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        if (domainInfoDTO.getId() == null || domainInfoDTO.getId() <= 0){
            return CommonErrorCode.invalidParameter("域名id不能为空");
        }
        DomainInfo domainInfoPO = domainInfoMapper.selectById(domainInfoDTO.getId());
        if (domainInfoPO == null){
            return CommonErrorCode.invalidParameter("域名不存在，更新域名信息失败");
        }
        if (!DomainStatusEnum.Managed.name().equals(domainInfoPO.getStatus())){
            return CommonErrorCode.invalidParameter("该域名未被管理，无法进行修改");
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public ErrorCode checkDeleteParam(DomainInfoDTO domainInfoDTO){
        DomainInfo domainInfoPO = domainInfoMapper.selectById(domainInfoDTO.getId());
        if (domainInfoPO == null){
            return CommonErrorCode.invalidParameter("未找到需要删除的域名");
        }
        VirtualGatewayQuery query = VirtualGatewayQuery.builder().domainId(domainInfoDTO.getId()).build();
        if (virtualGatewayInfoService.exist(query)){
            return CommonErrorCode.invalidParameter("当前域名已被网关使用，不允许删除");
        }
        List<ServiceProxyDto> serviceProxyDtos = serviceProxyService.getServiceProxyByHost(domainInfoPO.getHost());
        if (CollectionUtils.isNotEmpty(serviceProxyDtos)){
            return CommonErrorCode.invalidParameter("当前域名已被服务使用，不允许删除");
        }
        return CommonErrorCode.SUCCESS;
    }


    private ErrorCode paramCheck(DomainInfoDTO domainInfoDTO){
        Set<String> protocol = CommonUtil.splitStringToStringSet(domainInfoDTO.getProtocol(), ",");
        if (protocol.contains(SCHEME_HTTPS) && domainInfoDTO.getCertificateId() == null){
            return CommonErrorCode.invalidParameter("HTTPS域名必须携带证书");
        }
        if (domainInfoDTO.getCertificateId() != null){
            CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(domainInfoDTO.getCertificateId());
            if (certificateInfoPO == null){
                return CommonErrorCode.invalidParameter("无效的证书id");
            }
            if (StringUtils.isBlank(certificateInfoPO.getPrivateKey())){
                return CommonErrorCode.invalidParameter("未上传服务器私钥");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public DomainInfo toMeta(DomainInfoDTO domainInfoDTO){
        return DomainInfo.builder()
                .description(domainInfoDTO.getDescription())
                .status(domainInfoDTO.getStatus())
                .protocol(domainInfoDTO.getProtocol())
                .id(domainInfoDTO.getId())
                .host(domainInfoDTO.getHost())
                .projectId(domainInfoDTO.getProjectId())
                .certificateId(domainInfoDTO.getCertificateId())
                .build();
    }

    @Override
    public DomainInfoDTO toView(DomainInfo domainInfoPO) {
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setDescription(domainInfoPO.getDescription());
        domainInfoDTO.setStatus(domainInfoPO.getStatus());
        domainInfoDTO.setProtocol(domainInfoPO.getProtocol());
        domainInfoDTO.setId(domainInfoPO.getId());
        domainInfoDTO.setHost(domainInfoPO.getHost());
        domainInfoDTO.setCertificateId(domainInfoPO.getCertificateId());
        Long certificateId = domainInfoPO.getCertificateId();
        if (certificateId != null){
            CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(domainInfoPO.getCertificateId());
            domainInfoDTO.setCertificateName(certificateInfoPO.getName());
        }
        domainInfoDTO.setProjectId(domainInfoPO.getProjectId());
        return domainInfoDTO;
    }
}
