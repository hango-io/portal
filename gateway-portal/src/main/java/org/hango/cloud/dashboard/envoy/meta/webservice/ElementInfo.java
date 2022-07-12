package org.hango.cloud.dashboard.envoy.meta.webservice;

public class ElementInfo {
    private String name;
    private String namespace;
    private String qName;
    private String prefix;
    private String minOccurs;
    private String maxOccurs;
    private boolean nillable;
    private String defaultValue;
    private String fixedValue;
    private String arrayType;
    private boolean mayArrayType;
    private boolean mayOptional;
    private boolean mayNullable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getQName() {
        return qName;
    }

    public void setQName(String qName) {
        this.qName = qName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(String minOccurs) {
        this.minOccurs = minOccurs;
    }

    public String getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(String maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getArrayType() {
        return arrayType;
    }

    public void setArrayType(String arrayType) {
        this.arrayType = arrayType;
    }

    public boolean isMayArrayType() {
        return mayArrayType;
    }

    public void setMayArrayType(boolean mayArrayType) {
        this.mayArrayType = mayArrayType;
    }

    public boolean isMayOptional() {
        return mayOptional;
    }

    public void setMayOptional(boolean mayOptional) {
        this.mayOptional = mayOptional;
    }

    public boolean isMayNullable() {
        return mayNullable;
    }

    public void setMayNullable(boolean mayNullable) {
        this.mayNullable = mayNullable;
    }
}
