package org.hango.cloud.dashboard.apiserver.meta.errorcode;


/**
 * 该 ApiErrorCode 类仅用于网关服务使用！！！
 * <p>
 * 若 ErrorCodeEnum 枚举格式化时需要的参数是固定的，则定义一个静态变量
 * 若 ErrorCodeEnum 枚举格式化时需要的参数必须为动态传入的，则定义一个静态方法
 * <p>
 * 该类中的静态变量、静态方法的命名格式为：ErrorCodeEnum 枚举名称 + 具体参数名称
 */
public class CommonErrorCode extends ErrorCode {

    public static ErrorCode Success = new ErrorCode(ErrorCodeEnum.Success);
    public static ErrorCode Failed = new ErrorCode(ErrorCodeEnum.Failed);
    //FixMe wdc添加
    public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.InvalidBodyFormat);
    public static ErrorCode ParameterNull = new ErrorCode(ErrorCodeEnum.ParameterNull);
    public static ErrorCode MissingParameterServiceTag = new ErrorCode(ErrorCodeEnum.MissingParameter, "ServiceTag");
    public static ErrorCode InternalServerError = new ErrorCode(ErrorCodeEnum.InternalServerError);
    public static ErrorCode MethodNotAllowed = new ErrorCode(ErrorCodeEnum.MethodNotAllowed);
    public static ErrorCode MissingParameters = new ErrorCode(ErrorCodeEnum.MissingParameters);
    public static ErrorCode EmptyProjectId = new ErrorCode(ErrorCodeEnum.EmptyProjectId);
    public static ErrorCode EmptyTenantId = new ErrorCode(ErrorCodeEnum.EmptyTenantId);
    /**
     * API 数据模型相关
     */
    public static ErrorCode NoSuchAPI = new ErrorCode(ErrorCodeEnum.NoSuchAPI);
    public static ErrorCode NoSuchApiInterface = new ErrorCode(ErrorCodeEnum.NoSuchInterface);
    public static ErrorCode NoSuchModel = new ErrorCode(ErrorCodeEnum.NoSuchModel);
    public static ErrorCode NoSuchShuntStrategy = new ErrorCode(ErrorCodeEnum.NoSuchShuntStrategy);
    public static ErrorCode AuthError = new ErrorCode(ErrorCodeEnum.AuthError);
    public static ErrorCode NoPermission = new ErrorCode(ErrorCodeEnum.NoPermission);
    public static ErrorCode ServiceTagIsNull = new ErrorCode(ErrorCodeEnum.EmptyServiceTag);
    public static ErrorCode InvalidParamType = new ErrorCode(ErrorCodeEnum.InvalidParamType);
    public static ErrorCode HostUnreachable = new ErrorCode(ErrorCodeEnum.HostUnreachable);
    public static ErrorCode SynchronizedConfigError = new ErrorCode(ErrorCodeEnum.SynchronizedConfigError);
    /**
     * api基本信息，包括requestHeader，response header等
     */
    public static ErrorCode RepeatedParamName = new ErrorCode(ErrorCodeEnum.DuplicateParamName);
    public static ErrorCode NotPublishedService = new ErrorCode(ErrorCodeEnum.NotPublishedService);
    public static ErrorCode NotPublishedApi = new ErrorCode(ErrorCodeEnum.NotPublishedApi);
    public static ErrorCode AlreadyBindTrafficControl = new ErrorCode(ErrorCodeEnum.AlreadyBindTrafficControl);
    public static ErrorCode NoSuchParamType = new ErrorCode(ErrorCodeEnum.NoSuchParamType);
    public static ErrorCode NoSuchArrayDataType = new ErrorCode(ErrorCodeEnum.NoSuchArrayDataType);
    public static ErrorCode ErrorTimeRange = new ErrorCode(ErrorCodeEnum.ErrorTimeRange);
    public static ErrorCode QueryTimeIllegal = new ErrorCode(ErrorCodeEnum.QueryTimeIllegal);
    /**
     * 网关相关
     */
    public static ErrorCode GwNameAlreadyExist = new ErrorCode(ErrorCodeEnum.AlreadyExistGwName);
    public static ErrorCode GwAlreadyExist = new ErrorCode(ErrorCodeEnum.AlreadyExistGw);
    public static ErrorCode CannotDeleteGateway = new ErrorCode(ErrorCodeEnum.CannotDeleteGateway);
    public static ErrorCode NoSuchGwId = new ErrorCode(ErrorCodeEnum.NoSuchGwId);
    public static ErrorCode ExistRelationWithOtherProject = new ErrorCode(ErrorCodeEnum.ExistRelationWithOtherProject);
    public static ErrorCode ExistPublishedService = new ErrorCode(ErrorCodeEnum.ExistPublishedService);
    /**
     * 服务相关
     */
    public static ErrorCode CannotDownloadSDK = new ErrorCode(ErrorCodeEnum.CannotDownloadServiceSDK);
    public static ErrorCode NoSuchService = new ErrorCode(ErrorCodeEnum.NoSuchService);
    public static ErrorCode ServicePublishedUpdateLimit = new ErrorCode(ErrorCodeEnum.ServicePublishedUpdateLimit);
    public static ErrorCode ServiceTypeInvalid = new ErrorCode(ErrorCodeEnum.ServiceTypeInvalid);
    /**
     * 授权相关
     */
    public static ErrorCode NoGatewayExist = new ErrorCode(ErrorCodeEnum.NoGatewayExist);
    /**
     * grpc相关
     */
    public static ErrorCode DuplicateProtobufName = new ErrorCode(ErrorCodeEnum.DuplicateProtobufName);
    public static ErrorCode InvalidProtobufId = new ErrorCode(ErrorCodeEnum.InvalidProtobufId);
    public static ErrorCode InvalidProtobufName = new ErrorCode(ErrorCodeEnum.InvalidProtobufName);
    public static ErrorCode InvalidProtobufDesc = new ErrorCode(ErrorCodeEnum.InvalidProtobufDesc);
    public static ErrorCode InvalidProtobufContent = new ErrorCode(ErrorCodeEnum.InvalidProtobufContent);
    public static ErrorCode ParseProtobufFailed = new ErrorCode(ErrorCodeEnum.ParseProtobufFailed);
    public static ErrorCode DeleteOperationRefused = new ErrorCode(ErrorCodeEnum.DeleteOperationRefused);
    public static ErrorCode OfflineOperationRefused = new ErrorCode(ErrorCodeEnum.OfflineOperationRefused);
    public static ErrorCode InvalidPublishOperation = new ErrorCode(ErrorCodeEnum.InvalidPublishOperation);
    /**
     * grpc相关 2.0
     */
    public static ErrorCode InvalidPbServiceList = new ErrorCode(ErrorCodeEnum.InvalidPbServiceList);
    public static ErrorCode InvalidPluginManager = new ErrorCode(ErrorCodeEnum.InvalidPluginManager);
    public static ErrorCode ProcessProtobufFailed = new ErrorCode(ErrorCodeEnum.ProcessProtobufFailed);
    public static ErrorCode PublishProtobufFailed = new ErrorCode(ErrorCodeEnum.PublishProtobufFailed);
    public static ErrorCode CouldNotOfflineService = new ErrorCode(ErrorCodeEnum.CouldNotOfflineService);
    /**
     * webservice相关
     */
    public static ErrorCode IllegalBindingType = new ErrorCode(ErrorCodeEnum.IllegalBindingType);
    public static ErrorCode IllegalBindingParam = new ErrorCode(ErrorCodeEnum.IllegalBindingParam);
    public static ErrorCode NoSuchWsdlInfo = new ErrorCode(ErrorCodeEnum.NoSuchWsdlInfo);
    public static ErrorCode NoSuchWsParam = new ErrorCode(ErrorCodeEnum.NoSuchWsParam);
    public static ErrorCode BindingWsPluginFailed = new ErrorCode(ErrorCodeEnum.BindingWsPluginFailed);
    public static ErrorCode UnBindingWsPluginFailed = new ErrorCode(ErrorCodeEnum.UnBindingWsPluginFailed);
    public static ErrorCode RouteWsPluginNonExist = new ErrorCode(ErrorCodeEnum.RouteWsPluginNonExist);
    public static ErrorCode RenderWsTemplateFailed = new ErrorCode(ErrorCodeEnum.RenderWsTemplateFailed);
    public static ErrorCode CreateWsTemplateFailed = new ErrorCode(ErrorCodeEnum.CreateWsTemplateFailed);
    /**
     * 插件相关
     */
    public static ErrorCode InvalidPluginName = new ErrorCode(ErrorCodeEnum.InvalidPluginName);
    public static ErrorCode InvalidPluginVersion = new ErrorCode(ErrorCodeEnum.InvalidPluginVersion);
    public static ErrorCode InvalidGroovyFile = new ErrorCode(ErrorCodeEnum.InvalidGroovyFile);
    public static ErrorCode InvalidPluginContent = new ErrorCode(ErrorCodeEnum.InvalidPluginContent);
    public static ErrorCode InvalidPluginVariable = new ErrorCode(ErrorCodeEnum.InvalidPluginVariable);
    public static ErrorCode InvalidPluginOperation = new ErrorCode(ErrorCodeEnum.InvalidPluginOperation);
    public static ErrorCode InvalidPluginId = new ErrorCode(ErrorCodeEnum.InvalidPluginId);
    public static ErrorCode InvalidPluginNameAndVersion = new ErrorCode(ErrorCodeEnum.InvalidPluginNameAndVersion);
    public static ErrorCode DeletePluginInfoFailed = new ErrorCode(ErrorCodeEnum.DeletePluginInfoFailed);
    public static ErrorCode UpdatePluginInfoFailed = new ErrorCode(ErrorCodeEnum.UpdatePluginInfoFailed);
    public static ErrorCode ParsePluginInfoFailed = new ErrorCode(ErrorCodeEnum.ParsePluginInfoFailed);
    public static ErrorCode ReadTimeOut = new ErrorCode(ErrorCodeEnum.ReadTimeOut);
    public static ErrorCode ScrollTimeOut = new ErrorCode(ErrorCodeEnum.ScrollTimeOut);
    public static ErrorCode InvalidOperation = new ErrorCode(ErrorCodeEnum.InvalidOperation);
    public static ErrorCode RepeatParamName = new ErrorCode(ErrorCodeEnum.RepeatParamName);
    public static ErrorCode PublishTypeNotSupport = new ErrorCode(ErrorCodeEnum.PublishTypeNotSupport);
    public static ErrorCode MissingParameterParamName = new ErrorCode(ErrorCodeEnum.MissingParameter, "ParamName");
    public static ErrorCode MissingParameterParamType = new ErrorCode(ErrorCodeEnum.MissingParameter, "ParamType");
    public static ErrorCode MissingParameterParamMatchingMode = new ErrorCode(ErrorCodeEnum.MissingParameter, "ParamMatchingMode");
    public static ErrorCode MissingParameterServiceName = new ErrorCode(ErrorCodeEnum.MissingParameter, "ServiceName");
    public static ErrorCode MissingParameterQueryTime = new ErrorCode(ErrorCodeEnum.MissingParameter, "StartTime Or EndTime");
    public static ErrorCode MissingParameterGwId = new ErrorCode(ErrorCodeEnum.MissingParameter, "GwId");
    public static ErrorCode MissingParameterMetricType = new ErrorCode(ErrorCodeEnum.MissingParameter, "MetricType");
    public static ErrorCode MissingParameterServiceId = new ErrorCode(ErrorCodeEnum.MissingParameter, "ServiceId");
    public static ErrorCode MissingParameterGwAddr = new ErrorCode(ErrorCodeEnum.MissingParameter, "GwAddr");
    public static ErrorCode MissingDataSource = new ErrorCode(ErrorCodeEnum.MissingParameter, "DataSource");
    public static ErrorCode MissingParameterServiceAddr = new ErrorCode(ErrorCodeEnum.MissingParameter, "ServiceAddr");
    public static ErrorCode MissingParameterRegistryCenterAddr = new ErrorCode(ErrorCodeEnum.MissingParameter, "RegistryCenterAddr");
    public static ErrorCode MissingParameterApplicationName = new ErrorCode(ErrorCodeEnum.MissingParameter, "ApplicationName");
    public static ErrorCode MissingParameterPolicyName = new ErrorCode(ErrorCodeEnum.MissingParameter, "PolicyName");
    public static ErrorCode MissingParameterPolicyType = new ErrorCode(ErrorCodeEnum.MissingParameter, "PolicyType");
    public static ErrorCode MissingParameterWhiteList = new ErrorCode(ErrorCodeEnum.MissingParameter, "WhiteList");
    public static ErrorCode MissingParameterRuleId = new ErrorCode(ErrorCodeEnum.MissingParameter, "RuleId");
    public static ErrorCode RepeatServiceAddr = new ErrorCode(ErrorCodeEnum.RepeatValue, "ServiceAddr");
    public static ErrorCode RepeatInstanceName = new ErrorCode(ErrorCodeEnum.RepeatValue, "InstanceName");
    public static ErrorCode RepeatParamValue = new ErrorCode(ErrorCodeEnum.RepeatValue, "ParamValue");
    public static ErrorCode UpdateNotAllowedRuleName = new ErrorCode(ErrorCodeEnum.UpdateNotAllowed, "RuleName");
    public static ErrorCode CustomParamMappingInvalid = new ErrorCode(ErrorCodeEnum.CustomParamMappingInvalid);
    public static ErrorCode ServiceNotFound = new ErrorCode(ErrorCodeEnum.ServiceNotFound);
    public static ErrorCode CannotOfflineService = new ErrorCode(ErrorCodeEnum.CannotOfflineService);
    public static ErrorCode CannotOfflineGrpcService = new ErrorCode(ErrorCodeEnum.CannotOfflineGrpcService);
    public static ErrorCode ServiceNameAlreadyExist = new ErrorCode(ErrorCodeEnum.AlreadyExist, "ServiceName");
    public static ErrorCode TrafficColorRuleAlreadyExist = new ErrorCode(ErrorCodeEnum.TrafficColorRuleAlreadyExist);
    public static ErrorCode TrafficColorRuleNameAlreadyExist = new ErrorCode(ErrorCodeEnum.TrafficColorRuleNameAlreadyExist);
    public static ErrorCode TrafficColorRuleNameIsEmpty = new ErrorCode(ErrorCodeEnum.TrafficColorRuleNameIsEmpty);
    public static ErrorCode TrafficMatchNotSupport = new ErrorCode(ErrorCodeEnum.TrafficMatchNotSupport);
    public static ErrorCode NoSuchTrafficColorRule = new ErrorCode(ErrorCodeEnum.NoSuchTrafficColorRule);
    public static ErrorCode CannotDeleteOnlineTrafficColorRule = new ErrorCode(ErrorCodeEnum.CannotDeleteOnlineTrafficColorRule);
    public static ErrorCode ServiceTagAlreadyExist = new ErrorCode(ErrorCodeEnum.ServiceTagAlreadyExist);
    public static ErrorCode ApiAlreadyExist = new ErrorCode(ErrorCodeEnum.ApiAlreadyExist);
    public static ErrorCode InvalidCustomHeaderKey = new ErrorCode(ErrorCodeEnum.InvalidCustomHeaderKey);
    public static ErrorCode CustomHeaderNotMatchBounds = new ErrorCode(ErrorCodeEnum.NotMatchBounds, "CustomHeader");
    public static ErrorCode UpdateFailure = new ErrorCode(ErrorCodeEnum.UpdateFailure);
    public static ErrorCode FileIsEmpty = new ErrorCode(ErrorCodeEnum.FileIsEmpty);
    public static ErrorCode IllegalFileFormat = new ErrorCode(ErrorCodeEnum.IllegalFileFormat);
    public static ErrorCode MissingUploadedFile = new ErrorCode(ErrorCodeEnum.MissingUploadedFile);
    public static ErrorCode UpdateToGwFailure = new ErrorCode(ErrorCodeEnum.UpdateToGwFailure);
    public static ErrorCode SameNamePolicyExist = new ErrorCode(ErrorCodeEnum.SameNamePolicyExist);
    public static ErrorCode NoSuchGateway = new ErrorCode(ErrorCodeEnum.NoSuchGateway);
    public static ErrorCode NoSuchPolicy = new ErrorCode(ErrorCodeEnum.NoSuchPolicy);
    public static ErrorCode CannotBindingPolicy = new ErrorCode(ErrorCodeEnum.CannotBindingPolicy);
    public static ErrorCode CannotUpdatePolicyUntilUnbinding = new ErrorCode(ErrorCodeEnum.CannotUpdatePolicyUntilUnbinding);
    public static ErrorCode CannotDeletePolicyUntilUnbinding = new ErrorCode(ErrorCodeEnum.CannotDeletePolicyUntilUnbinding);
    public static ErrorCode PolicyBoundLimit = new ErrorCode(ErrorCodeEnum.PolicyBoundLimit);
    public static ErrorCode ServiceNotPublished = new ErrorCode(ErrorCodeEnum.ServiceNotPublished);
    public static ErrorCode ShuntWayBindingNotAvailable = new ErrorCode(ErrorCodeEnum.ShuntWayBindingNotAvailable);
    public static ErrorCode ServiceNotAllowUpdateInstance = new ErrorCode(ErrorCodeEnum.ServiceNotAllowUpdateInstance);
    public static ErrorCode NoSuchProject = new ErrorCode(ErrorCodeEnum.NoSuchProject);
    public static ErrorCode AlreadyExistAliasName = new ErrorCode(ErrorCodeEnum.AlreadyExistAliasName);
    public static ErrorCode InvalidInstanceName = new ErrorCode(ErrorCodeEnum.InvalidInstanceName);
    public static ErrorCode AlreadyExistRuleName = new ErrorCode(ErrorCodeEnum.AlreadyExistRuleName);
    public static ErrorCode InvalidModulusShuntInstanceList = new ErrorCode(ErrorCodeEnum.InvalidModulusShuntInstanceList);
    public static ErrorCode CannotModifyService = new ErrorCode(ErrorCodeEnum.CannotUpdateService);
    public static ErrorCode CannotDeleteOnlineService = new ErrorCode(ErrorCodeEnum.CannotDeleteOnlineService);
    public static ErrorCode CannotDeleteApiService = new ErrorCode(ErrorCodeEnum.CannotDeleteApiService);
    public static ErrorCode CannotDeleteOnlineApi = new ErrorCode(ErrorCodeEnum.CannotDeleteOnlineApi);
    public static ErrorCode CannotUpdateServiceName = new ErrorCode(ErrorCodeEnum.CannotUpdateServiceName);
    public static ErrorCode DubboServicePublishWayLimit = new ErrorCode(ErrorCodeEnum.DubboServicePublishWayLimit);
    public static ErrorCode DubboServiceParamLimit = new ErrorCode(ErrorCodeEnum.DubboServiceParamLimit);
    public static ErrorCode TooManyDimensions = new ErrorCode(ErrorCodeEnum.TooManyDimensions);
    public static ErrorCode ConsulOperationException = new ErrorCode(ErrorCodeEnum.ConsulOperationException);
    /**
     * 授权相关
     **/
    public static ErrorCode InvalidEnvId = new ErrorCode(ErrorCodeEnum.InvalidEnvId);
    public static ErrorCode InvalidProjectId = new ErrorCode(ErrorCodeEnum.InvalidProjectId);
    public static ErrorCode SameNameGatewayExists = new ErrorCode(ErrorCodeEnum.SameNameGatewayExist);
    public static ErrorCode SameNameGatewayClusterExists = new ErrorCode(ErrorCodeEnum.SameNameGatewayClusterExist);
    public static ErrorCode NoSuchRouteRule = new ErrorCode(ErrorCodeEnum.NoSuchRouteRule);
    public static ErrorCode InvalidDestinationService = new ErrorCode(ErrorCodeEnum.InvalidDestinationService);
    public static ErrorCode SameNameRouteRuleExist = new ErrorCode(ErrorCodeEnum.SameNameRouteRuleExist);
    public static ErrorCode SameParamRouteRuleExist = new ErrorCode(ErrorCodeEnum.SameParamRouteRuleExist);
    public static ErrorCode ServiceAlreadyPublished = new ErrorCode(ErrorCodeEnum.ServiceAlreadyPublished);
    public static ErrorCode RouteRuleAlreadyPublished = new ErrorCode(ErrorCodeEnum.RouteRuleAlreadyPublished);
    public static ErrorCode MirrorByRouteRule = new ErrorCode(ErrorCodeEnum.MirrorByRouteRule);
    public static ErrorCode RouteRuleNotPublished = new ErrorCode(ErrorCodeEnum.RouteRuleNotPublished);
    public static ErrorCode RouteRuleServiceNotMatch = new ErrorCode(ErrorCodeEnum.RouteRuleServiceNotMatch);

    public static ErrorCode RouteHasTrafficMarkRules = new ErrorCode(ErrorCodeEnum.RouteHasTrafficMarkRules);
    public static ErrorCode RouteRuleMethodInvalid = new ErrorCode(ErrorCodeEnum.RouteRuleMethodInvalid);
    public static ErrorCode CannotDeleteRouteRuleService = new ErrorCode(ErrorCodeEnum.CannotDeleteRouteRuleService);
    public static ErrorCode SortKeyInvalid = new ErrorCode(ErrorCodeEnum.SortKeyInvalid);
    public static ErrorCode SortValueInvalid = new ErrorCode(ErrorCodeEnum.SortValueInvalid);
    public static ErrorCode NoSuchPluginBinding = new ErrorCode((ErrorCodeEnum.NoSuchPluginBinding));
    public static ErrorCode IllegalPluginType = new ErrorCode((ErrorCodeEnum.IllegalPluginType));
    public static ErrorCode CannotDuplicateBinding = new ErrorCode(ErrorCodeEnum.CannotDuplicateBinding);
    public static ErrorCode CannotDuplicateBindingAuthPlugin = new ErrorCode(ErrorCodeEnum.CannotDuplicateBindingAuthPlugin);
    public static ErrorCode InvalidApiPath = new ErrorCode(ErrorCodeEnum.InvalidApiPath);
    public static ErrorCode InvalidActiveSwitch = new ErrorCode(ErrorCodeEnum.InvalidActiveSwitch);
    public static ErrorCode InvalidPassiveSwitch = new ErrorCode(ErrorCodeEnum.InvalidPassiveSwitch);
    public static ErrorCode InvalidTimeout = new ErrorCode(ErrorCodeEnum.InvalidTimeout);
    public static ErrorCode InvalidHttpStatusCode = new ErrorCode(ErrorCodeEnum.InvalidHttpStatusCode);
    public static ErrorCode InvalidHealthyInterval = new ErrorCode(ErrorCodeEnum.InvalidHealthyInterval);
    public static ErrorCode InvalidHealthyThreshold = new ErrorCode(ErrorCodeEnum.InvalidHealthyThreshold);
    public static ErrorCode InvalidUnHealthyInterval = new ErrorCode(ErrorCodeEnum.InvalidUnHealthyInterval);
    public static ErrorCode InvalidUnHealthyThreshold = new ErrorCode(ErrorCodeEnum.InvalidUnHealthyThreshold);
    public static ErrorCode InvalidConsecutiveErrors = new ErrorCode(ErrorCodeEnum.InvalidConsecutiveErrors);
    public static ErrorCode InvalidBaseEjectionTime = new ErrorCode(ErrorCodeEnum.InvalidBaseEjectionTime);
    public static ErrorCode InvalidMaxEjectionPercent = new ErrorCode(ErrorCodeEnum.InvalidMaxEjectionPercent);
    public static ErrorCode NoRouteRulePath = new ErrorCode(ErrorCodeEnum.NoRouteRulePath);
    public static ErrorCode RouteRuleContainsNginxCapture = new ErrorCode(ErrorCodeEnum.RouteRuleContainsNginxCapture);
    public static ErrorCode NotModifyPriority = new ErrorCode(ErrorCodeEnum.NotModifyPriority);
    public static ErrorCode NoSuchPlugin = new ErrorCode(ErrorCodeEnum.NoSuchPlugin);
    public static ErrorCode NotModifyRouteRuleName = new ErrorCode(ErrorCodeEnum.NotModifyRouteRuleName);
    public static ErrorCode InvalidTotalWeight = new ErrorCode(ErrorCodeEnum.InvalidTotalWeight);
    public static ErrorCode BackendServiceDifferent = new ErrorCode(ErrorCodeEnum.BackendServiceDifferent);
    /**
     * 负载均衡相关
     */
    public static ErrorCode InvalidLoadBanlanceType = new ErrorCode(ErrorCodeEnum.InvalidLoadBanlanceType);
    public static ErrorCode InvalidSimpleLoadBanlanceType = new ErrorCode(ErrorCodeEnum.InvalidSimpleLoadBanlanceType);
    public static ErrorCode InvalidConsistentHashObject = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashObject);
    public static ErrorCode InvalidConsistentHashType = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashType);
    public static ErrorCode InvalidConsistentHashHttpCookieObject = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpCookieObject);
    public static ErrorCode InvalidConsistentHashHttpCookieName = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpCookieName);
    public static ErrorCode InvalidConsistentHashHttpCookieTtl = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpCookieTtl);
    public static ErrorCode InvalidConsistentHashHttpHeaderName = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpHeaderName);
    public static ErrorCode InvalidConsistentHashSourceIP = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashSourceIP);
    public static ErrorCode InvalidHttp1MaxPendingRequests = new ErrorCode(ErrorCodeEnum.InvalidHttp1MaxPendingRequests);
    public static ErrorCode InvalidHttp2MaxRequests = new ErrorCode(ErrorCodeEnum.InvalidHttp2MaxRequests);
    public static ErrorCode InvalidIdleTimeout = new ErrorCode(ErrorCodeEnum.InvalidIdleTimeout);
    public static ErrorCode InvalidMaxRequestsPerConnection = new ErrorCode(ErrorCodeEnum.InvalidMaxRequestsPerConnection);
    public static ErrorCode InvalidMaxConnections = new ErrorCode(ErrorCodeEnum.InvalidMaxConnections);
    public static ErrorCode InvalidConnectTimeout = new ErrorCode(ErrorCodeEnum.InvalidConnectTimeout);
    public static ErrorCode DuplicatedSubsetName = new ErrorCode(ErrorCodeEnum.DuplicatedSubsetName);


    //-----------------------Envoy 网关部分ErrorCode-------------------//
    public static ErrorCode InvalidSubsetName = new ErrorCode(ErrorCodeEnum.InvalidSubsetName);
    public static ErrorCode InvalidSubsetStaticAddr = new ErrorCode(ErrorCodeEnum.InvalidSubsetStaticAddr);
    public static ErrorCode DuplicatedSubsetStaticAddr = new ErrorCode(ErrorCodeEnum.DuplicatedSubsetStaticAddr);
    public static ErrorCode DuplicatedStaticAddr = new ErrorCode(ErrorCodeEnum.DuplicatedStaticAddr);
    /**
     * 注册中心相关
     */
    public static ErrorCode InvalidEurekaAddress = new ErrorCode(ErrorCodeEnum.InvalidEurekaAddress);
    public static ErrorCode ProjectNotAssociatedGateway = new ErrorCode(ErrorCodeEnum.ProjectNotAssociatedGateway);
    public static ErrorCode NoSuchVirtualHost = new ErrorCode(ErrorCodeEnum.NoSuchVirtualHost);
    public static ErrorCode VirtualHostAlreadyExist = new ErrorCode(ErrorCodeEnum.VirtualHostAlreadyExist);
    public static ErrorCode SameNamePluginTemplateExist = new ErrorCode(ErrorCodeEnum.SameNamePluginTemplateExist);
    public static ErrorCode NoSuchPluginTemplate = new ErrorCode(ErrorCodeEnum.NoSuchPluginTemplate);
    public static ErrorCode CannotUpdatePlugin = new ErrorCode(ErrorCodeEnum.CannotUpdatePlugin);
    /**
     * 集成相关
     */
    public static ErrorCode AlreadyExistIntegrationName = new ErrorCode(ErrorCodeEnum.AlreadyExistIntegrationName);
    public static ErrorCode NoSuchIntegration = new ErrorCode(ErrorCodeEnum.NoSuchIntegration);
    public static ErrorCode CannotUpdateIntegrationRule = new ErrorCode(ErrorCodeEnum.CannotUpdateIntegrationRule);
    public static ErrorCode CannotDeleteIntegration = new ErrorCode(ErrorCodeEnum.CannotDeleteIntegration);
    public static ErrorCode IntegrationAlreadyPublished = new ErrorCode(ErrorCodeEnum.IntegrationAlreadyPublished);
    public static ErrorCode NoSuchSchema = new ErrorCode(ErrorCodeEnum.NoSuchSchema);
    public static ErrorCode IntegrationNotPublished = new ErrorCode(ErrorCodeEnum.IntegrationNotPublished);
    public static ErrorCode FailedToPublishServiceOrRoute = new ErrorCode(ErrorCodeEnum.FailedToPublishServiceOrRoute);
    public static ErrorCode FailedToPublishIntegration = new ErrorCode(ErrorCodeEnum.FailedToPublishIntegration);
    public static ErrorCode FailedToOfflineServiceOrRoute = new ErrorCode(ErrorCodeEnum.FailedToOfflineServiceOrRoute);

    private CommonErrorCode(ErrorCodeEnum errorCodeEnum, String[] args) {
        super(errorCodeEnum, args);
    }

    public static ErrorCode ApiConflictOccurs(String errorMessage) {
        return new ErrorCode(ErrorCodeEnum.ApiConflictOccurs, errorMessage);
    }

    public static ErrorCode TimeRangeTooLarge(String days) {
        return new ErrorCode(ErrorCodeEnum.TimeRangeTooLarge, days);
    }

    public static ErrorCode InvalidParameterValue(Object value, String name) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, name, value.toString());
    }

    public static ErrorCode InvalidParameterValue(Object value, String name, String message) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, name, String.valueOf(value), message);
    }

    public static ErrorCode InvalidJSONFormat(String message) {
        return new ErrorCode(ErrorCodeEnum.JsonParseException, message);
    }

    public static ErrorCode MissingParameter(String paramName) {
        return new ErrorCode(ErrorCodeEnum.MissingParameter, paramName);
    }

    //FixMe wdc添加
    public static ErrorCode InvalidParameterValueServiceId(String serviceId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ServiceId", serviceId);
    }

    public static ErrorCode InvalidParameterValueParamTypeId(String paramTypeId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ParamTypeId", paramTypeId);
    }

    public static ErrorCode InvalidParameterValueModelId(String modelId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ModelId", modelId);
    }

    public static ErrorCode InvalidParameterValueDate(String date) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Date", date);
    }

    public static ErrorCode InvalidParameterValueModelName(String modelName) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ModelName", modelName);
    }

    public static ErrorCode InvalidParameterValueGwId(String gwId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "GwId", gwId);
    }

    public static ErrorCode InvalidParameterValueId(String id) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Id", id);
    }

    public static ErrorCode InvalidParameterValueErrorType(String errorType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ErrorType", errorType);
    }

    public static ErrorCode DataSourceError(String datasource) {
        return new ErrorCode(ErrorCodeEnum.DataSourceError, datasource);
    }

    public static ErrorCode ResourceDownloadFailed(String url) {
        return new ErrorCode(ErrorCodeEnum.ResourceDownloadFailed, url);
    }

    public static ErrorCode IllegalWsdlFormat(String message) {
        return new ErrorCode(ErrorCodeEnum.IllegalWsdlFormat, message);
    }

    public static ErrorCode InvalidParameterApiId(String apiId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ApiId", apiId);
    }

    public static ErrorCode ShuntWayError(String shuntWay) {
        return new ErrorCode(ErrorCodeEnum.ShuntWayError, shuntWay);
    }

    public static ErrorCode InvalidServiceProxy(String service, String gateway) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, service, gateway);
    }

    public static ErrorCode InvalidParameterApiPath(String apiPath) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Path", apiPath);
    }

    public static ErrorCode InvalidParameterApiName(String apiName) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ApiName", apiName);
    }

    public static ErrorCode InvalidParameterMethod(String method) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Method", method);
    }

    public static ErrorCode InvalidParameterApiType(String apiType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Type", apiType);
    }

    public static ErrorCode InvalidParameterApiDesc(String desc) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Desc", desc);
    }

    public static ErrorCode InvalidParameterServiceId(String serviceId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ServiceId", serviceId);
    }

    public static ErrorCode InvalidParameterServiceName(String serviceName) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ServiceName", serviceName);
    }

    public static ErrorCode InvalidParameterGwId(Object gwId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "GwId", String.valueOf(gwId));
    }

    public static ErrorCode InvalidParameterWeight(String weight) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Weight", weight);
    }

    public static ErrorCode InvalidParameterCaseName(String caseName) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "CaseName", caseName);
    }

    public static ErrorCode InvalidParameterParamValue(String paramValuea) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ParamValue", paramValuea);
    }

    public static ErrorCode InvalidParameterCaseId(String caseId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "CaseId", caseId);
    }

    public static ErrorCode InvalidParameterMetricType(String metricType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "MetricType", metricType);
    }

    public static ErrorCode InvalidParameterDimensionType(String dimensionType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "DimensionType", dimensionType);
    }

    public static ErrorCode InvalidParameterTaskId(String taskId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "TaskId", taskId);
    }

    public static ErrorCode InvalidAddrAcquireStrategy(String addrAcquireStrategy) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "AddrAcquireStrategy", addrAcquireStrategy);
    }

    public static ErrorCode InvalidDuration(String duration) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Duration", duration);
    }

    public static ErrorCode InvalidCustomHeader(String customHeader) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "CustomHeader", customHeader);
    }

    public static ErrorCode InvalidLoadBalanceRuleId(long loadBalanceRuleId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "LoadBalanceRuleId", String.valueOf(loadBalanceRuleId));
    }

    public static ErrorCode MissingParameterFiled(String param) {
        return new ErrorCode(ErrorCodeEnum.MissingParameter, param);
    }

    public static ErrorCode InvalidParameterServiceAddr(String serviceAddr) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ServiceAddr", serviceAddr);
    }

    public static ErrorCode InvalidParameterRegistryAddr(String registryAddr) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "RegistryAddr", registryAddr);
    }

    public static ErrorCode InvalidParameterRegistryCenterType(String registryCenterType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "RegistryCenterType", registryCenterType);
    }

    public static ErrorCode InvalidParameterRegex(long regex) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "Regex", String.valueOf(regex));
    }

    public static ErrorCode InvalidParameterRespCode(String respCode) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "RespCode", String.valueOf(respCode));
    }

    public static ErrorCode InvalidParameterPolicyType(String policyType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "PolicyType", policyType);
    }

    public static ErrorCode InvalidParameterWhiteList(String whiteList) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "WhiteList", whiteList);
    }

    public static ErrorCode InvalidParameterBindingObjectId(String bindingObjectId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "BindingObjectId", bindingObjectId);
    }

    public static ErrorCode InvalidParameterBindingObjectType(String bindingObjectType) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "BindingObjectType", bindingObjectType);
    }

    public static ErrorCode InvalidParameterPolicyName(String policyName) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "PolicyName", policyName);
    }

    public static ErrorCode InvalidParameterProjectId(String projectId) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, "ProjectId", projectId);
    }

    public static ErrorCode InvalidParameter(String object, String name) {
        return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, name, object);
    }

    public static ErrorCode NotMatchBounds(String arg) {
        return new ErrorCode(ErrorCodeEnum.NotMatchBounds, arg);
    }

    public static ErrorCode RouteRuleAlreadyPublished(String errorGwName) {
        return new ErrorCode(ErrorCodeEnum.RouteRuleAlreadyPublishedToGw, errorGwName);
    }

    public static ErrorCode BatchPublishRouteError(String errorGwName) {
        return new ErrorCode(ErrorCodeEnum.BatchPublishRouteError, errorGwName);
    }

    public static ErrorCode SubsetUsedByRouteRule(String routeRuleName) {
        return new ErrorCode(ErrorCodeEnum.SubsetUsedByRouteRule, routeRuleName);
    }
}
