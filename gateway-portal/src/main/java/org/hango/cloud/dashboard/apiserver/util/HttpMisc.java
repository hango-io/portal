package org.hango.cloud.dashboard.apiserver.util;

import com.google.common.collect.ImmutableSet;
import org.springframework.http.HttpHeaders;


public class HttpMisc {

    private final static ImmutableSet<String> CRITICAL_REQUEST_HEADER_KEYS = ImmutableSet.of(
            "content-length",
            "expect",
            "host",
            "proxy-authenticate",
            "proxy-authorization",
            "transfer-encoding",
            "connection",
            "upgrade",
            "http2-settings",
            "te",
            "keep-alive");

    private final static ImmutableSet<String> CRITICAL_RESPONSE_HEADER_KEYS = ImmutableSet.of(
            "content-length",
            "date",
            "expect",
            "host",
            "transfer-encoding",
            "connection",
            "upgrade",
            "http2-settings",
            "te",
            "keep-alive");

    public static boolean isNormalCode(int code, int... exNormalList) {
        if (code >= 200 && code <= 300) {
            return true;
        }
        for (int except : exNormalList) {
            if (code == except) {
                return true;
            }
        }
        return false;
    }

    public static HttpHeaders removeCriticalRequestHeaders(HttpHeaders headers) {
        return removeRequestHeaders(headers, CRITICAL_REQUEST_HEADER_KEYS);
    }

    public static HttpHeaders removeCriticalResponseHeaders(HttpHeaders headers) {
        return removeRequestHeaders(headers, CRITICAL_RESPONSE_HEADER_KEYS);
    }

    private static HttpHeaders removeRequestHeaders(HttpHeaders headers, ImmutableSet<String> headerKeys) {
        final HttpHeaders removed = new HttpHeaders();
        removed.putAll(headers);
        for (String key : headerKeys) {
            removed.remove(key);
        }
        return removed;
    }

}
