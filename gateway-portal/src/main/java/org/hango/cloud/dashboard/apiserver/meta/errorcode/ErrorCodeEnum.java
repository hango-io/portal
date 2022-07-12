package org.hango.cloud.dashboard.apiserver.meta.errorcode;


/**
 * 新版 OpenAPI 后使用的 ApiErrorCode 枚举
 * <p>
 * 该枚举为基础枚举，不能直接用于返回，需要使用 ApiErrorCode 进行封装
 *
 * @see ErrorCodeEnum
 * <p>
 * After 2017.09.20
 */
public enum ErrorCodeEnum {
    Success("Success", "Success", "处理成功", 200),
    Failed("Failed", "Failed", "操作失败", 400),

    InvalidFormat("InvalidFormat", "The format of the input parameter %s is illegal.", "参数 %s 的格式非法", 400),
    InvalidBodyFormat("InvalidFormat", "The format of the request body is illegal.", "请求体格式非法", 400),
    RequestExpired("RequestExpired", "Request has expired.", "请求已过期", 400),
    MissingParameter("MissingParameter", "The required input parameter %s for processing this request is not supplied.", "参数  %s 缺失", 400),
    IllegalAccessKey("IllegalAccessKey", "The access key you provided is illegal.", "AccessKey 无效", 400),
    AccessKeyNotFound("AccessKeyNotFound", "We can not found the access key you provided.", "AccessKey 不存在", 401),
    InternalServerError("InternalServerError", "Internal server error.", "服务器内部错误", 500),
    ReadTimeOut("ReadTimeOut", "read time out.", "查询超时", 400),
    ScrollTimeOut("ScrollTimeOut", "scroll time out.", "审计调用查询失败", 400),
    InvalidParameterValue("InvalidParameterValue", "The parameter %s cannot accept value %s.", "参数 %s 的值 %s 非法", 400),
    SignatureDoesNotMatch("SignatureDoesNotMatch", "The request signature we calculated does not match the signature you provided.", "签名不匹配", 403),
    InvalidAuthorizationInfo("InvalidAuthorizationInfo", "The authorization info you provided is invalid.", "认证信息无效", 400),
    OutOfBounds("OutOfBounds", "%s is out of bounds", "%s超过限制", 400),
    UpdateNotAllowed("UpdateNotAllowed", "Parameter %s update not allowed.", "参数 %s 不允许被修改", 405),

    RepeatValue("RepeatValue", "%s value is repeat", "%s值重复", 400),
    DryRunOperation("DryRunOperation", "The quest would have successed, but the DryRun parameter was used.", "签名认证通过，但是使用了 DryRun 参数", 400),
    ApiFreqOutOfLimit("ApiFreqOutOfLimit", "Api freq out of limit.", "访问频率过高，请稍后再试", 403),

    // 用于 nce-api 模块没有找到用户信息时返回
    AuthError("AuthError", "There is no auth info, you can not access.", "没有认证信息，无权访问。", 403),

    // 用于Namespace
    NameExistAndDeleting("NameExistAndDeleting", "Name %s already exist and is deleting, please retry later.", "名称%s 已存在且正在删除中，请稍后再试", 400),

    // 如下几个枚举用于异常处理
    MethodNotAllowed("MethodNotAllow", "Http method not allowed.", "http 方法不支持", 405),
    InvalidParameters("InvalidParameters", "Invalid parameters.", "参数无效", 400),
    UnSupportedMediaType("UnSupportedMediaType", "Unsupported media type.", "不支持的媒体类型", 415),
    UnProcessableEntity("UnProcessableEntity", "Unprocessable entity.", "不可处理的实体", 422),
    MissingParameters("MissingParameters", "Missing parameters.", "参数缺失", 400),

    NoSuchAPI("NoSuchApi", "No such api.", "没有请求的API", 404),
    NoSuchService("NoSuchService", "No such service", "没有请求的服务", 400),
    NoSuchInterface("NoSuchApiId", "No such api id", "请求api Id不存在", 400),

