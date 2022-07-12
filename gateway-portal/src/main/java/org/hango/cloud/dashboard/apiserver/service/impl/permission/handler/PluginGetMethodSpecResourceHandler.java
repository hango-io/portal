package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/8/10
 */
@Component
public class PluginGetMethodSpecResourceHandler extends AbstractPluginSpecResourceHandler {

    private static final PluginGetMethodSpecResourceHandler HANDLER;

    private static final Logger logger = LoggerFactory.getLogger(PluginGetMethodSpecResourceHandler.class);

    static {
        HANDLER = new PluginGetMethodSpecResourceHandler();
    }

    PluginGetMethodSpecResourceHandler() {
    }

    public static PluginGetMethodSpecResourceHandler getInstance() {
        return HANDLER;
    }

    @Override
    public <R> R handle(HttpServletRequest request) {
        IEnvoyPluginInfoService pluginService = getPluginService(request);
        String pluginBindingInfoId = request.getParameter("PluginBindingInfoId");
        EnvoyPluginBindingInfo pluginBindingInfo = pluginService.getPluginBindingInfo(NumberUtils.toLong(pluginBindingInfoId));
        if (pluginBindingInfo == null) {
            logger.info("AbstractPluginGetMethodSpecResourceHandler Intercept , Can Not Found PluginInfo , pluginBindingInfoId = {}", pluginBindingInfoId);
            return null;
        }
        return (R) String.valueOf(pluginBindingInfo.getGwId());
    }
}
