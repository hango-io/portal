package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/21
 */
public class SyncGetMethodSpecResourceHandler extends BaseSpecResourceHandler {

    private static final SyncGetMethodSpecResourceHandler HANDLER;

    static {
        HANDLER = new SyncGetMethodSpecResourceHandler();
    }

    SyncGetMethodSpecResourceHandler() {
    }

    public static SyncGetMethodSpecResourceHandler getInstance() {
        return HANDLER;
    }

    @Override
    public <R> R handle(HttpServletRequest request) {
        return (R) request.getParameter("DesGwId");
    }
}
