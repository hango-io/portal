package ${packageName};
import com.netease.cloud.restful.sdk.core.http.APIRequestMeta;
<#list importList as import>
    import ${import};
</#list>
/**
*  @author com.neatese.cloud.apigataway
*/
public class ${className? cap_first}Request extends APIRequestMeta {
<#--======================参数声明========================-->
<#if requestQueryStringList?size gt 0>
    //请求的queryString
    <#list requestQueryStringList as attr>
        <#if attr.type = "Number">
            private ${attr.type} ${attr.name};
        <#else>
            private ${attr.type} ${attr.name};
        </#if>
    </#list>
</#if>
<#if requestHeaderList?size gt 0>
    //请求hearder
    <#list requestHeaderList as attr>
        private String ${attr.name};
    </#list>
</#if>
<#if requestBodyList?size gt 0>
    //请求body
    <#list requestBodyList as attr>
        <#if attr.type = "Number">
            private ${attr.type} ${attr.name};
        <#else>
            private ${attr.type} ${attr.name};
        </#if>
    </#list>
</#if>
<#if requestBodyList?size gt 0>
    //请求path变量
    <#list requestPathStringList as attr>
        private ${attr.type} ${attr.name};
    </#list>
</#if>

private ${className? cap_first}Request (Builder builder) {
this.uri = "/"+"${serviceName}"+"${uri}";
this.httpMethod = "${apiMethod}";
<#if requestQueryStringList?size gt 0>
    <#list requestQueryStringList as attr>
        this.${attr.name} = builder.${attr.name};
        params.put("${attr.name}", ${attr.name});
    </#list>
</#if>
<#if requestHeaderList?size gt 0>
    <#list requestHeaderList as attr>
        this.${attr.name} = builder.${attr.name};
        headers.put("${attr.name?replace('_','-')}", ${attr.name});
    </#list>
</#if>
<#if requestBodyList?size gt 0>
    <#list requestBodyList as attr>
        this.${attr.name} = builder.${attr.name};
        bodyParams.put("${attr.name}", ${attr.name});
    </#list>
</#if>
<#if requestPathStringList?size gt 0>
    <#list requestPathStringList as attr>
        this.${attr.name} = builder.${attr.name};
        pathParams.put("${attr.name}", ${attr.name});
    </#list>
</#if>
}

<#--=======================参数gettersetter=====================-->
<#if requestQueryStringList?size gt 0>
    <#list requestQueryStringList as attr>
        public void set${attr.name? cap_first}(${attr.type} ${attr.name}) {
        this.${attr.name} = ${attr.name};
        params.put("${attr.name}", ${attr.name});
        }

        public ${attr.type} get${attr.name? cap_first}() {
        return this.${attr.name};
        }
    </#list>
</#if>
<#if requestHeaderList?size gt 0>
    <#list requestHeaderList as attr>
        public void set${attr.name? cap_first}(String ${attr.name}) {
        this.${attr.name} = ${attr.name};
        headers.put("${attr.name?replace('_','-')}", ${attr.name});
        }

        public String get${attr.name? cap_first}() {
        return this.${attr.name};
        }

    </#list>
</#if>
<#if requestBodyList?size gt 0>
    <#list requestBodyList as attr>
        public void set${attr.name? cap_first}(${attr.type} ${attr.name}) {
        this.${attr.name} = ${attr.name};
        bodyParams.put("${attr.name}", ${attr.name});
        }

        public ${attr.type} get${attr.name? cap_first}() {
        return this.${attr.name};
        }

    </#list>
</#if>

public static Builder builder() {
return new Builder();
}

@Override
public String toString() {
return "${className}Request [params=" + params + ", headers=" + headers + ", bodyParams=" + bodyParams
+ ",  uri=" + uri;
}

public static class Builder{
<#--======================参数声明========================-->
<#if requestQueryStringList?size gt 0>
    <#list requestQueryStringList as attr>
        private ${attr.type} ${attr.name};
    </#list>
</#if>
<#if requestHeaderList?size gt 0>
    <#list requestHeaderList as attr>
        <#if attr.value = "">
            private String ${attr.name};
        <#else>
            private String ${attr.name} = "${attr.value}";
        </#if>
    </#list>
</#if>
<#if requestBodyList?size gt 0>
    <#list requestBodyList as attr>
        private ${attr.type} ${attr.name};
    </#list>
</#if>
<#if requestPathStringList?size gt 0>
    <#list requestPathStringList as attr>
        private ${attr.type} ${attr.name};
    </#list>
</#if>

<#--=======================参数gettersetter=====================-->
<#if requestQueryStringList?size gt 0>
    <#list requestQueryStringList as attr>
        public Builder ${attr.name}(${attr.type} ${attr.name}) {
        this.${attr.name} = ${attr.name};
        return this;
        }
    </#list>
</#if>
<#if requestHeaderList?size gt 0>
    <#list requestHeaderList as attr>
        <#if attr.value = "">
        <#--header没有默认值则提供设值方法-->
            public Builder ${attr.name}(String ${attr.name}) {
            this.${attr.name} = ${attr.name};
            return this;
            }
        </#if>
    </#list>
</#if>
<#if requestBodyList?size gt 0>
    <#list requestBodyList as attr>
        public Builder ${attr.name}(${attr.type} ${attr.name}) {
        this.${attr.name} = ${attr.name};
        return this;
        }

    </#list>
</#if>
<#if requestBodyList?size gt 0>
    <#list requestPathStringList as attr>
        public Builder ${attr.name}(${attr.type} ${attr.name}) {
        this.${attr.name} = ${attr.name};
        return this;
        }

    </#list>
</#if>

// build方法
public ${className? cap_first}Request build() {
return new ${className? cap_first}Request(this);
}

}
}