package org.hango.cloud.gdashboard.api.meta.swagger;

import java.io.Serializable;

public class SwaggerDetailsDto implements Serializable {
    private String swaggerType;
    private String swaggerName;
    private String swaggerSync;
    private String apiPath;
    private String apiMethod;
    private String message;

    public SwaggerDetailsDto(String swaggerType, String swaggerName, String swaggerSync) {
        this.swaggerType = swaggerType;
        this.swaggerName = swaggerName;
        this.swaggerSync = swaggerSync;
    }

    public SwaggerDetailsDto() {
    }

    public String getSwaggerType() {
        return swaggerType;
    }

    public void setSwaggerType(String swaggerType) {
        this.swaggerType = swaggerType;
    }

    public String getSwaggerName() {
        return swaggerName;
    }

    public void setSwaggerName(String swaggerName) {
        this.swaggerName = swaggerName;
    }

    public String getSwaggerSync() {
        return swaggerSync;
    }

    public void setSwaggerSync(String swaggerSync) {
        this.swaggerSync = swaggerSync;
    }


    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
