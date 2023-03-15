package org.hango.cloud.common.infra.virtualgateway.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Modified hanjiahao
 * 网关基本管理，包括创建网关、删除网关、查询网关基本信息
 */
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2022-10-30"})
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
        if (CollectionUtils.isNotEmpty(virtualGatewayDto.getVirtualHostList())) {
            iDomainInfoService.createDomainInfoByVgId(ProjectTraceHolder.getProId(),virtualGatewayDto.getVirtualHostList(), virtualGatewayId);
        }
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
        //hango 开源版本时才会调用
        if (CollectionUtils.isNotEmpty(virtualGatewayDto.getVirtualHostList())) {
            iDomainInfoService.updateDomainInfoByVg(ProjectTraceHolder.getProId(),virtualGatewayDto.getVirtualHostList(), virtualGatewayDto.getId());
        }
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
        if (virtualGatewayDto == null) {
            return apiReturn(new Result());
        }
        ErrorCode errorCode = virtualGatewayService.checkDeleteParam(virtualGatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        virtualGatewayService.delete(virtualGatewayDto);
        return apiReturn(new Result(virtualGwId));
    }

    /**
      * @Description: hango 删除虚拟网关信息
      * 包括解绑和删除两个部分
      * @param virtualGwId
      * @return apiReturn
      * @author xianyanglin
      * @date 2023/2/6 14:33
      */
    @GetMapping(params = {"Action=DeleteVirtualGatewayFromHango"})
    public Object deleteVirtualGatewayFromHango(@RequestParam(name = "VirtualGwId") long virtualGwId) {
        logger.info("删除虚拟网关信息! VirtualGwId = {}", virtualGwId);
        VirtualGatewayDto virtualGatewayDto = virtualGatewayService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            return apiReturn(new Result());
        }
        //检查是否存在还未下线的服务
        ErrorCode errorCode = virtualGatewayService.checkDeleteVirtualGatewayParamFromHango(virtualGatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)){
            return apiReturn(Result.err(errorCode));
        }
        //解绑
        iDomainInfoService.deleteDomainInfoByVgId(ProjectTraceHolder.getProId(),virtualGwId);
        //删除
        virtualGatewayService.delete(virtualGatewayDto);
        return apiReturn(new Result(virtualGwId));
    }

    /**
     * 查询所有虚拟网关信息
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribeAllVirtualGateway"})
    public Object findAll() {
        logger.info("查询所有虚拟网关信息! ");
        List<? extends VirtualGatewayDto> virtualGatewayDtoList = virtualGatewayService.findAll();
        return apiReturn(new Result(virtualGatewayDtoList));
    }

    /**
     * 分页查询所有虚拟网关信息
     *
     * @param limit
     * @param offset
     * @return
     */
    @GetMapping(params = {"Action=DescribeAllVirtualGatewayByPage"})
    public Object findAllByPage(@RequestParam(name = "Offset", defaultValue = "0", required = false) long offset,
                                @RequestParam(name = "Limit", defaultValue = "20", required = false) long limit) {
        logger.info("分页查询所有虚拟网关信息! ");
        List<? extends VirtualGatewayDto> virtualGatewayDtoList = virtualGatewayService.findAll(offset, limit);
        long virtualGatewayCount = virtualGatewayService.countAll();
        return apiReturn(new PageResult(virtualGatewayDtoList, virtualGatewayCount));
    }

    /**
     * 通过Id获取虚拟网关信息信息
     *
     * @param virtualGwId
     * @return
     */
    @GetMapping(params = {"Action=DescribeVirtualGatewayById"})
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
    @PostMapping(params = {"Action=DescribeVirtualGateway"})
    public Object describeVirtualGateway(@RequestBody QueryVirtualGatewayDto query) {
        logger.info("通过基于项目隔离，项目id,虚拟网关名称获取虚拟网关信息! query is {}", JSON.toJSONString(query));
        List<VirtualGatewayDto> virtualGatewayDtoList = virtualGatewayService.getVirtualGatewayListByConditions(query);
        int virtualGatewayCount = virtualGatewayService.countVirtualGatewayByConditions(query);

        return apiReturn(new PageResult(virtualGatewayDtoList, virtualGatewayCount));
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
        virtualGatewayProjectService.updateBindDomainStatus(bind);
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
        virtualGatewayProjectService.updateUnbindDomainStatus(virtualGwId, projectId);
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


}
