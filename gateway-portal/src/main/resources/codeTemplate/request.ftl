package ${packageName};

import com.netease.cloud.core.model.SdkRequest;
<#list importList as import>
    import ${import};
</#list>

/**
*  @author com.neatese.cloud.apigataway
*/

public class ${action}Request extends SdkRequest {
<#--======================参数声明========================-->

// 请求字符串query string参数
<#list requestQueryStringList as attr>
    <#if attr.type = "Number">
        private ${attr.type} ${attr.name};  //不推荐Number类型，尽量将Number型参数修改为基本数字类型
    <#else>
        private ${attr.type} ${attr.name};
    </#if>
</#list>

// 请求头request header参数
<#list requestHeaderList as attr>
    private String ${attr.name};
</#list>

// 请求体request body参数
<#list requestBodyList as attr>
    <#if attr.type = "Number">
        private ${attr.type} ${attr.name};  //不推荐Number类型，尽量将Number型参数修改为基本数字类型
    <#else>
        private ${attr.type} ${attr.name};
    </#if>
</#list>

// 构造方法
private ${action}Request (Builder builder) {
this.action = "${action}";
this.version = "${version}";
this.serviceName = "${serviceName}";
this.canonicalURI = "${apiPath}";
this.httpRequestMethod = "${apiMethod}";
queryParameters.put("Action", action);
queryParameters.put("Version", version);

<#list requestQueryStringList as attr>
    this.${attr.name} = builder.${attr.name};
    queryParameters.put("${attr.name}", ${attr.name});
</#list>

<#list requestHeaderList as attr>
    this.${attr.name} = builder.${attr.name};
    headers.put("${attr.name?replace('_','-')}", ${attr.name});
</#list>

<#list requestBodyList as attr>
    this.${attr.name} = builder.${attr.name};
    bodyParameters.put("${attr.name}", ${attr.name});
</#list>
}

<#--=======================参数gettersetter=====================-->
// 请求字符串query string取设值方法
<#list requestQueryStringList as attr>
    public void set${attr.name}(${attr.type} ${attr.name}) {
    this.${attr.name} = ${attr.name};
    queryParameters.put("${attr.name}", ${attr.name});
    }

    public ${attr.type} get${attr.name}() {
    return this.${attr.name};
    }

</#list>

// 请求头request header取设值方法
<#list requestHeaderList as attr>
    public void set${attr.name}(String ${attr.name}) {
    this.${attr.name} = ${attr.name};
    headers.put("${attr.name?replace('_','-')}", ${attr.name});
    }

    public String get${attr.name}() {
    return this.${attr.name};
    }

</#list>

// 请求体request body取设值方法
<#list requestBodyList as attr>
    public void set${attr.name}(${attr.type} ${attr.name}) {
    this.${attr.name} = ${attr.name};
    bodyParameters.put("${attr.name}", ${attr.name});
    }

    public ${attr.type} get${attr.name}() {
    return this.${attr.name};
    }

</#list>


public static Builder builder() {
return new Builder();
}


@Override
public String toString() {
return "${action}Request [queryParameters=" + queryParameters + ", headers=" + headers + ", bodyParameters=" + bodyParameters
+ ",  action=" + action + ", version=" + version;
}


public static class Builder{
<#--======================参数声明========================-->

// 请求字符串query string参数
<#list requestQueryStringList as attr>
    private ${attr.type} ${attr.name};
</#list>

// 请求头request header参数
<#list requestHeaderList as attr>
    <#if attr.value = "">
        private String ${attr.name};
    <#else>
        private String ${attr.name} = "${attr.value}";
    </#if>
</#list>

// 请求体request body参数
<#list requestBodyList as attr>
    private ${attr.type} ${attr.name};
</#list>

<#--=======================参数gettersetter=====================-->
// 请求字符串query string设值方法
<#list requestQueryStringList as attr>
    public Builder ${attr.name}(${attr.type} ${attr.name}) {
    this.${attr.name} = ${attr.name};
    return this;
    }

</#list>

// 请求头request header设值方法
<#list requestHeaderList as attr>
    <#if attr.value = "">
    <#--header没有默认值则提供设值方法-->
        public Builder ${attr.name}(String ${attr.name}) {
        this.${attr.name} = ${attr.name};
        return this;
        }
    </#if>
</#list>

// 请求体request body设值方法
<#list requestBodyList as attr>
    public Builder ${attr.name}(${attr.type} ${attr.name}) {
    this.${attr.name} = ${attr.name};
    return this;
    }

</#list>

// build方法
public ${action}Request build() {
return new ${action}Request(this);
}

}
}