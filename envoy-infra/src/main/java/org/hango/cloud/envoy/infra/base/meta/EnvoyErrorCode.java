package org.hango.cloud.envoy.infra.base.meta;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCodeEnum;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/5/5
 */
public class EnvoyErrorCode extends ErrorCode {

    /************************************************ 引用公共异常ErrorCode start ********************************************************************************/
    public static ErrorCode UPDATE_VIRTUAL_HOST_FAILED = new ErrorCode(ErrorCodeEnum.UPDATE_FAILED, "Virtual Host");

    /************************************************ 引用公共异常ErrorCode end ********************************************************************************/

    public static ErrorCode COULD_NOT_OFFLINE_SERVICE = new EnvoyErrorCode(EnvoyErrorCodeEnum.COULD_NOT_OFFLINE_SERVICE);


    /**
     * grpc相关 2.0
     */

    public static ErrorCode INVALID_PROTOBUF_CONTENT = new EnvoyErrorCode(EnvoyErrorCodeEnum.INVALID_PROTOBUF_CONTENT);
    public static ErrorCode PARSE_PROTOBUF_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.PARSE_PROTOBUF_FAILED);
    public static ErrorCode INVALID_PB_SERVICE_LIST = new EnvoyErrorCode(EnvoyErrorCodeEnum.INVALID_PB_SERVICE_LIST);
    public static ErrorCode INVALID_PLUGIN_MANAGER = new EnvoyErrorCode(EnvoyErrorCodeEnum.INVALID_PLUGIN_MANAGER);
    public static ErrorCode PROCESS_PROTOBUF_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.PROCESS_PROTOBUF_FAILED);
    public static ErrorCode PUBLISH_PROTOBUF_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.PUBLISH_PROTOBUF_FAILED);

    /**
     * Dubbo相关
     */
    public static ErrorCode GENERIC_INFO_INVALID = new EnvoyErrorCode(EnvoyErrorCodeEnum.GENERIC_INFO_INVALID);
    public static ErrorCode DUBBO_ATTACHMENT_CONFIG_INVAILD = new EnvoyErrorCode(EnvoyErrorCodeEnum.DUBBO_ATTACHMENT_CONFIG_INVAILD);


    public static ErrorCode OFFLINE_PROTOBUF_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.OFFLINE_PROTOBUF_FAILED);
    public static ErrorCode COULD_NOT_OFFLINE_WS_SERVICE = new EnvoyErrorCode(EnvoyErrorCodeEnum.COULD_NOT_OFFLINE_WS_SERVICE);

    public static ErrorCode TRAFFIC_COLOR_RULE_NAME_IS_EMPTY = new EnvoyErrorCode(EnvoyErrorCodeEnum.TRAFFIC_COLOR_RULE_NAME_IS_EMPTY);
    public static ErrorCode TRAFFIC_MATCH_NOT_SUPPORT = new EnvoyErrorCode(EnvoyErrorCodeEnum.TRAFFIC_MATCH_NOT_SUPPORT);
    public static ErrorCode TRAFFIC_COLOR_RULE_NAME_ALREADY_EXIST = new EnvoyErrorCode(EnvoyErrorCodeEnum.TRAFFIC_COLOR_RULE_NAME_ALREADY_EXIST);
    public static ErrorCode TRAFFIC_COLOR_TAG_ALREADY_EXIST = new EnvoyErrorCode(EnvoyErrorCodeEnum.TRAFFIC_COLOR_TAG_ALREADY_EXIST);
    public static ErrorCode NO_SUCH_TRAFFIC_COLOR_RULE = new EnvoyErrorCode(EnvoyErrorCodeEnum.NO_SUCH_TRAFFIC_COLOR_RULE);

    public static ErrorCode UPDATE_PLUGIN_INFO_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.UPDATE_PLUGIN_INFO_FAILED);

    public static ErrorCode CANNOT_DELETE_ONLINE_TRAFFIC_COLOR_RULE = new EnvoyErrorCode(EnvoyErrorCodeEnum.CANNOT_DELETE_ONLINE_TRAFFIC_COLOR_RULE);

    public static ErrorCode NO_SUCH_WSDL_INFO = new EnvoyErrorCode(EnvoyErrorCodeEnum.NO_SUCH_WSDL_INFO);

    public static ErrorCode ILLEGAL_BINDING_PARAM = new EnvoyErrorCode(EnvoyErrorCodeEnum.ILLEGAL_BINDING_PARAM);

    public static ErrorCode BINDING_WS_PLUGIN_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.BINDING_WS_PLUGIN_FAILED);

    public static ErrorCode UN_BINDING_WS_PLUGIN_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.UN_BINDING_WS_PLUGIN_FAILED);

    public static ErrorCode ROUTE_WS_PLUGIN_NON_EXIST = new EnvoyErrorCode(EnvoyErrorCodeEnum.ROUTE_WS_PLUGIN_NON_EXIST);
    public static ErrorCode RENDER_WS_TEMPLATE_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.RENDER_WS_TEMPLATE_FAILED);
    public static ErrorCode CREATE_WS_TEMPLATE_FAILED = new EnvoyErrorCode(EnvoyErrorCodeEnum.CREATE_WS_TEMPLATE_FAILED);

    public static ErrorCode NOT_PUBLISHED_SERVICE = new EnvoyErrorCode(EnvoyErrorCodeEnum.NOT_PUBLISHED_SERVICE);

    public static ErrorCode NOT_PUBLISHED_API = new EnvoyErrorCode(EnvoyErrorCodeEnum.NOT_PUBLISHED_API);

    public static ErrorCode NO_SUCH_WS_PARAM = new EnvoyErrorCode(EnvoyErrorCodeEnum.NO_SUCH_WS_PARAM);


    public EnvoyErrorCode(EnvoyErrorCodeEnum errorCodeEnum, String... args) {
        super(errorCodeEnum.getStatusCode(), errorCodeEnum.getCode(),
                String.format(errorCodeEnum.getMsg(), args), String.format(errorCodeEnum.getEnMsg()));
    }

    public static EnvoyErrorCode illegalWsdlFormat(String message) {
        return new EnvoyErrorCode(EnvoyErrorCodeEnum.ILLEGAL_WSDL_FORMAT, message);
    }

    public static EnvoyErrorCode resourceDownloadFailed(String url) {
        return new EnvoyErrorCode(EnvoyErrorCodeEnum.RESOURCE_DOWNLOAD_FAILED, url);
    }

    public EnvoyErrorCode(int statusCode, String code, String message, String enMessage) {
        super();
    }


}
