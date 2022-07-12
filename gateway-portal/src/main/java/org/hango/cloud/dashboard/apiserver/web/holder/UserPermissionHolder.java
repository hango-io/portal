package org.hango.cloud.dashboard.apiserver.web.holder;

/**
 * 用户权限相关holder
 *
 * @author hanjiahao
 */
public class UserPermissionHolder {
    public static final String USER_PERMISSION = "x-auth-token";
    public static final String USER_ACCOUNTID = "x-auth-accountId";

    private static ThreadLocal<String> accountId = new ThreadLocal<>();

    private static ThreadLocal<String> jwt = new ThreadLocal<>();

    public static String getAccountId() {
        return accountId.get();
    }

    public static void setAccountId(String accountId) {
        UserPermissionHolder.accountId.set(accountId);
    }

    public static String getJwt() {
        return jwt.get();
    }

    public static void setJwt(String jwt) {
        UserPermissionHolder.jwt.set(jwt);
    }

    public static void removePermission() {
        UserPermissionHolder.accountId.remove();
        UserPermissionHolder.jwt.remove();
    }
}
