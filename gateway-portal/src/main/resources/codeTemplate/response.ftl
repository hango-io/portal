package ${packageName};

import com.netease.cloud.core.model.SdkResponse;
<#list importList as import>
    import ${import};
</#list>

/**
*  @author com.neatese.cloud.apigataway
*/

public class ${action}Response extends SdkResponse {
// 响应头response header参数
<#list responseHeaderList as attr>
    private String ${attr.name};
</#list>

// 响应体response body参数
<#list responseBodyList as attr>
    <#if attr.name !="Code" && attr.name !="Message" && attr.name != "RequestId">
        <#if attr.type = "Number">
            private ${attr.type} ${attr.name};  //不推荐Number类型，尽量将Number型参数修改为基本数字类型
        <#else>
            private ${attr.type} ${attr.name};
        </#if>
    </#if>
</#list>

<#list responseBodyList as attr>
    <#if attr.name !="Code" && attr.name !="Message" && attr.name != "RequestId">
        public ${attr.type} get${attr.name}() {
        return ${attr.name};
        }

        public void set${attr.name}(${attr.type} ${attr.name}) {
        this.${attr.name} = ${attr.name};
        }

    </#if>
</#list>

@Override
public String toString() {
StringBuilder stringBuilder = new StringBuilder();
<#list responseHeaderList as attr>
    stringBuilder.append("${attr.name}:" + ${attr.name} + "\n");
</#list>
<#list responseBodyList as attr>
    stringBuilder.append("${attr.name}:" + ${attr.name} + "\n");
</#list>
return stringBuilder.toString();

}

}