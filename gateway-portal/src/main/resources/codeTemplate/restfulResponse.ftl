package ${packageName};
import com.netease.cloud.restful.sdk.core.http.APIResponse;
<#list importList as import>
    import ${import};
</#list>

/**
*  @author com.neatese.cloud.apigataway
*/

public class ${className? cap_first}Response extends APIResponse {
<#if responseHeaderList?size gt 0>
    <#list responseHeaderList as attr>
        private String ${attr.name};
    </#list>
</#if>
<#if responseBodyList?size gt 0>
    <#list responseBodyList as attr>
        <#if attr.name != "RequestId">
            <#if attr.type = "Number">
                private ${attr.type} ${attr.name};  //不推荐Number类型，尽量将Number型参数修改为基本数字类型
            <#else>
                private ${attr.type} ${attr.name};
            </#if>
        </#if>
    </#list>
</#if>
<#if responseBodyList?size gt 0>
    <#list responseBodyList as attr>
        <#if attr.name != "RequestId">
            public ${attr.type} get${attr.name? cap_first}() {
            return ${attr.name};
            }

            public void set${attr.name? cap_first}(${attr.type} ${attr.name}) {
            this.${attr.name} = ${attr.name};
            }

        </#if>
    </#list>
</#if>


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