    AlreadyExist("AlreadyExist", "Parameter %s you provided is already exist.", "当前项目已存在同名服务，项目下不允许创建同名服务", 400),
    ServiceTagAlreadyExist("ServiceTagAlreadyExist", "ServiceTag you provided is already exist.", "服务标识已存在", 400),
    TrafficColorRuleAlreadyExist("TrafficColorRuleAlreadyExist", "TrafficColorRule is already exist in this service and route.", "当前服务和路由下已经存在流量染色规则", 400),
    TrafficColorRuleNameAlreadyExist("TrafficColorRuleNameAlreadyExist", "TrafficColorRuleName is already exist, do not allow to create again.", "该流量染色规则名称已存在", 400),
    TrafficColorRuleNameIsEmpty("TrafficColorRuleNameIsEmpty", "TrafficColorRuleName is Empty.", "流量染色规则名称为空", 400),
    TrafficMatchNotSupport("TrafficMatchNotSupport", "TrafficMatch is Not Support.", "染色匹配不支持 仅支持Header匹配", 400),
    NoSuchTrafficColorRule("NoSuchTrafficColorRule", "No such traffic color rule", "没有该染色规则", 400),
    CannotDeleteOnlineTrafficColorRule("CannotDeleteOnlineTrafficColorRule", "You can't delete trafficColorRule unless offline the trafficColorRule", "在停用流量染色规则之前，不能删除该规则", 400),
    QuotaInsufficient("QuotaInsufficient", "Quota %s is insufficient.", "%s 配额不足", 400),
    CanNotDelete("CanNotDelete", "This %s can't be deleted.", "此%s不能删除", 400),
    CanNotFound("ResourceNotFound", "Can't found %s", "找不到对应%s", 400),
    UnCertificated("UnCertificated", "Not yet certificated.", "未实名认证", 400),
    ParameterNull("ParameterNull", "Parameter null", "参数为空", 400),
    ParameterError("ParameterError", "Parameter %s error.", "参数%s错误", 400),

    NoPermissionError("NoPrivate", "No private to config parameter %s", "无权设置参数%s", 403),
    DuplicateNameError("DuplicateNameError", "The value for %s is Duplicated", "%s设置重复", 400),
    ContainStatusError("InvalidParameterValue", "Container %s status %s not support this operation.", "容器%s 的状态%s 不允许执行当前操作", 400),
    WorkloadStatusError("InvalidParameterValue", "Workload %s status %s not support this operation.", "负载%s 的状态%s 不允许执行当前操作", 400),
    NoPermissionBindWorkload("NoPermission",
            "Bare Container is not allowed to bind service", "高性能容器不允许绑定服务", 403),
    NotMatchBounds("NotMatchBounds", "%s is not  match the bounds", "%s不符合限制", 400),

    // StatefulWorkload
    InvalidWorkloadState("InvalidWorkloadState", "The workload can't operate in current state.", "工作负载处于无法进行此操作的状态。", 400),
    IllegalMountingWan("IllegalMountingWan", "Illegal status of wan.", "公网状态不适合绑定。", 400),
    ResourcePackageError("ResourcePackageError", "Unable to manipulate resource package.", "无法操作资源包资源。", 400),
    NumError("InvalidParameterValue", "The number of %s is illegal", "%s 数目错误", 400),
    NumNotMatch("NumNotMatch", "The number of %s doesn't match with created.", "%s数目与创建时不匹配", 400),
    NameNotMatch("NameNotMatch", "%s doesn't match with previous.", "%s 名称与先前不匹配", 400),
    CanNotFoundContainer("ResourceNotFound", "Can't found container %s .", "找不到对应的容器 %s", 404),
    CanNotFoundWorkload("ResourceNotFound", "Can't found workload %s .", "找不到对应的工作负载 %s", 404),
    IllegalMountingWanWithNodBindIp("IllegalMountingWan", "The workload not bind a ip.", "工作负载没有绑定ip。", 400),
    IpTypeNotMatch("IpTypeNotMatch", "This type of ip not match with workload.", "公网ip类型和绑定的负载不匹配。", 400),

    LogDirsAndMountPathDuplicateError("InvalidParameterValue", "Value for %s and for %s are Duplicated", "%s 和 %s 值有重复", 400),
    CannotUpdateSpec("CannotUpdateSpec", "The current spec type of this workload is same as the spec type %s you provided, cannot update.", "工作负载当前规格与传入的规格 %s 一致，无法镜像更新规格操作。", 400),
    CannotRebuild("CannotRebuild", "The charge type of this workload is not 'Package' or package is unchanged, cannot do rebuild operation.", "工作负载的计费方式不为资源包计费或者资源包未表更，无法进行资源包重建操作", 400),
    CannotUpdate("CannotUpdate", "%s can't update", "%s 不能更新", 400),
    SpecCannotUse("SpecCannotUse", "You can not use this specification.", "您不能使用该规格", 403),
    ChargeTypeIllegal("ChargeTypeIllegal", "The charge type of this workload is %s, can not do this operation.", "工作负载当前计费类型为 %s，不能进行该操作。", 400),
    NoPermission("NoPermission", "You don't have permission to access the specified interface.", "对不起，您没有权限访问该接口。", 401),

    ParamNotMatch("ParamNotMatch", "Param not match", "参数不匹配", 400),
    JsonParseException("JSONParseException", "The json parse has exception %s.", "Json解析异常:%s", 400),

    // 计费有关
    InsufficientFunds("InsufficientFunds", "Not sufficient funds in account", "账号余额不足", 403), OrderAlreadyUsed("OrderAlreadyUsed",
            "The order id is already used", "该订单号已经被使用", 403), ChargeException("ChargeException",
            "Charge system exception, please try again or later", "计费异常，请重试", 500),

