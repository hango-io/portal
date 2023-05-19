package org.hango.cloud.common.infra.virtualgateway.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.domain.dto.DomainBindDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.SingleVgBindDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Modified hanjiahao
 * 网关基本管理，包括创建网关、删除网关、查询网关基本信息
 */
@RestController
@RequestMapping(value = ApiConst.HANGO_VIRTUAL_GATEWAY_V1_PREFIX)
@Validated
public class VirtualGatewayController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(VirtualGatewayController.class);
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayService;

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Autowired
    private IDomainInfoService iDomainInfoService;
    /**
     * 创建虚拟网关信息
     *
     * @param virtualGatewayDto
     * @return
     */
    @PostMapping(params = {"Action=CreateVirtualGateway"})
    public Object createVirtualGateway(@RequestBody @Validated VirtualGatewayDto virtualGatewayDto) {
        logger.info("创建虚拟网关信息! virtualGatewayDto = {}", JSON.toJSONString(virtualGatewayDto));
        ErrorCode errorCode = virtualGatewayService.checkCreateParam(virtualGatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long virtualGatewayId = virtualGatewayService.create(virtualGatewayDto);
        return apiReturn(new Result(virtualGatewayId));
    }

    /**
     * 修改虚拟网关信息
     *
     * @param virtualGatewayDto
     * @return
     */
    @PostMapping(params = {"Action=UpdateVirtualGateway"})
    public Object updateVirtualGateway(@RequestBody VirtualGatewayDto virtualGatewayDto) {
        logger.info("修改虚拟网关信息! virtualGatewayDto = {}", JSON.toJSONString(virtualGatewayDto));
        ErrorCode errorCode = virtualGatewayService.checkUpdateParam(virtualGatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long virtualGatewayId = virtualGatewayService.update(virtualGatewayDto);
        return apiReturn(new Result(virtualGatewayId));
    }

    /**
     * 删除虚拟网关信息
     *
     * @param virtualGwId
     */
    @GetMapping(params = {"Action=DeleteVirtualGateway"})
    public Object deleteVirtualGateway(@RequestParam(name = "VirtualGwId") long virtualGwId) {
        logger.info("删除虚拟网关信息! VirtualGwId = {}", virtualGwId);
        VirtualGatewayDto virtualGatewayDto = virtualGatewayService.get(virtualGwId);
        ErrorCode errorCode = virtualGatewayService.checkDeleteParam(virtualGatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        virtualGatewayService.delete(virtualGatewayDto);
        return apiReturn(new Result(virtualGwId));
    }


    /**
     * 通过Id获取虚拟网关信息信息
     *
     * @param virtualGwId
     * @return
     */
    @GetMapping(params = {"Action=DescribeVirtualGateway"})
    public Object get(@RequestParam(name = "VirtualGwId") long virtualGwId) {
        logger.info("通过Id获取虚拟网关信息! virtualGwId = {}", virtualGwId);
        VirtualGatewayDto virtualGatewayDto = virtualGatewayService.get(virtualGwId);
        return apiReturn(new Result<>(virtualGatewayDto));
    }


    /**
     * 通过基于项目隔离，项目id,虚拟网关名称分页获取虚拟网关信息信息
     *
     * @param query
     * @return
     */
    @PostMapping(params = {"Action=DescribeVirtualGatewayPage"})
    public Object describeVirtualGatewayPage(@RequestBody QueryVirtualGatewayDto query) {
        logger.info("分页查询虚拟网关信息! query is {}", JSON.toJSONString(query));
        Page<VirtualGatewayDto> page = virtualGatewayService.getVirtualGatewayPage(query);
        return apiReturn(new PageResult(page.getRecords(), page.getTotal()));
    }

    /**
     * 通过基于项目隔离，项目id,虚拟网关名称分页获取虚拟网关信息信息
     *
     * @param query
     * @return
     */
    @PostMapping(params = {"Action=DescribeVirtualGatewayList"})
    public Object describeVirtualGatewayList(@RequestBody QueryVirtualGatewayDto query) {
        logger.info("列表查询虚拟网关信息! query is {}", JSON.toJSONString(query));
        List<VirtualGatewayDto> virtualGatewayList = virtualGatewayService.getVirtualGatewayList(query);
        return apiReturn(new Result<>(virtualGatewayList));
    }


    /**
     * 关联项目
     *
     * @param bind
     * @return
     */
    @PostMapping(params = {"Action=UpdateProjectBinding"})
    public Object bindProject(@RequestBody @Validated VirtualGatewayBindDto bind) {
        logger.info("虚拟网关关联对象! bind is {}", JSON.toJSONString(bind));
        ErrorCode errorCode = virtualGatewayProjectService.checkBindProject(bind);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long result = virtualGatewayProjectService.bindProject(bind);
        return apiReturn(new Result<>(result));
    }

    /**
     * 关联项目
     *
     * @param virtualGwId
     * @param projectId
     * @return
     */
    @GetMapping(params = {"Action=UnBindProject"})
    public Object unBindProject(@RequestParam(name = "VirtualGwId") long virtualGwId,
                                @RequestParam(name = "ProjectId") long projectId) {
        logger.info("虚拟网关取消关联对象! VirtualGwId is {} , ProjectId is {}", virtualGwId, projectId);
        ErrorCode errorCode = virtualGatewayProjectService.checkUnBindProject(virtualGwId,projectId);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long result = virtualGatewayProjectService.unbindProject(virtualGwId, projectId);
        return apiReturn(new Result<>(result));
    }


    /**
     * 查询虚拟网关关联信息
     *
     * @param query
     * @return
     */
    @PostMapping(params = {"Action=DescribeProjectBind"})
    public Object bindProject(@RequestBody @Validated QueryVirtualGatewayDto query) {
        List<SingleVgBindDto> bindList = virtualGatewayProjectService.getBindList(query);
        long count = virtualGatewayProjectService.countBindList(query);
        return apiReturn(new PageResult(bindList, count));
    }


    /**
     * 虚拟网关绑定域名
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=BindDomainInfo"}, method = RequestMethod.POST)
    public Object bindDomainInfo(@RequestBody DomainBindDTO domainInfoDTO) {
        logger.info("start bind domain param:{}", JSONObject.toJSONString(domainInfoDTO));
        ErrorCode errorCode = virtualGatewayProjectService.checkBindParam(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        virtualGatewayProjectService.bindDomain(domainInfoDTO);
        return apiReturn(new Result(domainInfoDTO.getVirtualGwId()));
    }

    /**
     * 虚拟网关绑定域名
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UnbindDomainInfo"}, method = RequestMethod.POST)
    public Object unbindDomainInfo(@RequestBody DomainBindDTO domainInfoDTO) {
        logger.info("start unbind domain param:{}", JSONObject.toJSONString(domainInfoDTO));
        ErrorCode errorCode = virtualGatewayProjectService.checkUnbindParam(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        virtualGatewayProjectService.unbindDomain(domainInfoDTO);
        return apiReturn(new Result(domainInfoDTO.getVirtualGwId()));
    }


}
