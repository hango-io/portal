package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class EnvoyServiceWsdlElementDto {
    @JSONField(name = "Name")
    private String name;

    @JSONField(name = "Namespace")
    private String namespace;

    @JSONField(name = "QName")
    private String qName;

    @JSONField(name = "Prefix")
    private String prefix;

    @JSONField(name = "MinOccurs")
    private String minOccurs;

    @JSONField(name = "MaxOccurs")
    private String maxOccurs;

    @JSONField(name = "Nillable")
    private boolean nillable;

    @JSONField(name = "DefaultValue")
    private String defaultValue;

    @JSONField(name = "FixedValue")
    private String fixedValue;

    @JSONField(name = "ArrayType")
    private String arrayType;

    @JSONField(name = "MayArrayType")
    private boolean mayArrayType;

    @JSONField(name = "MayOptional")
    private boolean mayOptional;

    @JSONField(name = "MayNullable")
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
