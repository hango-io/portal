package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/21
 */
public class GetMethodSpecResourceHandler extends BaseSpecResourceHandler {

    private static final GetMethodSpecResourceHandler HANDLER;

    static {
        HANDLER = new GetMethodSpecResourceHandler();
    }

    GetMethodSpecResourceHandler() {
    }

    public static GetMethodSpecResourceHandler getInstance() {
        return HANDLER;
    }

    @Override
    public <R> R handle(HttpServletRequest request) {
        return (R) request.getParameter("GwId");
    }
}