    ResourceNotFound("ResourceNotFound", "The resource requested is not found.", "请求的资源不存在", 404),
    UserStatusAbnormal("UserStatusAbnormal", "User status abnormal.", "用户状态异常", 403),
    TooManyRequest("TooManyRequest", "Too Many Request, Please try again later.", "访问频率过快，请稍后重试。", 429),
    TimeRangeTooLarge("TimeRangeTooLarge", "Does not support interval queries greater than %s days.", "暂不支持大于%s天的区间查询.", 400),
    QueryTimeIllegal("QueryTimeIllegal", "Start Time must before End TIme.", "起止时间必须小于截至时间.", 400),


    //供G-Dashboard
    InterfaceNotFound("InterfaceNotFound", "We can't found the interface.", "您指定的API不存在!", 400),
    ServiceNotFound("ServiceNotFound", "We can't found the service.", "您指定的Service不存在!", 404),
    ServiceNotPublished("ServiceNotPublished", "We can't found the service publish information.", "您指定的Service未发布!", 400),
    ServiceNotAllowUpdateInstance("ServiceNotAllowUpdateInstance", "you can't update the service instance when choose registry center strategy.", "注册中心拉取服务方式不能更新实例!", 400),
    CannotPublishApi("CannotPublishApi", "You can't pushlish an interface before publishing a service", "在未发布服务之前不能发布接口", 400),
    CannotOfflineService("CannotOfflineService", "You can't offline a service before offline all api of this service.", "在未下线该服务所有接口之前，不允许下线服务", 400),
    HystrixBadRequest("HystrixBadRequest", "Hystrix bad request %.", "熔断器请求非法:%s", 400),
    UpdateFailure("UpdateFailure", "update failure", "更新失败", 400),
    MaxUploadSizeExceed("MaxUploadSizeExceed", "The document is too large!", "文件大小不能超过10M！", 400),
    FileIsEmpty("FileIsEmpty", "The file must not be empty.", "文件不能为空", 400),
    IllegalFileFormat("IllegalFileFormat", "The file format is illegal.", "文件格式非法", 400),
    ApiAlreadyExist("ApiAlreadyExist", "This api is already exist.", "API已存在", 400),
    MissingUploadedFile("WrongFile", "Please select a correct file.", "请选择一个文件,且文件大小不能超过10M", 400),
    HasNoPermission("NoPermissionOperation", "You do not have permission to operate.", "没有权限操作", 400),

    //参数分流
    ShuntWayError("ShuntWayError", "%s shunt way reserve at least one", "%s分流方式至少保留一条", 400),
    InvalidShuntWay("InvalidShuntWay", "Wrong shunt way", "参数分流方式传参错误", 400),
    InvalidInstanceName("InvalidInstanceName", "Wrong shunt way", "权重分流策略每条目标配置只支持一个目标名称", 400),
    InvalidInstanceList("InvalidInstancesList", "The list of instances couldn't be null", "实例列表不能为空或者重复", 400),
    InvalidParamShuntType("InvalidParamShuntType", "Wrong parameter shunt type", "参数分流仅支持名单分流和取模阈值", 400),
    InvalidModulusShuntInstanceList("InvalidModulusShuntInstanceList", "When modulus shunt chosen,the instance list can be only exist one element ", "取模阈值方式时，实例列表只能存在一条数据", 400),
    InvalidParamName("InvalidParamName", "ParamName couldn't be null", "参数名不能为空", 400),
    InvalidParamType("InvalidParamType", "ParamType is invalid", "参数类型取值不合法", 400),
    InvalidParam("InvalidParam", "Param is invalid", "无效的参数", 400),
    InvalidShuntSwitch("InvalidShuntSwitch", "ShuntSwitch is invalid", "分流开关取值不合法", 400),
    InvalidItemList("InvalidItemList", "ItemList couldn't be null", "名单列表不能为空", 400),
    InvalidModulusThreshold("InvalidModulusThreshold", "ModulusThreshold is invalid", "取模阈值要在0-100之间，且为整数", 400),
    InvalidShuntWayList("InvalidShuntWayList", "ShuntWayList is invalid", "分流规则不能为空", 400),
    ShuntWayBindingNotAvailable("ShuntWayBindingNotAvailable", "You can only bind shunt way strategy for target type is instance name when service published by customInput kind", "以手动填写方式发布的服务只能启用目标类型为实例名称的分流策略", 400),

    //webservice
    InvalidWebServiceClassName("InvalidClassName", "ClassName couldn't be null", "类名不能为空", 400),
    InvalidMethodName("InvalidMethodName", "MethodName couldn't be null", "方法名不能为空", 400),
    InvalidParamSort("InvalidParamSort", "ParamSort couldn't be null", "参数序号必须为正整数", 400),

