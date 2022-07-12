package org.hango.cloud.dashboard.apiserver.service.impl.permission.handler;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/8/10
 */
public class PluginPostMethodSpecResourceHandler extends AbstractPluginSpecResourceHandler {

    private static final PluginPostMethodSpecResourceHandler HANDLER;

    private static final Logger logger = LoggerFactory.getLogger(PluginPostMethodSpecResourceHandler.class);


    static {
        HANDLER = new PluginPostMethodSpecResourceHandler();
    }

    PluginPostMethodSpecResourceHandler() {
    }

    public static PluginPostMethodSpecResourceHandler getInstance() {
        return HANDLER;
    }

    @Override
    public <R> R handle(HttpServletRequest request) {
        IEnvoyPluginInfoService pluginService = getPluginService(request);
        String pluginBindingInfoId = String.valueOf(getSpecResourceInfo(request, "PluginBindingInfoId"));
        EnvoyPluginBindingInfo pluginBindingInfo = pluginService.getPluginBindingInfo(NumberUtils.toLong(pluginBindingInfoId));
        if (pluginBindingInfo == null) {
            logger.info("AbstractPluginGetMethodSpecResourceHandler Intercept , Can Not Found PluginInfo , pluginBindingInfoId = {}", pluginBindingInfoId);
            return null;
        }
        String gwId = String.valueOf(pluginBindingInfo.getGwId());
        return (R) gwId;
    }

}
