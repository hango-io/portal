package org.hango.cloud.gdashboard.api.meta;

/**
 * 区分参数的类型，包含webservice接口定义中的Service|Method|RequestParam|ResponseParam四种
 */
@SuppressWarnings("java:S115")
public enum WebServiceParamType {

    Service,
    Method,
    RequestParam,
    ResponseParam;

    WebServiceParamType() {
    }

    public static String getWebServiceType(String type) {
        for (WebServiceParamType webServiceParamType : WebServiceParamType.values()) {
            if (webServiceParamType.name().equals(type)) {
                return webServiceParamType.name();
            }
        }
        return null;
    }
}
