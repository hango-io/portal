package org.hango.cloud.envoy.infra.dubbo.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/16
 */
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX)
public class DubboController extends AbstractController {

    public static final Logger logger = LoggerFactory.getLogger(DubboController.class);

    @Autowired
    private IDubboBindingService dubboBindingService;

    @Autowired
    private IDubboMetaService dubboMetaService;

    /**
     * 发布Dubbo路由功能
     *
     * @param dubboBindingDto
     * @return
     */
    @PostMapping(params = {"Action=PublishDubbo", "Version=2020-10-29"})
    public String publishEnvoyDubbo(@RequestBody @Validated DubboBindingDto dubboBindingDto) {
        logger.info("进行路由Dubbo转换发布操作，关联类型：{} ,关联ID：{} ", dubboBindingDto.getObjectType(), dubboBindingDto.getObjectId());
        ErrorCode errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            logger.info("出现错误，errMsg = {}", errorCode.getMessage());
            return apiReturn(errorCode);
        }
        long id = dubboBindingService.saveDubboInfo(dubboBindingDto);
        if (BaseConst.ERROR_RESULT == id) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        return apiReturnSuccess(id);
    }

    /**
     * 下线Dubbo路由功能
     *
     * @param objectId
     * @param objectType
     * @return
     */
    @GetMapping(params = {"Action=OfflineDubbo", "Version=2020-10-29"})
    public String offlineEnvoyDubbo(@RequestParam(value = "ObjectId") long objectId,
                                    @RequestParam(value = "ObjectType", required = false, defaultValue = ApiConst.ROUTE) String objectType) {
        logger.info("进行路由Dubbo转换下线操作，，关联类型：{} ,关联ID：{}", objectType, objectId);
        //操作审计记录资源名称
        if (dubboBindingService.deleteDubboInfo(objectId, objectType)) {
            return apiReturnSuccess(null);
        }
        return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 查看Dubbo路由功能
     *
     * @param objectId
     * @param objectType
     * @return
     */
    @GetMapping(params = {"Action=DescribePublishedDubbo", "Version=2020-10-29"})
    public String describeEnvoyDubbo(@RequestParam(value = "ObjectId") long objectId,
                                     @RequestParam(value = "ObjectType", required = false, defaultValue = ApiConst.ROUTE) String objectType) {
        logger.info("进行路由Dubbo转换查询操作，，关联类型：{} ,关联ID：{} ", objectType, objectId);
        DubboBindingDto dubboBindingDto = dubboBindingService.getByIdAndType(objectId, objectType);
        dubboBindingService.processMethodWorks(dubboBindingDto);
        return apiReturnSuccess(dubboBindingDto);
    }

    /**
     * 获取Dubbo Meta信息
     *
     * @param virtualGwId
     * @param igv
     * @param method
     * @return
     */
    @GetMapping(params = {"Action=DescribeDubboMeta", "Version=2021-10-30"})
    public String describeDubboMeta(@RequestParam(value = "VirtualGwId") long virtualGwId,
                                    @RequestParam(value = "Igv") String igv,
                                    @RequestParam(value = "method", required = false) String method
    ) {
        igv = StringUtils.removeEnd(igv, BaseConst.DUBBO_SERVICE_SUFFIX);
        logger.info("进行路由Dubbo 元信息查询操作，，网关ID：{} ,Igv信息：{} , 方法：{}", virtualGwId, igv, method);
        List<DubboMetaDto> dubboDto = dubboMetaService.findByIgv(virtualGwId, igv);
        if (CollectionUtils.isEmpty(dubboDto)) {
            dubboDto = dubboMetaService.refreshDubboMeta(virtualGwId, igv);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put(RESULT, dubboDto);
        result.put(TOTAL_COUNT, dubboDto.size());
        return apiReturnSuccess(result);
    }


    /**
     * 刷新Dubbo Meta信息
     *
     * @param virtualGwId
     * @param igv
     * @param method
     * @return
     */
    @GetMapping(params = {"Action=RefreshDubboMeta", "Version=2021-10-30"})
    public String refreshDubboMeta(@RequestParam(value = "VirtualGwId") long virtualGwId,
                                   @RequestParam(value = "Igv") String igv,
                                   @RequestParam(value = "method", required = false) String method) {
        logger.info("进行路由Dubbo 元信息查询操作，，网关ID：{} ,Igv信息：{} , 方法：{}", virtualGwId, igv, method);
        igv = StringUtils.removeEnd(igv, BaseConst.DUBBO_SERVICE_SUFFIX);
        List<DubboMetaDto> dubboDto = dubboMetaService.refreshDubboMeta(virtualGwId, igv);
        Map<String, Object> result = Maps.newHashMap();
        result.put(RESULT, dubboDto);
        result.put(TOTAL_COUNT, dubboDto.size());
        return apiReturnSuccess(result);
    }

}
