package org.hango.cloud.envoy.infra.base.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/5/6
 */
public enum EnvoyErrorCodeEnum {

    COULD_NOT_OFFLINE_SERVICE("CouldNotOfflineService", "You couldn't offline a service before offline protobuf.", "在未下线或删除该服务下pb文件前，不允许删除服务", 400),
    COULD_NOT_OFFLINE_WS_SERVICE("CouldNotOfflineService", "You couldn't offline a service before offline webservice config.", "在未下线或删除该服务下webservice接口，不允许删除服务", 400),

    INVALID_PB_SERVICE_LIST("InvalidPbServiceList", "This protobuf file does not contain these services.", "该pb文件不包含传入的某个服务", 400),
    INVALID_PLUGIN_MANAGER("InvalidPluginManager", "The envoy.grpc_json_transcoder is not managered.", "GRPC协议转换插件未配置", 400),
    PROCESS_PROTOBUF_FAILED("ProcessProtobufFailed", "Processing protobuf was failed.", "pb文件处理失败", 400),
    PUBLISH_PROTOBUF_FAILED("PublishProtobufFailed", "Publishing protobuf was failed.", "pb文件更新到数据面失败", 400),

    OFFLINE_PROTOBUF_FAILED("OfflineProtobufFailed", "Offlining protobuf was failed.", "pb文件下线失败", 400),
    PARSE_PROTOBUF_FAILED("ParseProtobufFailed", "The operation was failed", "编译pb文件失败，请检查pb文件是否合法或者与其它服务pb文件定义冲突", 400),
    INVALID_PROTOBUF_CONTENT("InvalidPbContent", "Protobuf file couldn't be null", "pb文件不能为空", 400),

    /************************************************ Dubbo ErrorCode Start ********************************************************************************/

    GENERIC_INFO_INVALID("GenericInfoInvalid", "generic config invalid", "泛型配置格式错误", 400),
    DUBBO_ATTACHMENT_CONFIG_INVAILD("DubboAttachmentConfigInvaild", "dubbo attachment config invaild", "隐式参数配置错误", 400),


    /************************************************ Dubbo ErrorCode end ********************************************************************************/

    INVALID_PUBLISH_OPERATION("InvalidPublishOperation", "The protobuf file couldn't be published.", "服务未发布，则pb不能发布", 400),
    TRAFFIC_COLOR_RULE_NAME_IS_EMPTY("TrafficColorRuleNameIsEmpty", "TrafficColorRuleName is Empty.", "流量染色规则名称为空", 400),

    TRAFFIC_MATCH_NOT_SUPPORT("TrafficMatchNotSupport", "TrafficMatch is Not Support.", "染色匹配不支持 仅支持Header匹配", 400),

    TRAFFIC_COLOR_RULE_NAME_ALREADY_EXIST("TrafficColorRuleNameAlreadyExist", "TrafficColorRuleName is already exist, do not allow to create again.", "当前项目已存在同名流量染色规则，项目下不允许创建同名流量染色规则", 400),

    TRAFFIC_COLOR_TAG_ALREADY_EXIST("TrafficColorTagAlreadyExist", "TrafficColorTag is already exist in this service and route.", "当前服务和路由下已经存在该流量染色标识", 400),

    NO_SUCH_TRAFFIC_COLOR_RULE("NoSuchTrafficColorRule", "No such traffic color rule", "没有该染色规则", 400),

    UPDATE_PLUGIN_INFO_FAILED("UpdatePluginInfoFailed", "The operation was failed", "更新插件失败", 400),

    CANNOT_DELETE_ONLINE_TRAFFIC_COLOR_RULE("CannotDeleteOnlineTrafficColorRule", "You can't delete trafficColorRule unless offline the trafficColorRule", "在停用流量染色规则之前，不能删除该规则", 400),

    ILLEGAL_WSDL_FORMAT("IllegalWsdlContent", "Illegal wsdl format: %s", "非法wsdl文件格式: %s", 400),

    RESOURCE_DOWNLOAD_FAILED("ResourceDownloadFailed", "Failed to download the WSDL dependent resource:%s.  Please check if the network is reachable.", "下载wsdl依赖资源:%s失败， 请检查网络是否可到达", 400),

    NO_SUCH_WSDL_INFO("NoSuchWsdlInfo", "There is no specified wsdl configuration", "不存在指定的wsdl配置", 400),

    ILLEGAL_BINDING_PARAM("IllegalBindingParam", "Illegal binding parameter(portType, operation, binding, address).", "非法的binding参数(portType, operation, binding, address)", 400),

    BINDING_WS_PLUGIN_FAILED("BindingWsPluginFailed", "Failed to bind webservice plugin.", "绑定webservice插件失败", 400),
    UN_BINDING_WS_PLUGIN_FAILED("UnBindingWsPluginFailed", "Failed to unbind webservice plugin.", "解除绑定webservice插件失败", 400),

    ROUTE_WS_PLUGIN_NON_EXIST("RouteWsPluginNonExist", "There is no webservice plugin on the route.", "路由不存在webservice插件", 400),
    RENDER_WS_TEMPLATE_FAILED("RenderWsTemplateFailed", "Failed to render webservice template, possibly due to illeage template or parameter.", "渲染webservice模板失败，可能因为非法的模板或参数", 400),
    CREATE_WS_TEMPLATE_FAILED("CreateWsTemplateFailed", "Failed to create webservice template.", "创建webservice请求模板失败", 400),

    NOT_PUBLISHED_SERVICE("NotPublishedService", "The api's service is not published", "API所属服务没有发布，请先发布", 400),

    NOT_PUBLISHED_API("NotPublishedApi", "The api is not published", "API没有发布到当前网关，请先发布", 400),

    NO_SUCH_WS_PARAM("NoSuchWsParam", "There is no specified webservice param", "不存在指定的webservice param", 400),
    ;

    private String code;
    private String enMsg;
    private String msg;
    private int statusCode;

    EnvoyErrorCodeEnum(String code, String enMsg, String msg, int statusCode) {
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