    EmptyServiceTag("EmptyServiceTag", "The serviceTag couldn't be null", "服务标识不能为空", 400),
    HostUnreachable("HostUnreachable", "The host is unreachable", "主机地址无法访问", 400),
    SynchronizedConfigError("SynchronizedConfigError", "Synchronized config is error", "同步网关配置信息出现异常", 400),
    ParseSwaggerFailure("ParseSwaggerFailure", "Parse swagger is failure", "解析swagger文件异常，请检查网络配置或swagger文件格式", 400),
    InvalidProjectId("InvalidProjectId", "Invalid project id", "ProjectId不合法或不存在", 400),
    UpdateToGwFailure("UpdateToGwFailure", "Updating info to gateway is failure", "同步到网关失败，请排查网络环境是否正常", 400),
    SameNamePolicyExist("SameNamePolicyExist", "The policy with the same name already exists and cannot be created.", "同名策略已存在，不允许重复创建!", 400),
    NoSuchGateway("NoSuchGateway", "No such gateway", "指定的网关不存在", 400),
    NoSuchPolicy("NoSuchPolicy", "No such policy", "指定的策略不存在", 404),
    CannotBindingPolicy("CannotBindingPolicy", "You can't binding policies until the service or api is published", "在服务/接口未发布之前不能绑定策略", 400),
    CannotUpdatePolicyUntilUnbinding("CannotUpdatePolicyUntilUnbinding", "You can't update policy until unbinding the policy with all services", "在解绑所有绑定的服务之前，该策略不允许更改。", 400),
    CannotDeletePolicyUntilUnbinding("CannotDeletePolicyUntilUnbinding", "You can't delete policy until unbinding the policy with all services", "在解绑所有绑定的服务之前，该策略不允许删除。", 400),
    PolicyBoundLimit("PolicyBoundLimit", "Only 20 policy can be bound to an object", "一个对象仅允许绑定20条策略!", 400),
    DubboServicePublishWayLimit("DubboServicePublishWayLimit", "dubbo service can not support custom input way", "Dubbo服务发布暂不支持手动填写服务地址方式!", 400),
    DubboServiceParamLimit("DubboServiceParamLimit", "You can't create dubbo param while the service is not a dubbo service", "非Dubbo服务无法创建Dubbo参数!", 400),
    NoSuchProject("NoSuchProject", "No such project.", "指定的项目不存在!", 404),
    EmptyProjectId("EmptyProjectId", "The projectId is empty.", "指定的项目id为空!", 400),
    EmptyTenantId("EmptyTenantId", "The tenantId is empty.", "指定的租户id为空!", 400),

    //网关相关
    AlreadyExistGwName("AlreadyExistGwName", "GwName already exist.", "网关名称已经存在", 400),
    AlreadyExistGw("AlreadyExistGw", "Gw already exist.", "网关已经存在", 400),
    CannotDeleteGateway("CannotDeleteGateway", "Cannot delete gateway,please offline service or api", "网关存在已发布信息，无法删除", 400),
    NoSuchGwId("NoSuchGwId", "No such gwId", "网关id不存在", 400),
    NotPublishedService("NotPublishedService", "The api's service is not published", "API所属服务没有发布，请先发布", 400),
    DataSourceError("DataSourceError", "Can not load %s datasource ", "加载 %s 数据源异常", 400),
    ExistRelationWithOtherProject("ExistRelationWithOtherProject", "exist relation with other project, do not allow to delete ", "当前项目存在与其他项目的关联关系 不允许删除", 400),
    ExistPublishedService("ExistPublishedService", "exist published service, do not allow to delete ", "当前项目存在已发布服务，不允许删除", 400),


    //服务相关
    CannotUpdateService("CannotUpdateService", "You can't update service until offline the service", "在下线服务之前，不能修改服务的基本信息", 400),
    ServicePublishedUpdateLimit("ServicePublishedUpdateLimit", "You can't update service publish type", "不能更新发布服务的发布方式", 400),
    CannotUpdateServiceName("CannotUpdateServiceName", "You can't update service name cause of sync", "从元数据同步服务，不允许修改服务名称", 400),
    CannotDeleteOnlineService("CannotDeleteOnlineService", "You can't delete service until offline the service", "在下线服务之前，不能删除服务", 400),
    CannotDeleteApiService("CannotDeleteApiService", "You can't delete service until delete all apis", "在删除api前，不能删除服务", 400),
    CannotDownloadServiceSDK("CannotDownloadServiceSDK", "You can't download sdk cause of no APIs", "服务下不存在API，不允许下载SDK", 400),
    InvalidCustomHeaderKey("InvalidCustomHeaderKey", "You can't set custom header key with _ character", "自定义Header中Key不能使用 _ ", 400),
    ConsulOperationException("ConsulOperationException", "Consul connection failed ! please be sure consul address is correct", "Consul 连接失败 ! 请确认Consul地址是否填写正确 ", 400),

