package com.netease.cloud.nsf.parser;

import org.apache.camel.util.URISupport;
import org.dom4j.Element;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parser基础类，提供Parser基础公共方法
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public class BaseParser {
    private static final String GLOBAL_PROPERTY = "GlobalProperty";
    protected static final String GLOBAL_INTEGRATION_ID = "GlobalIntegrationId";

    /**
     * 使用该方法代替Element.addElement方法，
     * 该方法能够为该步骤添加对应的Step Id，提供后续日志打印用
     *
     * @param parent
     * @param step
     * @param name
     * @return
     */
    protected Element addElement(Element parent, StepNode step, String name) {
        Element ret = parent.addElement(name);
        if (Objects.nonNull(step) && Objects.nonNull(step.get())) {
            String id = step.get().getId();
            if (Objects.nonNull(id)) {
                // stepId规范：integrationId/stepId
                ret.addAttribute("id", String.format("%s/%s", getIntegrationId(step), id));
            }
        }
        return ret;
    }

    protected void parseNext(Element current, StepNode step, ParserContext parserContext) {
        if (Objects.isNull(step) || Objects.isNull(step.next()) || Objects.isNull(current)) {
            return;
        }
        if (Objects.nonNull(step.next())) {
            parserContext.getParser(step.next()).parse(current, step.next(), parserContext);
        }
    }

    protected void parseChild(Element parent, StepNode step, ParserContext parserContext) {
        if (Objects.isNull(step) || Objects.isNull(step.firstChild()) || Objects.isNull(parent)) {
            return;
        }
        if (Objects.nonNull(step.firstChild())) {
            parserContext.getParser(step.firstChild()).parse(parent, step.firstChild(), parserContext);
        }
    }

    protected String createQueryString(Map<String, Object> options) {
        try {
            return URISupport.createQueryString(options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected String appendParametersToURI(String uri, Map<String, Object> uriOptions) {
        try {
            return URISupport.appendParametersToURI(uri, uriOptions);
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void setGlobalProperty(StepNode step, String key, Object value) {
        if (Objects.isNull(step) || Objects.isNull(key)) {
            return;
        }
        if (Objects.isNull(step.root())) {
            throw new RuntimeException("there is no root stepNode when set global property");
        }
        if (Objects.isNull(step.root().get().getProperty().get(GLOBAL_PROPERTY))) {
            step.root().get().getProperty().add(GLOBAL_PROPERTY, new HashMap<>());
        }
        step.root().get().getProperty().get(GLOBAL_PROPERTY, Map.class).put(key, value);
    }

    protected Object getGlobalProperty(StepNode step, String key) {
        if (Objects.isNull(step) || Objects.isNull(key) || Objects.isNull(step.root())) {
            return null;
        }
        if (Objects.isNull(step.root().get().getProperty().get(GLOBAL_PROPERTY))) {
            return null;
        }
        return step.root().get().getProperty().get(GLOBAL_PROPERTY, Map.class).get(key);
    }

    protected String getIntegrationId(StepNode stepNode) {
        Object integrationId = getGlobalProperty(stepNode, GLOBAL_INTEGRATION_ID);
        if (Objects.isNull(integrationId)) return null;
        return String.valueOf(integrationId);
    }

    protected void setHeader(Element parent, StepNode stepNode, String key, String value) {
        Element setHeader = addElement(parent, stepNode, "setHeader");
        setHeader.addAttribute("headerName", key);
        Element constant = addElement(setHeader, stepNode, "constant");
        constant.setText(value);
    }

    protected void setSimpleHeader(Element parent, StepNode stepNode, String key, String value) {
        Element setHeader = addElement(parent, stepNode, "setHeader");
        setHeader.addAttribute("headerName", key);
        Element constant = addElement(setHeader, stepNode, "simple");
        constant.setText(value);
    }

    protected void setBody(Element parent, StepNode stepNode, String value) {
        Element setBody = addElement(parent, stepNode, "setBody");
        Element constant = addElement(setBody, stepNode, "constant");
        constant.setText(value);
    }

    protected void setSimpleBody(Element parent, StepNode stepNode, String value) {
        Element setBody = addElement(parent, stepNode, "setBody");
        Element constant = addElement(setBody, stepNode, "simple");
        constant.setText(value);
    }

    protected void removeHeaders(Element parent, StepNode stepNode, String pattern) {
        Element removeHeaders = addElement(parent, stepNode, "removeHeaders");
        removeHeaders.addAttribute("pattern", pattern);
    }
}
