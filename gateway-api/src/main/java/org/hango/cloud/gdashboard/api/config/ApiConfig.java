package org.hango.cloud.gdashboard.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiConfig {

    @Value("${databasePrefix:nce_gateway_}")
    private String databasePrefix;

    public String getDatabasePrefix() {
        return databasePrefix;
    }

    public void setDatabasePrefix(String databasePrefix) {
        this.databasePrefix = databasePrefix;
    }
}
