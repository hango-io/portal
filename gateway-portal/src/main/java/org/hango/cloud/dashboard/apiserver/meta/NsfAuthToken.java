package org.hango.cloud.dashboard.apiserver.meta;

import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/8/7
 */
public class NsfAuthToken {
    public static final String TOKEN_KEY = "X-nsf-authentication";
    public static final long ONE_DAY = 86400000L;
    private String tokenId;
    private Long expirationTime;

    public NsfAuthToken(Map<String, Object> resMap) {
        this.tokenId = (String) resMap.get("TokenId");
        this.expirationTime = System.currentTimeMillis() + 86400000L;
    }

    public String getTokenId() {
        return this.tokenId;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.expirationTime;
    }
}
