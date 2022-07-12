package org.hango.cloud.dashboard.apiserver.web.controller.apimanage;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiListDto;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.OperationLog;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IOperationLogService;
import org.hango.cloud.gdashboard.api.service.IWebServiceParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * api基本信息管理，包括API名称，标识等基本信息
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ApiBasicInfoController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(ApiBasicInfoController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IOperationLogService operationLogService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IWebServiceParamService webServiceParamService;

    /**
     * 创建新的API
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateApi"}, method = RequestMethod.POST)
    @Audit(eventName = "CreteApi", description = "创建API")
    public Object addApi(@Validated @RequestBody ApiInfoBasicDto apiInfoBasicDto) {
        logger.info("创建API，apiInfoBasicDto:{}", apiInfoBasicDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, null, apiInfoBasicDto.getApiName());
        AuditResourceHolder.set(resource);

        //服务id校验
        if (serviceInfoService.getServiceByServiceId(apiInfoBasicDto.getServiceId()) == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        ApiErrorCode errorCode = apiInfoService.checkParamApiBasicDto(apiInfoBasicDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        long apiId = apiInfoService.addApiInfos(apiInfoBasicDto, apiInfoBasicDto.getType());
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setObjectId(apiId);
            operationLog.setType(Const.API);
            operationLog.setCreateDate(System.currentTimeMillis());
            operationLog.setEmail(UserPermissionHolder.getAccountId());
            operationLog.setOperation(UserPermissionHolder.getAccountId() + "创建了该API");
            operationLogService.addApiOperationLog(operationLog);
        } catch (Exception e) {
            logger.error("记录用户创建API时发生异常！{}", e);
        }
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
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        ApiInfoBasicDto apiInfoBasicDto = BeanUtil.copy(apiInfo, ApiInfoBasicDto.class);
        Map<String, Object> result = new HashMap<>();
        result.put("ApiInfoBasic", apiInfoBasicDto);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * 更新API信息
     *
     * @param apiInfoBasicDto api基本信息dto
     * @return 更新结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateApi"}, method = RequestMethod.POST)
    @Audit(eventName = "UpdateApi", description = "修改API")
    public Object updateApi(@Validated @RequestBody ApiInfoBasicDto apiInfoBasicDto) {
        logger.info("更新API基本信息，apiInfoBasicBto:{}", apiInfoBasicDto);

        //操作审计记录资源名称
        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, apiInfoBasicDto.getId(), apiInfoBasicDto.getApiName()));

        ApiInfo apiInfo = apiInfoService.getApiById(apiInfoBasicDto.getId());
        if (apiInfo == null) {
            logger.info("更新apiId:{},查询api基本信息不存在", apiInfoBasicDto.getId());
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }

        //服务id校验
        if (serviceInfoService.getServiceByServiceId(apiInfoBasicDto.getServiceId()) == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }

        ApiErrorCode errorCode = apiInfoService.checkParamApiBasicDto(apiInfoBasicDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
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
        String operation = UserPermissionHolder.getAccountId() + "修改了API，修改为:" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiInfo.getId(), Const.API, operation);

        apiInfoService.updateApi(apiInfo);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 根据apiId删除API基本信息
     *
     * @param apiId 接口APIid
     * @return 删除结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteApiById"}, method = RequestMethod.GET)
    @Audit(eventName = "DeleteApi", description = "删除API")
    public Object deleteApi(@RequestParam(value = "ApiId") long apiId) {
        logger.info("请求删除apiId:{}的接口信息", apiId);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, apiId, null);
        AuditResourceHolder.set(resource);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo != null) {
            resource.setResourceName(apiInfo.getApiName());
            if (NumberUtils.INTEGER_ONE.equals(NumberUtils.toInt(apiInfo.getStatus()))) {
                logger.info("接口未下线，不能进行删除");
                return apiReturn(CommonErrorCode.CannotDeleteOnlineApi);
            }
        }
        //删除webservice param
        webServiceParamService.deleteWebserviceParam(apiId);
        apiInfoService.deleteApi(apiId);
        operationLogService.addApiOperationLog(operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiId, Const.API, UserPermissionHolder.getAccountId() + "删除了该API"));
        return apiReturn(CommonErrorCode.Success);
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
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
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
        Map<String, Object> result = new HashMap<>();
        result.put("TotalCount", apiInfoService.getCountByProjectOrService(ProjectTraceHolder.getProId(), serviceId, apiDocumentId, pattern));
        result.put("ApiList", apiListDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

}
