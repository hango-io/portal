package org.hango.cloud.envoy.infra.virtualgateway.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.virtualgateway.dto.IstioGatewayDto;
import org.hango.cloud.envoy.infra.virtualgateway.dto.IstioGatewayServerDto;
import org.hango.cloud.envoy.infra.virtualgateway.dto.IstioGatewayTlsDto;
import org.hango.cloud.envoy.infra.virtualgateway.service.IEnvoyVgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/7
 */
@Service
public class EnvoyVgServiceImpl implements IEnvoyVgService {

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private CertificateInfoMapper certificateInfoMapper;

    @Autowired
    private IPluginInfoService pluginInfoService;

    //本迭代只支持单向Tls认证
    public static final String SIMPLE = "SIMPLE";



    private static final Logger logger = LoggerFactory.getLogger(EnvoyVgServiceImpl.class);

    @Override
    public boolean publishToGateway(VirtualGatewayBindDto virtualGatewayBindDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayBindDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.warn("未找到对应的虚拟网关，virtualGatewayBindDto = {}", JSON.toJSONString(virtualGatewayBindDto));
            return false;
        }
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null) {
            logger.warn("未找到对应的网关，virtualGatewayBindDto = {}", JSON.toJSONString(virtualGatewayBindDto));
            return false;
        }
        return publishIstioGateway(trans(virtualGatewayBindDto, virtualGatewayDto, gatewayDto), gatewayDto);
    }

    @Override
    public boolean offlineToGateway(long virtualGwId, long projectId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            logger.warn("未找到对应的虚拟网关，virtualGwId = {} , projectId = {}", virtualGwId, projectId);
            return false;
        }
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        if (CollectionUtils.isEmpty(projectIdList)) {
            logger.warn("出现脏数据，该虚拟网关下已无绑定的项目，因此无需继续下线，virtualGwId = {} , projectId = {}", virtualGwId, projectId);
            return true;
        }
        //如果虚拟网关关联的项目有多个，unBindProject操作即为删除某个虚拟网关对应项目的的host配置，而不是直接删除Istio Gateway资源，因此更新即可
        if (projectIdList.size() != NumberUtils.INTEGER_ONE) {
            logger.info("该虚拟网关下有多个项目关联，开始更新Istio Gateway 资源 virtualGatewayDto = {}", JSON.toJSONString(virtualGatewayDto));
            return publishToGateway(new VirtualGatewayBindDto(virtualGwId, projectIdList.stream().filter(p -> p == projectId).collect(Collectors.toList())));
        }
        if (projectIdList.get(0) != projectId) {
            logger.info("该虚拟网关仅被一个项目关联，但不是该项目，因此忽略删除虚拟网关操作 virtualGatewayDto = {} ，projectId = {}", JSON.toJSONString(virtualGatewayDto), projectId);
            return true;
        }
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null) {
            logger.warn("未找到对应的网关，virtualGwId = {} , projectId = {}", virtualGwId, projectId);
            return false;
        }
        VirtualGatewayBindDto virtualGatewayBindDto = new VirtualGatewayBindDto(virtualGwId, projectIdList);
        return offlineIstioGateway(trans(virtualGatewayBindDto, virtualGatewayDto, gatewayDto), gatewayDto);
    }

    @Override
    public boolean refreshToGateway(DomainInfoDTO domainInfoDTO) {
        if (SCHEME_HTTP.equalsIgnoreCase(domainInfoDTO.getProtocol())){
            //http域名不需要刷新
            return true;
        }
        List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getManagedVirtualGatewayList(domainInfoDTO.getProjectId(), domainInfoDTO.getEnv(), domainInfoDTO.getProtocol());
        if (CollectionUtils.isEmpty(virtualGatewayDtos)){
            //https域名未生效无需更新配置
            return true;
        }

        logger.info("开始刷新证书, domainInfoDTO:{}", JSONObject.toJSONString(domainInfoDTO));
        boolean res = true;
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
            VirtualGatewayBindDto virtualGatewayBindDto = new VirtualGatewayBindDto();
            virtualGatewayBindDto.setVirtualGwId(virtualGatewayDto.getId());
            virtualGatewayBindDto.setProjectIdList(virtualGatewayDto.getProjectIdList());
            virtualGatewayBindDto.setDomainInfoDTO(domainInfoDTO);
            boolean pubRes = publishToGateway(virtualGatewayBindDto);
            res &= pubRes;
        }
        return res;
    }

    /**
     * 转换数据
     *
     * @param virtualGatewayBindDto
     * @param virtualGatewayDto
     * @param gatewayDto
     * @return
     */
    private IstioGatewayDto trans(VirtualGatewayBindDto virtualGatewayBindDto, VirtualGatewayDto virtualGatewayDto, GatewayDto gatewayDto) {
        if (ObjectUtils.anyNull(virtualGatewayBindDto, virtualGatewayDto, gatewayDto)) {
            return null;
        }
        IstioGatewayDto istioGatewayDto = new IstioGatewayDto();
        istioGatewayDto.setGwCluster(gatewayDto.getGwClusterName());
        istioGatewayDto.setName(StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, gatewayDto.getGwClusterName(), virtualGatewayDto.getCode()));
        List<IstioGatewayServerDto> servers = StringUtils.equalsIgnoreCase(SCHEME_HTTP, virtualGatewayDto.getProtocol())
                ? transHttp(virtualGatewayDto) : transHttps(virtualGatewayBindDto, virtualGatewayDto);
        istioGatewayDto.setServers(servers);
        return istioGatewayDto;
    }

    /**
     * 转换数据
     *
     * @param virtualGatewayDto
     * @return
     */
    private List<IstioGatewayServerDto> transHttp(VirtualGatewayDto virtualGatewayDto) {
        List<IstioGatewayServerDto> servers = new ArrayList<>();
        IstioGatewayServerDto server = new IstioGatewayServerDto();
        server.setHosts(Lists.newArrayList(BaseConst.SYMBOL_ASTERISK));
        server.setProtocol(virtualGatewayDto.getProtocol());
        server.setNumber(virtualGatewayDto.getPort());
        servers.add(server);
        return servers;
    }

    /**
     * 转换数据
     *
     * @param virtualGatewayBindDto
     * @param virtualGatewayDto
     * @return
     */
    private List<IstioGatewayServerDto> transHttps(VirtualGatewayBindDto virtualGatewayBindDto, VirtualGatewayDto virtualGatewayDto) {
        List<IstioGatewayServerDto> servers = new ArrayList<>();
        //查询虚拟网关下所有项目的域名
        List<Long> projectIdList = virtualGatewayBindDto.getProjectIdList();
        List<DomainInfoDTO> domainInfoDTOS = domainInfoService.getDomainInfos(projectIdList, SCHEME_HTTPS, virtualGatewayDto.getEnvId());
        if (CollectionUtils.isEmpty(domainInfoDTOS)){
            logger.error("虚拟网关下未绑定域名，创建失败");
            return null;
        }
        //替换DB中的域名信息
        DomainInfoDTO domainInfoDTO = virtualGatewayBindDto.getDomainInfoDTO();
        if (domainInfoDTO != null){
            domainInfoDTOS = domainInfoDTOS.stream().filter(o -> !StringUtils.equals(o.getHost(), domainInfoDTO.getHost())).collect(Collectors.toList());
            domainInfoDTOS.add(domainInfoDTO);
        }
        //基于证书进行分组
        Map<Long, List<DomainInfoDTO>> doaminMap = domainInfoDTOS.stream().filter(o -> o.getCertificateId() != null).collect(Collectors.groupingBy(DomainInfoDTO::getCertificateId));
        for (Map.Entry<Long, List<DomainInfoDTO>> entry : doaminMap.entrySet()) {
            List<DomainInfoDTO> domainInfos = entry.getValue();
            IstioGatewayServerDto server = new IstioGatewayServerDto();
            List<String> hosts = domainInfos.stream().map(DomainInfoDTO::getHost).collect(Collectors.toList());
            server.setHosts(hosts);
            server.setProtocol(virtualGatewayDto.getProtocol());
            server.setNumber(virtualGatewayDto.getPort());
            IstioGatewayTlsDto istioGatewayTlsDto = new IstioGatewayTlsDto();
            istioGatewayTlsDto.setMode(SIMPLE);
            CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(entry.getKey());
            istioGatewayTlsDto.setCredentialName(certificateInfoPO.getName());
            server.setPortalIstioGatewayTLSDTO(istioGatewayTlsDto);
            servers.add(server);
        }
        return servers;
    }


    /**
     * 发布istio gateway
     *
     * @param istioGatewayDto
     * @param gatewayDto
     * @return
     */
    private boolean publishIstioGateway(IstioGatewayDto istioGatewayDto, GatewayDto gatewayDto) {
        if (ObjectUtils.anyNull(istioGatewayDto, gatewayDto)) {
            logger.warn("网关信息为空");
            return false;
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.ACTION, "UpdateIstioGateway");
        params.put(BaseConst.VERSION, BaseConst.PLANE_VERSION);


        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.postRequest(gatewayDto.getConfAddr() + PLANE_PORTAL_PATH, JSONObject.toJSONString(istioGatewayDto), params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                recordErrorLog(response);
                return false;
            }
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常，e", e);
            return false;
        }
        return true;
    }

    private void recordErrorLog(HttpClientResponse response){
        logger.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
    }


    /**
     * 下线istio gateway
     *
     * @param istioGatewayDto
     * @param gatewayDto
     * @return
     */
    private boolean offlineIstioGateway(IstioGatewayDto istioGatewayDto, GatewayDto gatewayDto) {
        if (ObjectUtils.anyNull(istioGatewayDto, gatewayDto)) {
            logger.warn("网关信息为空");
            return false;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.ACTION, "DeleteIstioGateway");
        params.put(BaseConst.VERSION, BaseConst.PLANE_VERSION);


        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.postRequest(gatewayDto.getConfAddr() + PLANE_PORTAL_PATH, JSONObject.toJSONString(istioGatewayDto), params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                recordErrorLog(response);
                return false;
            }
        } catch (Exception e) {
            logger.error("调用api-plane发布接口异常", e);
            return false;
        }
        return true;
    }

}
