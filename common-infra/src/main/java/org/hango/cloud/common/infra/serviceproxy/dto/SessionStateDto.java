package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/8/17
 */
@Getter
@Setter
public class SessionStateDto implements Serializable {

    private static final long serialVersionUID = 1235788312555981195L;

    /**
     * cookie key
     */
    @JSONField(name = "CookieName")
    private String cookieName = "SERVERID";

    /**
     * 对应API-plane插件类型，不允许被修改
     */
    @Setter(AccessLevel.NONE)
    private String kind = "session-state";

    /**
     * cookie的有效期，单位秒
     */
    @JSONField(name = "CookieTTL")
    private Integer cookieTTL;

    /**
     * cookie写入路径
     */
    @JSONField(name = "CookiePath")
    private String cookiePath = "/";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionStateDto that = (SessionStateDto) o;
        return Objects.equals(cookieName, that.cookieName)
                && Objects.equals(kind, that.kind)
                && Objects.equals(cookieTTL, that.cookieTTL)
                && Objects.equals(cookiePath, that.cookiePath);
    }




}
