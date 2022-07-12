package org.hango.cloud.dashboard.apiserver.service.impl;

import org.hango.cloud.dashboard.apiserver.aop.DynamicAuditBean;
import org.hango.cloud.dashboard.apiserver.config.ElasticSearchConfig;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditQuery;
import org.hango.cloud.dashboard.apiserver.meta.audit.CallStatisticsInfo;
import org.hango.cloud.dashboard.apiserver.service.IAuditService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 审计多数据源基类
 * @date 2019/3/28
 */
@Component
@Primary
public class AuditServiceImpl implements IAuditService {

    protected static final String START_TIME = "startTime";
    protected static final String END_TIME = "endTime";
    protected static final String ERROR_TYPE = "errorType";
    protected static final String PROJECT_DIVIDED = "projectDivided";
    /**
     * 一天的毫秒值
     */
    protected static final Integer TIMESTAMP_ONE_DAY = 24 * 60 * 60 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    protected static String[] DEFAULT_IGNORE_PROPERTIES;

    static {
        DEFAULT_IGNORE_PROPERTIES = new String[]{START_TIME, END_TIME, ERROR_TYPE, PROJECT_DIVIDED};
    }

    @Autowired
    protected IGatewayInfoService gatewayInfoService;

    @Autowired
    protected ElasticSearchConfig elasticSearchConfig;

    @Override
    @DynamicAuditBean
    public CallStatisticsInfo getAuditStatisticsInfo(AuditQuery auditQuery, GatewayInfo gatewayInfo) {
        return null;
    }

    @Override
    @DynamicAuditBean
    public String getAuditDetail(String id, String auditIndex, GatewayInfo gatewayInfo) {
        return null;
    }

    @Override
    @DynamicAuditBean
    public Page<AuditInfo> getAuditInfoList(AuditQuery auditQuery, GatewayInfo gatewayInfo) {
        return null;
    }

    @Override
    @DynamicAuditBean
    public long countAuditInfo(AuditQuery auditQuery, GatewayInfo gatewayInfo) {
        return 0;
    }

    @Component
    class AuditDataSourceCommandRunner implements CommandLineRunner {
        @Autowired
        protected IGatewayInfoService gatewayInfoService;
        @Autowired
        protected ElasticSearchConfig elasticSearchConfig;

        @Override
        public void run(String... strings) throws Exception {
            List<GatewayInfo> all = gatewayInfoService.findAll();
            if (CollectionUtils.isEmpty(all)) {
                return;
            }

            for (GatewayInfo gatewayInfo : all) {
                String gwId = String.valueOf(gatewayInfo.getId());

                try {
                    switch (gatewayInfo.getAuditDatasourceSwitch()) {
                        case Const.AUDIT_DATASOURCE_ELASTICSEARCH:
                            elasticSearchConfig.getElasticsearchTemplateByGwId(gwId);
                        default:
                            break;
                    }
                } catch (Throwable e) {
                    logger.error("审计数据源加载失败，网关ID : {},数据源类型 : {}", gwId, gatewayInfo.getAuditDatasourceSwitch());
                }

            }
        }
    }
}
