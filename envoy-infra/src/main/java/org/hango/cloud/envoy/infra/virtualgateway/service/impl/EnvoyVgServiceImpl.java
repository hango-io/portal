package org.hango.cloud.envoy.infra.virtualgateway.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.domain.dao.IDomainInfoDao;
import org.hango.cloud.common.infra.domain.dto.DomainBindDTO;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.virtualgateway.dto.GatewaySettingDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.util.LogUtil;
import org.hango.cloud.envoy.infra.gateway.dto.EnvoyServiceDTO;
import org.hango.cloud.envoy.infra.gateway.dto.EnvoyServicePortDTO;
import org.hango.cloud.envoy.infra.gateway.service.IEnvoyGatewayService;
import org.hango.cloud.envoy.infra.virtualgateway.dto.IpSourceEnvoyFilterDTO;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.PLANE_PORTAL_PATH;
import static org.hango.cloud.common.infra.base.meta.BaseConst.SCHEME_HTTP;

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
    private IDomainInfoDao domainInfoDao;

    @Autowired
    private CertificateInfoMapper certificateInfoMapper;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IEnvoyGatewayService envoyGatewayService;

    //本迭代只支持单向Tls认证
    public static final String SIMPLE = "SIMPLE";

    public static final String CUSTOM_HEADER = "customHeader";

    public static final String XFF = "xff";

    public static final String NODE_PORT = "NodePort";
    public static final String CLUSTER_IP = "ClusterIp";

    private static final Logger logger = LoggerFactory.getLogger(EnvoyVgServiceImpl.class);

    @Override
    public boolean bindDomain(DomainBindDTO domainBindDTO) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(domainBindDTO.getVirtualGwId());
        if (!CollectionUtils.isEmpty(virtualGatewayDto.getDomainInfos()) &&
                SCHEME_HTTP.equalsIgnoreCase(virtualGatewayDto.getProtocol())){
            //http域名不需要刷新
            return true;
        }
        //构建目标域名列表
        List<Long> targetDomainIds = virtualGatewayDto.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toList());
        targetDomainIds.addAll(domainBindDTO.getDomainIds());
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(new ArrayList<>(targetDomainIds));
        virtualGatewayDto.setDomainInfos(domainInfos);
        //更新域名
        return publishToGateway(virtualGatewayDto);
    }


    @Override
    public boolean unBindDomain(DomainBindDTO domainBindDTO) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(domainBindDTO.getVirtualGwId());

        //构建域名列表
        List<Long> domainIds = virtualGatewayDto.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toList());
        domainIds.removeAll(domainBindDTO.getDomainIds());
        if (!CollectionUtils.isEmpty(domainIds) &&
                SCHEME_HTTP.equalsIgnoreCase(virtualGatewayDto.getProtocol())){
            //http域名不需要刷新
            return true;
        }
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(domainIds);
        //域名为空，需要删除虚拟网关
        if (CollectionUtils.isEmpty(domainInfos)){
            return offlineToGateway(domainBindDTO.getVirtualGwId());
        }
        //更新域名
        virtualGatewayDto.setDomainInfos(domainInfos);
        return publishToGateway(virtualGatewayDto);
    }


    @Override
    public boolean publishToGateway(VirtualGatewayDto virtualGatewayDto) {
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null) {
            logger.warn("未找到对应的网关，gwId = {}", virtualGatewayDto.getGwId());
            return false;
        }
        return publishIstioGateway(trans(virtualGatewayDto, gatewayDto.getGwClusterName()), gatewayDto);
    }

    @Override
    public boolean offlineToGateway(long virtualGwId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            logger.warn("未找到对应的虚拟网关，virtualGwId = {}", virtualGwId);
            return false;
        }
        return offlineIstioGateway(virtualGatewayDto);
    }

    @Override
    public boolean refreshToGateway(DomainInfoDTO domainInfoDTO) {
        if (SCHEME_HTTP.equalsIgnoreCase(domainInfoDTO.getProtocol())){
            //http域名不需要刷新
            return true;
        }
        DomainInfo dbDomain = domainInfoDao.get(domainInfoDTO.getId());
        if (!Objects.equals(dbDomain.getCertificateId(), domainInfoDTO.getCertificateId())){
            //证书未修改不需要刷新
            return true;
        }
        QueryVirtualGatewayDto query = new QueryVirtualGatewayDto();
        query.setDomainId(domainInfoDTO.getId());
        List<VirtualGatewayDto> virtualGatewayList = virtualGatewayInfoService.getVirtualGatewayList(query);
        if (CollectionUtils.isEmpty(virtualGatewayList)){
            //当前域名未被虚拟网关使用，不需要刷新
            return true;
        }
        boolean res = true;
        //刷新证书
        for (VirtualGatewayDto virtualGateway : virtualGatewayList) {
            List<DomainInfoDTO> domainInfos = virtualGateway.getDomainInfos();
            if (CollectionUtils.isEmpty(domainInfos)){
                continue;
            }
            for (DomainInfoDTO domainInfo : domainInfos) {
                if (domainInfo.getId().equals(domainInfoDTO.getId())){
                    continue;
                }
                domainInfo.setCertificateId(domainInfoDTO.getCertificateId());
                boolean pubRes = publishToGateway(virtualGateway);
                res &= pubRes;
            }
        }
        return res;
    }


    private IstioGatewayDto trans(VirtualGatewayDto virtualGatewayDto, String gwClusterName) {
        if (virtualGatewayDto == null) {
            return null;
        }
        IstioGatewayDto istioGatewayDto = new IstioGatewayDto();
        istioGatewayDto.setGwCluster(gwClusterName);
        istioGatewayDto.setName(StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, gwClusterName, virtualGatewayDto.getCode()));
        List<IstioGatewayServerDto> servers = StringUtils.equalsIgnoreCase(SCHEME_HTTP, virtualGatewayDto.getProtocol())
                ? transHttp(virtualGatewayDto) : transHttps(virtualGatewayDto);
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
     * 构造gateway资源，会基于证书进行分组，示例如下
     * servers:
     *   - hosts:
     *     - test.httpbin.com
     *     - hello.httpbin.com
     *     port:
     *       name: httpbin
     *       number: 443
     *       protocol: HTTPS
     *     tls:
     *       credentialName: kubernetes-gateway://gateway-system/httpbin
     *       mode: SIMPLE
     *   - hosts:
     *     - test.e2e.com
     *     port:
     *       name: e2e
     *       number: 444
     *       protocol: HTTPS
     *     tls:
     *       credentialName: kubernetes-gateway://gateway-system/e2e
     *       mode: SIMPLE
     */
    private List<IstioGatewayServerDto> transHttps(VirtualGatewayDto virtualGatewayDto) {
        List<IstioGatewayServerDto> servers = new ArrayList<>();
        List<DomainInfoDTO> domainInfoDTOS = virtualGatewayDto.getDomainInfos();
        if (CollectionUtils.isEmpty(domainInfoDTOS)){
            return null;
        }
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
                logger.error(LogUtil.buildPlaneErrorLog(response));
                return false;
            }
        } catch (Exception e) {
            logger.error(LogUtil.buildPlaneExceptionLog(), e);
            return false;
        }
        return true;
    }

    /**
     * 下线istio gateway
     *
     */
    private boolean offlineIstioGateway(VirtualGatewayDto virtualGatewayDto) {
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null){
            return false;
        }
        IstioGatewayDto istioGatewayDto = new IstioGatewayDto();
        istioGatewayDto.setGwCluster(gatewayDto.getGwClusterName());
        istioGatewayDto.setName(StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, gatewayDto.getGwClusterName(), virtualGatewayDto.getCode()));
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.ACTION, "DeleteIstioGateway");
        params.put(BaseConst.VERSION, BaseConst.PLANE_VERSION);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.postRequest(gatewayDto.getConfAddr() + PLANE_PORTAL_PATH, JSONObject.toJSONString(istioGatewayDto), params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                logger.error(LogUtil.buildPlaneErrorLog(response));
                return false;
            }
        } catch (Exception e) {
            logger.error(LogUtil.buildPlaneExceptionLog(), e);
            return false;
        }
        return true;
    }

    /**
     * 下线自定义ip配置
     */
    public boolean deleteIpSource(Long vgId) {
        GatewaySettingDTO gatewaySetting = virtualGatewayInfoService.getGatewaySetting(vgId);
        if (gatewaySetting == null){
            return true;
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgId);
        IpSourceEnvoyFilterDTO param = trans(virtualGatewayDto);
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.ACTION, "DeleteIpSource");
        params.put(BaseConst.VERSION, BaseConst.PLANE_VERSION);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/api", JSONObject.toJSONString(param), params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                logger.error(LogUtil.buildPlaneErrorLog(response));
                return false;
            }
        } catch (Exception e) {
            logger.error(LogUtil.buildPlaneExceptionLog(), e);
            return false;
        }
        return true;
    }

    private ErrorCode publishIpSource(GatewaySettingDTO gatewaySettingDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(gatewaySettingDto.getVirtualGwId());
        IpSourceEnvoyFilterDTO param = trans(virtualGatewayDto);
        param.setCustomIpAddressHeader(gatewaySettingDto.getCustomIpAddressHeader());
        Integer xffNumTrustedHops = gatewaySettingDto.getXffNumTrustedHops();
        if (xffNumTrustedHops != null){
            param.setXffNumTrustedHops(xffNumTrustedHops - 1);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.ACTION, "PublishIpSource");
        params.put(BaseConst.VERSION, BaseConst.PLANE_VERSION);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpClientResponse response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/api", JSONObject.toJSONString(param), params, headers, EnvoyConst.MODULE_API_PLANE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                logger.error(LogUtil.buildPlaneErrorLog(response));
                return CommonErrorCode.INTERNAL_SERVER_ERROR;
            }
        } catch (Exception e) {
            logger.error(LogUtil.buildPlaneExceptionLog(), e);
            return CommonErrorCode.INTERNAL_SERVER_ERROR;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode updateEnvoyGatewaySetting(GatewaySettingDTO gatewaySettingDto) {
        //参数校验
        ErrorCode errorCode = paramCheck(gatewaySettingDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        //数据处理
        dataHandler(gatewaySettingDto);
        //发布api-plane
        errorCode = publishIpSource(gatewaySettingDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        //更新db数据
        virtualGatewayInfoService.updateGatewaySetting(gatewaySettingDto);
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public GatewaySettingDTO getEnvoyGatewaySetting(Long virtualGwId) {
        return virtualGatewayInfoService.getGatewaySetting(virtualGwId);
    }

    private ErrorCode paramCheck(GatewaySettingDTO settingDto){
        if (CUSTOM_HEADER.equals(settingDto.getIpSource())
                && StringUtils.isBlank(settingDto.getCustomIpAddressHeader())){
            return CommonErrorCode.invalidParameter("自定义Header不能为空");
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(settingDto.getVirtualGwId());
        if (virtualGatewayDto == null){
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    private void dataHandler(GatewaySettingDTO settingDto){
        switch (settingDto.getIpSource()){
            case CUSTOM_HEADER:
                settingDto.setXffNumTrustedHops(null);
                break;
            case XFF:
                settingDto.setCustomIpAddressHeader(null);
                Integer xffNumTrustedHops = settingDto.getXffNumTrustedHops();
                if (xffNumTrustedHops == null){
                    xffNumTrustedHops = 1;
                }
                settingDto.setXffNumTrustedHops(xffNumTrustedHops);
                break;
            default:
                break;
        }
    }

    private IpSourceEnvoyFilterDTO trans(VirtualGatewayDto virtualGatewayDto){
        IpSourceEnvoyFilterDTO param = new IpSourceEnvoyFilterDTO();
        param.setName("ip-source-"+ virtualGatewayDto.getGwClusterName() + "-" + virtualGatewayDto.getCode());
        param.setGwCluster(virtualGatewayDto.getGwClusterName());
        param.setPortNumber(virtualGatewayDto.getPort());
        return param;
    }


    @Override
    public List<String> getEnvoyListenerAddr(VirtualGatewayDto virtualGatewayDto) {
        if (CollectionUtils.isEmpty(virtualGatewayDto.getProjectIdList())){
            return null;
        }
        List<EnvoyServiceDTO> envoyServiceDTOS = envoyGatewayService.getEnvoyService(virtualGatewayDto.getGwId());
        if (CollectionUtils.isEmpty(envoyServiceDTOS)){
            return null;
        }
        //暂不考虑多服务的场景，只获取第一个服务配置。
        EnvoyServiceDTO envoyServiceDTO = envoyServiceDTOS.get(0);
        //ClusterIP
        if (CLUSTER_IP.equalsIgnoreCase(envoyServiceDTO.getServiceType())){
            return null;
        }
        List<String> ips = envoyServiceDTO.getIps();
        if (CollectionUtils.isEmpty(ips)){
            return null;
        }

        int targetPort = virtualGatewayDto.getPort();
        if (NODE_PORT.equalsIgnoreCase(envoyServiceDTO.getServiceType())){
            targetPort = getNodePort(virtualGatewayDto.getPort(), envoyServiceDTO.getPorts());
        }
        List<String> eps = new ArrayList<>();
        for (String ip : ips) {
            String ep = ip;
            if (targetPort > 0){
                ep = ep + ":" + targetPort;
            }
            eps.add(ep);
        }
        return eps;
    }

    private int getNodePort(int port, List<EnvoyServicePortDTO> servicePortDTOS){
        if (CollectionUtils.isEmpty(servicePortDTOS)){
            return 0;
        }
        return servicePortDTOS.stream().filter(o -> port == o.getPort()).map(EnvoyServicePortDTO::getNodePort).findFirst().orElse(0);
    }
}
