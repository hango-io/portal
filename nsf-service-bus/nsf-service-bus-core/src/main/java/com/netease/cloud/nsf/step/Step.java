package com.netease.cloud.nsf.step;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/21
 **/
public class Step {
    protected String id;
    protected String name;
    protected String stepKind;
    @JsonUnwrapped
    protected Property property;
    protected Steps childSteps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStepKind() {
        return stepKind;
    }

    public void setStepKind(String stepKind) {
        this.stepKind = stepKind;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Steps getChildSteps() {
        return childSteps;
    }

    public void setChildSteps(Steps childSteps) {
        this.childSteps = childSteps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(id, step.id) &&
                Objects.equals(name, step.name) &&
                Objects.equals(stepKind, step.stepKind) &&
                Objects.equals(property, step.property) &&
                Objects.equals(childSteps, step.childSteps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, stepKind, property, childSteps);
    }
}