    //API相关
    AlreadyExistAliasName("AlreadyExistAliasName", "The alias name already exist", "接口标识已经存在", 400),
    AlreadyExistRuleName("AlreadyExistRuleName", "The rule name already exist in current service", "该服务下分流策略名称已经存在", 400),
    CannotDeleteOnlineApi("CannotDeleteOnlineApi", "You can't delete api until offline the api", "在下线接口之前，不能删除接口", 400),
    DuplicateParamName("DuplicateParamName", "You can't create duplicate param name", "请求参数重复", 400),
    NotPublishedApi("NotPublishedApi", "The api is not published", "API没有发布到当前网关，请先发布", 400),
    AlreadyBindTrafficControl("AlreadyBindTrafficControl", "The api already bind traffic control", "API绑定了流控策略，请先解绑", 400),
    NoSuchParamType("NoSuchParamType", "No such param type ", "paramTyped不存在", 400),
    NoSuchArrayDataType("NoSuchArrayDataType", "No such  array data type", "array datat ype不存在", 400),
    ErrorTimeRange("ErrorTimeRange", "The time range is error", "不可用时间区间不合法", 400),

    //授权相关
    NoGatewayExist("NoGatewayExist", "The envId is not exist gateway", "该环境下不存在可用网关", 400),
    InvalidEnvId("InvalidEnvId", "The envId is iinvalid", "网关envId异常", 400),

    //grpc相关
    DuplicateProtobufName("DuplicateProtobufName", "This protobuf was already exist", "该pb文件对应的名称已存在", 400),
    InvalidProtobufId("InvalidProtobufId", "The parameter pbId was invalid", "参数pbId取值不合法", 400),
    InvalidProtobufName("InvalidProtobufName", "The parameter pbName was invalid", "参数pbName取值不合法", 400),
    InvalidProtobufDesc("InvalidProtobufDesc", "The parameter pbDesc was invalid", "参数pbDesc取值不合法", 400),
    InvalidProtobufContent("InvalidPbContent", "Protobuf file couldn't be null", "pb文件不能为空", 400),
    ParseProtobufFailed("ParseProtobufFailed", "The operation was failed", "编译pb文件失败，请检查pb文件是否合法或者与其它服务pb文件定义冲突", 400),
    ApiConflictOccurs("ApiConflictOccurs", "Api Conflict Occurs. Api: %s", "存在冲突的API:%s", 400),
    DeleteOperationRefused("DeleteOperationRefused", "Please offline protobuf first", "请先执行下线操作再进行删除", 400),
    InvalidPublishOperation("InvalidPublishOperation", "The protobuf file couldn't be published.", "服务未发布，则pb不能发布", 400),
    OfflineOperationRefused("OfflineOperationRefused", "Please publish protobuf first", "请先执行发布操作再进行下线", 400),
    CannotOfflineGrpcService("CannotOfflineGrpcService", "You can't offline a grpc service before offline all protobuf.", "在未下线该服务下所有pb文件前，不允许下线服务", 400),

    InvalidPbServiceList("InvalidPbServiceList", "This protobuf file does not contain these services.", "该pb文件不包含传入的某个服务", 400),
    InvalidPluginManager("InvalidPluginManager", "The envoy.grpc_json_transcoder is not managered.", "GRPC协议转换插件未配置", 400),
    ProcessProtobufFailed("ProcessProtobufFailed", "Processing protobuf was failed.", "pb文件处理失败", 400),
    PublishProtobufFailed("PublishProtobufFailed", "Publishing protobuf was failed.", "pb文件更新到数据面失败", 400),
    CouldNotOfflineService("CouldNotOfflineService", "You could't offline a service before offlining protobuf.", "在未下线该服务下pb文件前，不允许下线服务", 400),

    //webservice相关
    IllegalWsdlFormat("IllegalWsdlContent", "Illegal wsdl format: %s", "非法wsdl文件格式: %s", 400),
    ResourceDownloadFailed("ResourceDownloadFailed", "Failed to download the WSDL dependent resource:%s.  Please check if the network is reachable.", "下载wsdl依赖资源:%s失败， 请检查网络是否可到达", 400),
    IllegalBindingType("IllegalBindingType", "Illegal webservice binding type", "非法的webservice binding类型", 400),
    IllegalBindingParam("IllegalBindingParam", "Illegal binding parameter(portType, operation, binding, address).", "非法的binding参数(portType, operation, binding, address)", 400),
    NoSuchWsdlInfo("NoSuchWsdlInfo", "There is no specified wsdl configuration", "不存在指定的wsdl配置", 400),
    NoSuchWsParam("NoSuchWsParam", "There is no specified webservice param", "不存在指定的webservice param", 400),
    BindingWsPluginFailed("BindingWsPluginFailed", "Failed to bind webservice plugin.", "绑定webservice插件失败", 400),
    UnBindingWsPluginFailed("UnBindingWsPluginFailed", "Failed to unbind webservice plugin.", "解除绑定webservice插件失败", 400),
    RouteWsPluginNonExist("RouteWsPluginNonExist", "There is no webservice plugin on the route.", "路由不存在webservice插件", 400),
    RenderWsTemplateFailed("RenderWsTemplateFailed", "Failed to render webservice template, possibly due to illeage template or parameter.", "渲染webservice模板失败，可能因为非法的模板或参数", 400),
    CreateWsTemplateFailed("CreateWsTemplateFailed", "Failed to create webservice template.", "创建webservice请求模板失败", 400),

