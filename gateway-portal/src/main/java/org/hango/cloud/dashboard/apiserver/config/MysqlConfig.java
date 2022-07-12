package org.hango.cloud.dashboard.apiserver.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.exception.AuditDataSourceException;
import org.hango.cloud.dashboard.apiserver.meta.AuditDatabaseProperties;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/3/27
 */
@Component
public class MysqlConfig {

    private static Logger logger = LoggerFactory.getLogger(MysqlConfig.class);

    private Map<String, NamedParameterJdbcTemplate> namedParameterJdbcTemplateMap = new HashMap<>();
    @Autowired
    private IGatewayInfoService gatewayInfoService;


    /**
     * 通过网关id获取NamedParameterJdbcTemplate，进行审计
     *
     * @param gwId
     * @return
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplateByGwId(String gwId) {

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = namedParameterJdbcTemplateMap.get(gwId);
        if (null != namedParameterJdbcTemplate) {
            return namedParameterJdbcTemplate;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(NumberUtils.toLong(gwId));
        if (gatewayInfo != null && StringUtils.isNotBlank(gatewayInfo.getAuditDbConfig())) {
            AuditDatabaseProperties databaseProperties = JSON.parseObject(gatewayInfo.getAuditDbConfig(), AuditDatabaseProperties.class);
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setInitialSize(databaseProperties.getInitialSize());
            dataSource.setMaxIdle(32);
            dataSource.setMaxTotal(32);
            dataSource.setMinIdle(32);
            dataSource.setDriverClassName(databaseProperties.getDriverClassName());
            dataSource.setPassword(databaseProperties.getPassword());
            dataSource.setUrl(databaseProperties.getUrl());
            dataSource.setUsername(databaseProperties.getUsername());
            dataSource.setTestWhileIdle(databaseProperties.getTestWhileIdle());
            dataSource.setTimeBetweenEvictionRunsMillis(databaseProperties.getTimeBetweenEvictionRunsMillis());
            dataSource.setValidationQuery(databaseProperties.getValidationQuery());
            dataSource.setTestOnBorrow(databaseProperties.getTestOnBorrow());
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            namedParameterJdbcTemplateMap.put(gwId, namedParameterJdbcTemplate);
            return namedParameterJdbcTemplate;
        }
        logger.warn("未找到审计Mysql数据源");
        throw new AuditDataSourceException(Const.AUDIT_DATASOURCE_MYSQL);
    }

    /**
     * 删除某网关下的NamedParameterJdbcTemplate
     *
     * @param gwId
     */
    public void removeTemplateByGwId(String gwId) {
        NamedParameterJdbcTemplate NamedParameterJdbcTemplate = namedParameterJdbcTemplateMap.get(gwId);
        if (null != NamedParameterJdbcTemplate) {
            namedParameterJdbcTemplateMap.remove(gwId);
        }
    }
}
