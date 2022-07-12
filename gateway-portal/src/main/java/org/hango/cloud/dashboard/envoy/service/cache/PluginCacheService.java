package org.hango.cloud.dashboard.envoy.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class PluginCacheService {

    private static final Logger logger = LoggerFactory.getLogger(PluginCacheService.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;


    private LoadingCache<String, String> pluginTypeToNameCache = CacheBuilder.newBuilder().maximumSize(1000)
            .refreshAfterWrite(1, TimeUnit.DAYS).build(
                    new CacheLoader<String, String>() {
                        @Override
                        public String load(final String pluginType) throws Exception {
                            GatewayInfo gatewayInfo = gatewayInfoService.findAll().get(0);
                            if (gatewayInfo != null) {
                                return envoyPluginInfoService.getPluginInfoFromApiPlane(gatewayInfo.getId(), pluginType).getPluginName();
                            }
                            return pluginType;
                        }
                    });


    public String getPluginNameFromCache(String pluginType) {
        try {
            return pluginTypeToNameCache.get(pluginType);
        } catch (ExecutionException e) {
            logger.error("get plugin name from plugin type error, e:{}", e);
        } catch (Exception e) {
            logger.error("get plugin name error, this pluginType [{}] may not exist e:{}", pluginType, e);
        }
        return pluginType;
    }

}
