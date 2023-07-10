package org.hango.cloud.common.infra.base.meta;

/**
 * @Author zhufengwei
 * @Date 2023/4/25
 */
public class RegexConst {
    /**
     * 网关、虚拟网关标识正则校验
     */
    public static final String REGEX_GATEWAY_CODE = "^(?!-)[[a-z0-9\\\\-]*]{1,64}(?<!-)$";

    /**
     * 1.完整域名总长度[1-255]
     * 2.一个完整域名实际上是由多个标签（label）组成的，每个标签之间使用点号（.）分隔。每个标签的长度[1,62]
     * 3.每个标签支持a-z,A-Z,0-9,-,标签开头只支持a-z,A-Z,0-9
     */
    public static final String REGEX_DOMAIN = "^(?=^.{1,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$";

    /**
     * 备注信息，选填，支持全文本，最长200字符
     */
    public static final String REGEX_DESCRIPTION = "^[\\s\\S]{0,200}$";


    /**
     * 网关名称：必填，支持中文，数字，英文大小写，中划线，下划线，最大长度32字符
     */
    public static final String REGEX_GATEWAY_NAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\.]{1,32}$";

    /**
     * 虚拟网关名称：支持字母、数字、下划线和中划线，63个字符以内
     */
    public static final String REGEX_VIRTUAL_GATEWAY_NAME = "^[a-zA-Z0-9_-]{0,63}$";



    /**
     * 名称：必填，支持字母、数字和中划线，并且以小写字母或数字开头和结尾，63个字符以内
     */
    public static final String REGEX_NAME = "^([a-z0-9][a-z0-9-]{0,61}[a-z0-9])$|^[a-z0-9]$";

    /**
     * 路由匹配规则值（header和query）要求：大小写英文字母、数字和"-"，1-100字符
     */
    public static final String REGEX_MATCH_VALUE = "^[a-zA-Z0-9-]{1,100}$";
    /**
     * 路由匹配规则KEY（header和query）要求：大小写英文字母、数字和"-"，1-64字符
     */
    public static final String REGEX_MATCH_KEY = "^[a-zA-Z0-9-]{1,64}$";
    /**
     * 路由Path前缀和精确匹配规则值要求：大小写英文字母、数字-_ . ~ /，1-100字符，以"/"开头
     */
    public static final String REGEX_PATH_VALUE = "^/[-_.~a-zA-Z0-9\\/]{0,99}$";

    /**
     * 健康检查：大小写英文字母、数字-_ . ~ /，2-200字符，以"/"开头
     */
    public static final String REGEX_HEALTH_PATH_VALUE = "^/[\\w-_.~/?&=]{1,199}$";

    /**
     * 标签Value正则校验,以字母或数字开头和结尾，支持'-'、'_'、'.'，63个字符以内，不可为空
     */
    public static final String REGEX_LABEL_VALUE = "^(?=.{1,63}$)((?:[A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?$";

    /**
     * 标签Key正则校验,可选的前缀和名称，用斜杠（/）分隔。前缀必须是 DNS 子域：由点（.）分隔的一系列 DNS 标签，总共不超过 253 个字符。名称为 1-63 字符。以字母数字字符（[a-z0-9A-Z]）开头和结尾， 支持中划线（-）、下划线（_）、点（.）和之间的字母数字。
     */
    public static final String REGEX_LABEL_KEY = "^(?=.{1,253}$)([a-zA-Z0-9](?:[-a-z-A-Z0-9]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[-a-z-A-Z0-9]*[a-zA-Z0-9])?)*/)?((?=.{1,63}$)(?:[A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])$";


}
