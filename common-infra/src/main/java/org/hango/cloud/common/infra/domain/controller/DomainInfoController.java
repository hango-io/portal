package org.hango.cloud.common.infra.domain.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainQueryDTO;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2022/10/24
 * 域名管理控制台
 */
@Slf4j
@RestController
@RequestMapping(value = ApiConst.HANGO_DOMAIN_V1_PREFIX)
@Validated
public class DomainInfoController extends AbstractController {

    @Autowired
    private IDomainInfoService domainInfoService;

    /**
     * 创建域名
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateDomain"}, method = RequestMethod.POST)
    public Object createDomain(@Validated @RequestBody DomainInfoDTO domainInfoDTO) {
        log.info("start create domain param:{}", JSONObject.toJSONString(domainInfoDTO));
        domainInfoDTO.setProjectId(ProjectTraceHolder.getProId());
        ErrorCode errorCode = domainInfoService.checkCreateParam(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long id = domainInfoService.create(domainInfoDTO);
        return apiReturn(new Result(id));
    }


    /**
     * 更新域名
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateDomain"}, method = RequestMethod.POST)
    public Object updateDomain(@RequestBody DomainInfoDTO domainInfoDTO) {
        log.info("start update domain param:{}", JSONObject.toJSONString(domainInfoDTO));
        domainInfoDTO.setProjectId(ProjectTraceHolder.getProId());
        ErrorCode errorCode = domainInfoService.checkUpdateParam(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long id = domainInfoService.update(domainInfoDTO);
        return apiReturn(new Result(id));
    }

    /**
     * 删除域名
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteDomain"}, method = RequestMethod.GET)
    public Object deleteDomain(@RequestParam(value = "DomainId") long id) {
        log.info("start delete domain id:{}", id);
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setId(id);
        ErrorCode errorCode = domainInfoService.checkDeleteParam(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        domainInfoService.delete(domainInfoDTO);
        return apiReturn(new Result(id));
    }


    /**
     * 分页查询域名
     */
    @PostMapping(params = {"Action=DescribeDomainPage"})
    public Object describeDomainPage(@RequestBody DomainQueryDTO domainQueryDTO) {
        domainQueryDTO.setProjectId(ProjectTraceHolder.getProId());
        Page<DomainInfo> page = domainInfoService.getDomainInfoPage(domainQueryDTO);
        if (page.getRecords() == null){
            return PageResult.ofEmpty();
        }
        List<DomainInfoDTO> domainInfoDTOS = page.getRecords().stream().map(domainInfoService::toView).collect(Collectors.toList());
        return apiReturn(new PageResult(domainInfoDTOS, page.getTotal()));
    }

    /**
     * 列表查询域名信息
     */
    @PostMapping(params = {"Action=DescribeDomainList"})
    public Object describeDomainList(@RequestBody DomainQueryDTO domainQueryDTO) {
        domainQueryDTO.setProjectId(ProjectTraceHolder.getProId());
        List<DomainInfoDTO> domainInfoList = domainInfoService.getDomainInfoList(domainQueryDTO);
        return apiReturn(new Result(domainInfoList));
    }

    /**
     * 列表查询域名信息
     */
    @PostMapping(params = {"Action=DescribeBindDomainList"})
    public Object describeBindDomainList(@RequestBody DomainQueryDTO domainQueryDTO) {
        domainQueryDTO.setProjectId(ProjectTraceHolder.getProId());
        List<DomainInfoDTO> domainInfoList = domainInfoService.getBindDomainInfoList(domainQueryDTO);
        return apiReturn(new Result(domainInfoList));
    }

}