    /**
     * 数据模型相关
     */
    NoSuchModel("NoSuchModel", "No Such Model", "此数据模型不存在", 400),
    NoSuchShuntStrategy("NoSuchShuntStrategy", "No Such shunt strategy", "此分流策略不存在", 400),

    //插件相关
    InvalidPluginName("InvalidPluginName", "PluginName couldn't be null", "插件名不能为空", 400),
    InvalidPluginVersion("InvalidPluginVersion", "PluginVersion couldn't be null", "插件版本不能为空", 400),
    InvalidGroovyFile("InvalidGroovyFile", "Groovy file couldn't be complied successfully ", "插件文件读取译失败", 400),
    InvalidPluginContent("InvalidPluginContent", "Groovy file couldn't be null", "插件文件不能为空", 400),
    InvalidPluginVariable("InvalidPluginVariable", "Plugin variable is valid", "插件变量传值非法", 400),
    InvalidPluginVariablerepeat("InvalidPluginVariable", "Plugin variable is valid", "插件变量不能重复", 400),
    InvalidPluginOperation("InvalidPluginOperation", "Plugin couldn't be removed when it was alive", "插件在启用状态下不能将其删除", 400),
    InvalidPluginId("InvalidPluginId", "The plugin doesn't exist", "该插件不存在", 400),
    InvalidPluginNameAndVersion("InvalidPluginNameAndVersion", "The plugin was already exist", "该插件已存在", 400),
    DeletePluginInfoFailed("DeletePluginInfoFailed", "The operation was failed", "删除插件失败", 400),
    UpdatePluginInfoFailed("UpdatePluginInfoFailed", "The operation was failed", "更新插件失败", 400),
    ParsePluginInfoFailed("ParsePluginInfoFailed", "The operation was failed", "编译插件失败", 400),
    RepeatParamName("RepeatParamName", "The paramName is repeated", "参数存在重复，不能提交", 400),

    InvalidOperation("InvalidOperation", "The operation was invalid", "请不用重复提交", 400),
    TooManyDimensions("TooManyDimensions", "Too many dimensions, max dimension size is 10.", "维度数过多，最多仅允许接收10个", 400),

    SameNameGatewayExist("SameNameGatewayExist", "The gateway with the same name already exists and cannot be created.", "同名网关已存在，不允许重复创建!", 400),
    SameNameGatewayClusterExist("SameNameGatewayClusterExist", "The gateway cluster with the same name already exists and cannot be created.", "同名网关集群已存在，不允许重复创建!", 400),
    SameNameRouteRuleExist("SameNameRouteRuleExist", "The route rule with the same name already exists and cannot be "
            + "created.", "当前项目已存在同名路由，项目下不允许创建同名路由", 400),
    SameParamRouteRuleExist("SameParamRouteRuleExist", "The same param of route rule already exists and cannot be created.", "相同参数的路由规则已存在，不允许重复创建!", 400),
    NoSuchRouteRule("NoSuchRouteRule", "No such route rule", "指定的路由规则不存在", 400),
    InvalidDestinationService("InvalidDestinationService", "Destination service is invalid", "路由规则发布时指定的后端服务不能为空", 400),
    ServiceAlreadyPublished("ServiceAlreadyPublished", "The service has already published", "服务已经发布到当前网关", 400),
    RouteRuleAlreadyPublished("RouteRuleAlreadyPublished", "The route rule has already published", "路由规则已经发布至该网关", 400),
    RouteRuleAlreadyPublishedToGw("RouteRuleAlreadyPublishedToGw", "The route rule has already published to gw, gw is: %s.", "路由规则已发布至网关: %s", 400),
    RouteRuleNotPublished("RouteRuleNotPublished", "The route rule not published", "路由规则未发布", 400),
    RouteRuleServiceNotMatch("RouteRuleServiceNotMatch", "The route rule not match the service", "服务和路由规则不匹配", 400),
    RouteRuleMethodInvalid("RouteRuleMethodInvalid", "The route rule method is invalid", "路由规则指定的method不合法", 400),
    CannotDeleteRouteRuleService("CannotDeleteRouteRuleService", "The service has route rule", "服务存在路由规则，不允许删除", 400),
    SortKeyInvalid("SortKeyInvalid", "The sort key is invalid", "搜索查询查询搜索项不正确", 400),
    SortValueInvalid("SortValueInvalid", "The sort value is invalid", "搜索查询查询值不正确", 400),
    NoSuchPluginBinding("NoSuchPluginBinding", "No such plugin binding info", "指定的插件绑定关系不存在", 400),
    IllegalPluginType("IllegalPluginType", "The plugin type is illegal", "指定的插件类型不匹配", 400),
    CannotDuplicateBinding("CannotDuplicateBinding", "The plugin binding already exists and duplicate binding are not allowed", "插件绑定关系已存在，不允许重复绑定同一插件", 400),
    CannotDuplicateBindingAuthPlugin("CannotDuplicateBindingAuthPlugin", "The auth type plugin binding already exists",
            "认证类型插件绑定关系已存在，不允许重复绑定", 400),
    MirrorByRouteRule("MirrorByRouteRule", "The service has been mirrored by route rule", "该服务已被路由流量镜像使用", 400),

