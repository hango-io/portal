package org.hango.cloud.dashboard.apiserver.config;


import com.alibaba.fastjson.JSON;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.hango.cloud.dashboard.apiserver.exception.AuditDataSourceException;
import org.hango.cloud.dashboard.apiserver.meta.AuditElasticSearchProperties;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/15
 */
@Component
public class ElasticSearchConfig {

    private static Logger logger = LoggerFactory.getLogger(ElasticSearchConfig.class);

    private Map<String, JestClient> elasticsearchTemplateMap = new HashMap<>();
    @Autowired
    private IGatewayInfoService gatewayInfoService;

    /**
     * 通过网关id获取elasticsearchTemplate，进行审计
     */
    public JestClient getElasticsearchTemplateByGwId(String gwId) {
        JestClient jestClient = elasticsearchTemplateMap.get(gwId);
        if (null != jestClient) {
            return jestClient;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(NumberUtils.toLong(gwId));
        if (gatewayInfo != null && StringUtils.isNotBlank(gatewayInfo.getAuditDbConfig())) {
            try {

                JestClientFactory factory = new JestClientFactory();
                factory.setHttpClientConfig(createHttpClientConfig(
                        JSON.parseObject(gatewayInfo.getAuditDbConfig(), AuditElasticSearchProperties.class)));
                jestClient = factory.getObject();
                elasticsearchTemplateMap.put(gwId, jestClient);
                return jestClient;
            } catch (Throwable e) {
                logger.warn("初始化审计ElasticSearch数据源失败");
                throw new AuditDataSourceException(Const.AUDIT_DATASOURCE_ELASTICSEARCH);
            }
        } else {
            logger.warn("未找到审计ElasticSearch数据源");
            throw new AuditDataSourceException(Const.AUDIT_DATASOURCE_ELASTICSEARCH);
        }
    }

    protected HttpClientConfig createHttpClientConfig(AuditElasticSearchProperties properties) {
        HttpClientConfig.Builder builder = new HttpClientConfig.Builder(
                properties.getUris());
        if (StringUtils.isNotBlank(properties.getUsername())) {
            builder.defaultCredentials(properties.getUsername(),
                    properties.getPassword());
        }
        String proxyHost = properties.getProxy().getHost();
        if (StringUtils.isNotBlank(proxyHost)) {
            Integer proxyPort = properties.getProxy().getPort() == 0 ? 80 : properties.getProxy().getPort();
            builder.proxy(new HttpHost(proxyHost, proxyPort));
        }
        builder.multiThreaded(properties.isMultiThreaded());
        builder.connTimeout(properties.getConnectionTimeout())
                .readTimeout(properties.getReadTimeout());
        builder.discoveryEnabled(properties.getDiscoveryEnable());
        builder.discoveryFrequency(properties.getDiscoveryFrequency(), TimeUnit.MILLISECONDS);
        return builder.build();
    }


    /**
     * 删除某网关下的elasticsearchTemplate
     */
    public void removeTemplateByGwId(String gwId) {
        JestClient jestClient = elasticsearchTemplateMap.get(gwId);
        if (null != jestClient) {
            try {
                jestClient.close();
                elasticsearchTemplateMap.remove(gwId);
            } catch (IOException e) {
                logger.warn("RestHighLevelClient 注销失败");
            }

        }
    }
}
