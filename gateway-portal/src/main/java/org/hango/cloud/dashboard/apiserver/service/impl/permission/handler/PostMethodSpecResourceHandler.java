package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/21
 */
public class PostMethodSpecResourceHandler extends BaseSpecResourceHandler {

    private static final PostMethodSpecResourceHandler HANDLER;

    static {
        HANDLER = new PostMethodSpecResourceHandler();
    }

    PostMethodSpecResourceHandler() {
    }

    public static PostMethodSpecResourceHandler getInstance() {
        return HANDLER;
    }

    @Override
    public <R> R handle(HttpServletRequest request) {

        Object gwIdObs = getSpecResourceInfo(request, "GwIds");
        if (gwIdObs instanceof Collection) {
            return (R) gwIdObs;
        }
        return (R) getSpecResourceInfo(request, "GwId");
    }
}
