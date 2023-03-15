package org.hango.cloud.common.infra.plugin.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.jetbrains.annotations.NotNull;
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
    private IPluginInfoService pluginInfoService;

    private final LoadingCache<String, String> pluginTypeToNameCache = CacheBuilder.newBuilder().maximumSize(1000)
            .refreshAfterWrite(1, TimeUnit.DAYS).build(
                    new CacheLoader<String, String>() {
                        @NotNull
                        @Override
                        public String load(@NotNull final String pluginType) throws Exception {
                            PluginInfo pluginInfo = pluginInfoService.getPluginInfoFromDataPlane(pluginType);
                            return pluginInfo == null ? pluginType : pluginInfo.getPluginName();
                        }
                    });


    public String getPluginNameFromCache(String pluginType) {
        try {
            return pluginTypeToNameCache.get(pluginType);
        } catch (ExecutionException e) {
            logger.error("get plugin name from plugin type error, e:", e);
        } catch (Exception e) {
            logger.error("get plugin name error, this pluginType [{}] may not exist e:{}", pluginType, e);
        }
        return pluginType;
    }
}