    //健康检查相关
    InvalidApiPath("InvalidApiPath", "Api path is invalid", "接口路径不合法", 400),
    InvalidActiveSwitch("InvalidActiveSwitch", "The active switch is invalid", "主动检查开关不合法", 400),
    InvalidPassiveSwitch("InvalidPassiveSwitch", "The passive switch is invalid", "被动检查开关不合法", 400),
    InvalidTimeout("InvalidTimeout", "The timeout param is invalid", "超时时间不合法", 400),
    InvalidHttpStatusCode("InvalidHttpStatusCode", "Http status code is invalid", "健康状态码不合法", 400),
    InvalidHealthyInterval("InvalidHealthyInterval", "Health  interval is invalid", "健康实例检查间隔不合法", 400),
    InvalidHealthyThreshold("InvalidHealthyThreshold", "Health threshold is invalid", "健康阈值不合法", 400),
    InvalidUnHealthyInterval("InvalidUnHealthyInterval", "Unhealthy interval is invalid", "异常实例检查间隔不合法", 400),
    InvalidUnHealthyThreshold("InvalidUnHealthyThreshold", "Unhealthy threshold is invalid", "异常阈值不合法", 400),
    InvalidConsecutiveErrors("InvalidConsecutiveErrors", "Consecutive errors parameter is invalid", "连续失败次数不合法", 400),
    InvalidBaseEjectionTime("InvalidBaseEjectionTime", "Base ejection time is invalid", "驱逐时间不合法", 400),
    InvalidMaxEjectionPercent("InvalidMaxEjectionPercent", "Max ejection percent is invalid", "最多可驱逐的实例比不合法", 400),
    NoRouteRulePath("NoRouteRulePath", "No route rule path", "创建路由，path不能为空", 400),
    RouteRuleContainsNginxCapture("RouteRuleContainsNginxCapture", "Route rule contains nginx capture regex", "创建路由，path正则中不能包含nginx捕获正则", 400),
    NotModifyPriority("NotModifyPriority", "Not modify priority", "未修改优先级，不允许复制", 400),
    NoSuchPlugin("NoSuchPlugin", "No such plugin.", "指定的插件不存在", 400),
    NotModifyRouteRuleName("NotModifyRouteRuleName", "Not modify routeRuleName", "未修改路由规则名称，不允许复制", 400),

    SubsetUsedByRouteRule("SubsetUsedByRouteRule", "Subset was used by route rule.", "不能删除已被路由规则：%s，引用的版本", 400),
    InvalidTotalWeight("InvalidTotalWeight", "Total weight was invalid.", "权重之和必须为100", 400),
    BackendServiceDifferent("BackendServiceDifferent", "Backend services are different.", "发布服务，指定后端服务不同不允许创建", 400),
    BatchPublishRouteError("BatchPublishRouteError", "Batch publish route error, the error gw is: %s.", "批量发布路由至多网关失败，失败网关: %s", 500),
    ProjectNotAssociatedGateway("ProjectNotAssociatedGateway", "The current project is not associated with the specified gateway.", "当前项目未关联指定网关，不允许发布", 400),

    //负载均衡相关
    InvalidLoadBanlanceType("InvalidLoadBanlanceType", "This load balance type is invalid", "服务负载均衡类型取值为Simple或ConsistentHash", 400),
    InvalidSimpleLoadBanlanceType("InvalidSimpleLoadBanlanceType", "This simple load balance type is invalid", "Simple类型的负载均衡规则，仅包含ROUND_ROUBIN、LEAST_CONN、RANDOM", 400),
    InvalidConsistentHashObject("InvalidConsistentHashObject", "Consistent hash object is invalid", "一致性哈希对象格式非法", 400),
    InvalidConsistentHashType("InvalidConsistentHashType", "Consistent hash type is invalid", "一致性哈希对象类型为HttpHeaderName、HttpCookie、UseSourceIp三者之一", 400),
    InvalidConsistentHashHttpCookieObject("InvalidConsistentHashHttpCookieObject", "Http cookie is invalid", "一致性哈希对象使用cookie时，cookie对象不能为空", 400),
    InvalidConsistentHashHttpCookieName("InvalidConsistentHashHttpCookieName", "Http cookie name is invalid", "一致性哈希对象使用cookie时，cookie名称不能为空", 400),
    InvalidConsistentHashHttpCookieTtl("InvalidConsistentHashHttpCookieName", "Http cookie ttl is invalid", "一致性哈希对象使用cookie时，cookie ttl不能小于0", 400),
    InvalidConsistentHashHttpHeaderName("InvalidConsistentHashHttpHeaderName", "Http header name is invalid", "一致性哈希对象使用HttpHeaderName时，HttpHeaderName不能为空", 400),
    InvalidConsistentHashSourceIP("InvalidConsistentHashSourceIP", "Source ip is invalid", "一致性哈希对象使用源IP时，源IP不能为空", 400),

