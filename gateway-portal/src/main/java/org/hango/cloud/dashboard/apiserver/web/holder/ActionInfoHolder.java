package org.hango.cloud.dashboard.apiserver.web.holder;

/**
 * 用户权限相关holder
 *
 * @author hanjiahao
 */
public class ActionInfoHolder {

    private static ThreadLocal<String> actionInfo = new ThreadLocal<>();

    public static String getAction() {
        return actionInfo.get();
    }

    public static void setAction(String action) {
        ActionInfoHolder.actionInfo.set(action);
    }

    public static void removeAction() {
        ActionInfoHolder.actionInfo.remove();
    }
}
