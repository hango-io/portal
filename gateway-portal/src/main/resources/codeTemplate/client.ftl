package ${packageName};

import java.io.IOException;

import com.netease.cloud.core.httpservice.SdkClient;
<#list actionList as attr>
    import com.netease.cloud.${serviceName}.model.${attr}Request;
    import com.netease.cloud.${serviceName}.model.${attr}Response;
</#list>

/**
*  @author com.neatese.cloud.apigataway
*/
public class ${serviceName?cap_first}Client extends SdkClient{

/**
* 以默认配置生成Client
*
*/
public ${serviceName?cap_first}Client createWithDefaultConfig() throws IOException {
setDefaultClientConfig();
return this;
}

/**
* 以默认认证信息生成Client
*
*/
public ${serviceName?cap_first}Client createWithDefaultCredential() throws IOException {
setDefaultCredential();
return this;
}

/**
* 以默认的认证信息和默认配置生成Client
*
*/
public ${serviceName?cap_first}Client createWithDefault() throws IOException {
setDefaultClientConfig();
setDefaultCredential();
return this;
}

<#list actionList as attr>
    public ${attr}Response ${attr?uncap_first}(${attr}Request ${attr?uncap_first}Request) {
    return super.execute(${attr?uncap_first}Request, ${attr}Response.class);
    }

</#list>

}