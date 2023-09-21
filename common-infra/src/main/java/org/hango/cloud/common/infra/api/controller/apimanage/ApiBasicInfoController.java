package org.hango.cloud.common.infra.api.controller.apimanage;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.ApiManageConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceDataDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiListDto;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.HANGO_DASHBOARD_PREFIX;

/**
 * api基本信息管理，包括API名称，标识等基本信息
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = HANGO_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ApiBasicInfoController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(ApiBasicInfoController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;

    /**
     * 创建新的API
     */
    @RequestMapping(params = {"Action=CreateApi"}, method = RequestMethod.POST)
    public Object addApi(@Validated @RequestBody ApiInfoBasicDto apiInfoBasicDto) {
        logger.info("创建API，apiInfoBasicDto:{}", apiInfoBasicDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API, null, apiInfoBasicDto.getApiName());


        //服务id校验
        if (serviceProxyService.get(apiInfoBasicDto.getServiceId()) == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        ApiErrorCode errorCode = apiInfoService.checkParamApiBasicDto(apiInfoBasicDto);
        //参数校验
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        long apiId = apiInfoService.addApiInfos(apiInfoBasicDto, apiInfoBasicDto.getType());
        resource.setResourceId(apiId);
        return apiReturnSuccess(apiId);

    }

    /**
     * 根据Id查询api基本信息
     */
    @RequestMapping(params = {"Action=DescribeApiById"}, method = RequestMethod.GET)
    public Object getApiInfo(@RequestParam(value = "ApiId") long apiId) {
        logger.info("根据apiId:{},查询api基本信息", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            logger.info("根据apiId:{},查询api基本信息，接口不存在", apiId);
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        ApiInfoBasicDto apiInfoBasicDto = BeanUtil.copy(apiInfo, ApiInfoBasicDto.class);
        Map<String, Object> result = Maps.newHashMap();
        result.put("ApiInfoBasic", apiInfoBasicDto);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 更新API信息
     *
     * @param apiInfoBasicDto api基本信息dto
     * @return 更新结果
     */
    @RequestMapping(params = {"Action=UpdateApi"}, method = RequestMethod.POST)
    public Object updateApi(@Validated @RequestBody ApiInfoBasicDto apiInfoBasicDto) {
        logger.info("更新API基本信息，apiInfoBasicBto:{}", apiInfoBasicDto);

        //操作审计记录资源名称
        //AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, apiInfoBasicDto.getId(), apiInfoBasicDto.getApiName()));

        ApiInfo apiInfo = apiInfoService.getApiById(apiInfoBasicDto.getId());
        if (apiInfo == null) {
            logger.info("更新apiId:{},查询api基本信息不存在", apiInfoBasicDto.getId());
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }

        //服务id校验
        if (serviceProxyService.get(apiInfoBasicDto.getServiceId()) == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }

        ApiErrorCode errorCode = apiInfoService.checkParamApiBasicDto(apiInfoBasicDto);
        //参数校验
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        //看着整齐，没有进行service抽取
        apiInfo.setModifyDate(System.currentTimeMillis());
        apiInfo.setApiName(apiInfoBasicDto.getApiName());
        apiInfo.setApiPath(apiInfoBasicDto.getApiPath());
        apiInfo.setApiMethod(apiInfoBasicDto.getApiMethod());
        apiInfo.setAliasName(apiInfoBasicDto.getAliasName());
        apiInfo.setDescription(apiInfoBasicDto.getDescription());
        apiInfo.setDocumentStatusId(apiInfoBasicDto.getDocumentStatusId());
        String regex = apiInfo.getApiPath().replaceAll("\\{[^}]*\\}", "*");
        apiInfo.setRegex(regex);

        //操作审计
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{名称: " + apiInfo.getApiName() + ", 路径：" + apiInfo.getApiPath() + ", 方法：" + apiInfo.getApiMethod() + ", 描述：" + apiInfo.getDescription()
                + ", 状态：" + apiInfo.getStatus()).append("}");
        apiInfoService.updateApi(apiInfo);
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    /**
     * 根据apiId删除API基本信息
     *
     * @param apiId 接口APIid
     * @return 删除结果
     */
    @RequestMapping(params = {"Action=DeleteApiById"}, method = RequestMethod.GET)
    public Object deleteApi(@RequestParam(value = "ApiId") long apiId) {
        logger.info("请求删除apiId:{}的接口信息", apiId);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API, apiId, null);

        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo != null) {
            resource.setResourceName(apiInfo.getApiName());
            if (NumberUtils.INTEGER_ONE.equals(NumberUtils.toInt(apiInfo.getStatus()))) {
                logger.info("接口未下线，不能进行删除");
                return apiReturn(org.hango.cloud.envoy.advanced.bakup.apiserver.meta.errorcode.CommonErrorCode.CANNOT_DELETE_ONLINE_API);
            }
        }
        apiInfoService.deleteApi(apiId);
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    /**
     * 分页获取API
     *
     * @param pattern       模糊匹配pattern
     * @param offset        分页offset
     * @param limit         分页limit
     * @param serviceId     服务id，支持全部
     * @param apiDocumentId API状态，默认为全部状态
     * @return APIList
     */
    @RequestMapping(params = {"Action=DescribeApiListByLimit"}, method = RequestMethod.GET)
    public Object apiList(@RequestParam(value = "Pattern", required = false) String pattern,
                          @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                          @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                          @RequestParam(value = "ServiceId", required = false, defaultValue = "0") long serviceId,
                          @RequestParam(value = "ApiDocumentStatus", required = false, defaultValue = "0") long apiDocumentId) {
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        //没有传服务id，获取项目下的所有服务
        List<ApiInfo> apiInfos = apiInfoService.findAllApiByProjectLimit(ProjectTraceHolder.getProId(), serviceId, apiDocumentId, pattern, offset, limit);
        List<ApiListDto> apiListDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiInfos)) {
            apiInfos.forEach(apiInfo -> {
                ApiInfoBasicDto apiInfoBasicDto = BeanUtil.copy(apiInfo, ApiInfoBasicDto.class);
                ApiListDto apiListDto = BeanUtil.copy(apiInfo, ApiListDto.class);
                apiListDto.setApiInfoBasicDto(apiInfoBasicDto);
                apiListDto.setStatus(apiInfo.getStatus());
                apiListDtos.add(apiListDto);
            });
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("TotalCount", apiInfoService.getCountByProjectOrService(ProjectTraceHolder.getProId(), serviceId, apiDocumentId, pattern));
        result.put("ApiList", apiListDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

}
