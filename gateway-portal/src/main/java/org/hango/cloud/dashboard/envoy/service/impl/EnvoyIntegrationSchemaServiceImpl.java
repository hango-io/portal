package org.hango.cloud.dashboard.envoy.service.impl;

import org.hango.cloud.dashboard.envoy.dao.IEnvoyIntegrationSchemaDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationSchemaInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationSchemaService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationSchemaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 集成schema的Service实现
 */
@Service
public class EnvoyIntegrationSchemaServiceImpl implements IEnvoyIntegrationSchemaService {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationSchemaServiceImpl.class);

    @Autowired
    private IEnvoyIntegrationSchemaDao envoyIntegrationSchemaDao;


    @Override
    public List<EnvoyIntegrationSchemaInfo> getSchemaKindList() {
        return envoyIntegrationSchemaDao.getSchemaKindList();
    }

    @Override
    public EnvoyIntegrationSchemaInfo getSchemaByKind(String schemaKind) {
        return envoyIntegrationSchemaDao.getSchemaByKind(schemaKind);
    }

    @Override
    public EnvoyIntegrationSchemaDto fromMeta(EnvoyIntegrationSchemaInfo envoyIntegrationSchemaInfo) {
        EnvoyIntegrationSchemaDto dto = new EnvoyIntegrationSchemaDto();
        dto.setId(envoyIntegrationSchemaInfo.getId());
        dto.setCategory(envoyIntegrationSchemaInfo.getCategory());
        dto.setKind(envoyIntegrationSchemaInfo.getKind());
        dto.setName(envoyIntegrationSchemaInfo.getName());
        dto.setDescription(envoyIntegrationSchemaInfo.getDescription());
        dto.setSchema(envoyIntegrationSchemaInfo.getSchema());
        return dto;
    }
}
