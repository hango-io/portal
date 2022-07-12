package com.netease.cloud.nsf.server;

import org.apache.camel.*;
import org.apache.camel.support.LifecycleStrategySupport;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/26
 **/
public class CamelLifecycleListener extends LifecycleStrategySupport {
    private List<Service> registerServices;
    private List<RoutesBuilder> registerRoutes;
    private Map<String, Component> registerComponent;
    private ErrorHandlerFactory errorHandlerFactory;

    public void setRegisterServices(List<Service> registerServices) {
        this.registerServices = registerServices;
    }

    public void setRegisterRoutes(List<RoutesBuilder> registerRoutes) {
        this.registerRoutes = registerRoutes;
    }

    public void setRegisterComponent(Map<String, Component> registerComponent) {
        this.registerComponent = registerComponent;
    }

    public void setErrorHandlerFactory(ErrorHandlerFactory errorHandlerFactory) {
        this.errorHandlerFactory = errorHandlerFactory;
    }

    @Override
    public void onContextStart(CamelContext context) throws VetoCamelContextStartException {
        if (Objects.nonNull(registerServices)) {
            for (Service item : registerServices) {
                try {
                    context.addService(item);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(String.format("An exception occurred while registering the service:%s", item.getClass()), e);
                }
            }
        }
        if (Objects.nonNull(registerComponent)) {
            for (Map.Entry<String, Component> item : registerComponent.entrySet()) {
                context.addComponent(item.getKey(), item.getValue());
            }
        }
        if (Objects.nonNull(errorHandlerFactory)) {
            context.setErrorHandlerBuilder(errorHandlerFactory);
        }
        if (Objects.nonNull(registerRoutes)) {
            for (RoutesBuilder item : registerRoutes) {
                try {
                    context.addRoutes(item);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(String.format("An exception occurred while registering the route:%s", item.toString()), e);
                }
            }
        }
    }
}
