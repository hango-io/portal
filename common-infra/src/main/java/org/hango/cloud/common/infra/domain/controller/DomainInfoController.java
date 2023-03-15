package org.hango.cloud.common.infra.domain.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainRefreshResult;
import org.hango.cloud.common.infra.domain.pojo.DomainInfoPO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;

/**
 * @Author zhufengwei
 * @Date 2022/10/24
 * 域名管理控制台
 */
@Slf4j
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2022-10-30"})
@Validated
public class DomainInfoController extends AbstractController {

    @Autowired
    private IDomainInfoService domainInfoService;

    public static final String REFRESH_RESULT = "RefreshResult";


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
    @RequestMapping(params = {"Action=UpdateDomainInfo"}, method = RequestMethod.POST)
    public Object updateDomainInfo(@RequestBody DomainInfoDTO domainInfoDTO) {
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
    @RequestMapping(params = {"Action=DescribeDomainList"}, method = RequestMethod.GET)
    public Object describeDomainList(@RequestParam(value = "Host", required = false) String host,
                                     @RequestParam(value = "Limit", required = false, defaultValue = "20") int limit,
                                     @RequestParam(value = "Offset", required = false, defaultValue = "0") int offset) {
        log.info("DescribeDomainList Host:{}", host);
        long proId = ProjectTraceHolder.getProId();
        Page<DomainInfoPO> page = domainInfoService.getDomainInfoPage(proId, host, offset, limit);
        List<DomainInfoDTO> domainInfoDTOS = page.getRecords().stream().map(domainInfoService::toView).collect(Collectors.toList());
        return apiReturn(new PageResult(domainInfoDTOS, page.getTotal()));
    }

    /**
     * 查询域名列表
     */
    @RequestMapping(params = {"Action=DescribeDomains"}, method = RequestMethod.GET)
    public Object describeDomainLists(@RequestParam(value = "ProjectId") long projectId,
                                      @RequestParam(value = "Protocol") String protocol,
                                      @RequestParam(value = "Env", required = false) String env) {
        log.info("DescribeDomains proId:{},protocol:{}", projectId, protocol);
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(Collections.singletonList(projectId), protocol, env);
        return apiReturn(new Result(domainInfos));
    }


    /**
     * 获取待刷新配置
     */
    @RequestMapping(params = {"Action=DescribeDomainRefreshResult"}, method = RequestMethod.GET)
    public Object describeDomainRefreshResult() {
        long proId = ProjectTraceHolder.getProId();
        log.info("DescribeDomainRefreshResult proId:{}", proId);
        List<DomainRefreshResult> domainRefreshResult = domainInfoService.getDomainRefreshResult(proId);
        return apiReturn(new Result(domainRefreshResult));
    }


    /**
     * 刷新配置
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=RefreshDomain"}, method = RequestMethod.GET)
    public Object refreshDomain() {
        long proId = ProjectTraceHolder.getProId();
        log.info("DescribeDomainRefreshResult proId:{}", proId);
        List<DomainRefreshResult> results = domainInfoService.refreshDomain(proId);
        Map<String, Object> map = new HashMap<>();
        if (CollectionUtils.isEmpty(results)){
            map.put(REFRESH_RESULT, SUCCESS);
        }else {
            map.put(REFRESH_RESULT, FAILED);
            map.put(DATA, results);
        }
        return apiReturn(new Result(map));
    }
}