    InvalidHttp1MaxPendingRequests("InvalidHttp1MaxPendingRequests", "http1MaxPendingRequests is invalid", "http1MaxPendingRequests不能小于0", 400),
    InvalidHttp2MaxRequests("InvalidHttp2MaxRequests", "Http2MaxRequests is invalid", "http2MaxRequests不能小于0", 400),
    InvalidIdleTimeout("InvalidIdleTimeout", "IdleTimeout is invalid", "idleTimeout不能小于0", 400),
    InvalidMaxRequestsPerConnection("InvalidMaxRequestsPerConnection", "MaxRequestsPerConnection is invalid", "maxRequestsPerConnection不能小于0", 400),
    InvalidMaxConnections("InvalidmaxConnections", "MaxConnections is invalid", "maxConnections不能小于0", 400),
    InvalidConnectTimeout("InvalidConnectTimeout", "ConnectTimeout is invalid", "connectTimeout不能小于0", 400),
    DuplicatedSubsetName("DuplicatedSubsetName", "SubsetName is duplicated", "版本名称不能重复", 400),
    InvalidSubsetName("InvalidSubsetName", "Subset does not exist", "版本名称不存在", 400),
    InvalidSubsetStaticAddr("InvalidSubsetStaticAddr", "Subset's address is invalid", "版本中的地址不合法，不能为空且需要包含在服务发布地址列表中", 400),
    DuplicatedSubsetStaticAddr("DuplicatedSubsetStaticAddr", "Subset's addresses are duplicated", "版本中的地址不合法，存在重复的地址", 400),
    DuplicatedStaticAddr("DuplicatedStaticAddr", "Static address only belongs to one subset", "一个地址仅能属于一个版本", 400),

    InvalidEurekaAddress("InvalidEurekaAddress", "Eureka address is invalid", "Eureka地址不合法，不能为空且必须以http://开头，以/eureka/结尾", 400),
    NoSuchVirtualHost("NoSuchVirtualHost", "No such virtual host", "请求的virtual host不存在", 400),
    VirtualHostAlreadyExist("VirtualHostAlreadyExist", "The virtual host is already exist.", "该项目已关联域名，请修改", 400),

    SameNamePluginTemplateExist("SameNamePluginTemplateExist", "The plugin template with the same name already exists and cannot be created.", "同名插件模板已存在，不允许重复创建!", 400),
    NoSuchPluginTemplate("NoSuchPluginTemplate", "No such plugin template", "指定的插件模板不存在", 400),

    CannotUpdatePlugin("CannotUpdatePlugin", "Because the template is associated, you are not allowed to update the configuration", "关联了模板，不允许直接更新插件配置", 400),

    AlreadyExistIntegrationName("AlreadyExistIntegrationName", "IntegrationName you provided is already exist.", "集成名称已经存在", 400),
    NoSuchIntegration("NoSuchIntegration", "No such integration", "指定的集成不存在", 400),
    CannotUpdateIntegrationRule("CannotUpdateIntegrationRule", "You cannot update an integration when it is published", "在集成已发布时，不能修改规则", 400),
    CannotDeleteIntegration("CannotDeleteIntegration", "You cannot delete an integration when it is published", "指定的集成已发布，无法删除", 400),
    IntegrationAlreadyPublished("IntegrationAlreadyPublished", "The integration has already published.", "集成已经发布", 400),
    NoSuchSchema("NoSuchSchema", "No such schema", "指定的schema不存在", 400),
    IntegrationNotPublished("IntegrationNotPublished", "Integration not published", "指定ID的集成未发布", 400),
    FailedToPublishServiceOrRoute("FailedToPublishServiceOrRoute", "Failed to automatically publish service or route", "自动发布服务或路由失败", 400),
    FailedToPublishIntegration("FailedToPublishIntegration", "Failed to publish Integration", "发布集成失败", 400),
    FailedToOfflineServiceOrRoute("FailedToOfflineServiceOrRoute", "Failed to automatically offline service or route", "自动下线服务或路由失败", 400),
    PublishTypeNotSupport("PublishTypeNotSupport", "The publish type of service can not support this operation", "该服务的发布方式并不支持本操作", 400),
    CustomParamMappingInvalid("CustomParamMappingInvalid", "All params name must be blank when custom mapping switch is closed", "关闭自定义参数映射开关后， 所有自定义名称必须为空", 400),
    ;

    private String code;
    private String enMsg;
    private String msg;
    private int statusCode;

    private ErrorCodeEnum(String code, String enMsg, String msg, int statusCode) {
        this.code = code;
        this.enMsg = enMsg;
        this.msg = msg;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
