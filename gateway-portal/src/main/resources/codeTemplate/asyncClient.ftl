package ${packageName};

import java.io.IOException;
import java.util.concurrent.Future;

import com.netease.cloud.core.httpservice.SdkCallBack;
import com.netease.cloud.core.httpservice.HttpTask;
import com.netease.cloud.core.httpservice.SdkAsyncClient;
<#list actionList as attr>
    import com.netease.cloud.${serviceName}.model.${attr}Request;
    import com.netease.cloud.${serviceName}.model.${attr}Response;
</#list>

/**
*  @author com.neatese.cloud.apigataway
*/

public class ${serviceName?cap_first}AsyncClient extends SdkAsyncClient{

/**
* 以默认配置生成Client
*
*/
public ${serviceName?cap_first}AsyncClient createWithDefaultConfig() throws IOException {
setDefaultClientConfig();
return this;
}

/**
* 以默认认证信息生成Client
*
*/
public ${serviceName?cap_first}AsyncClient createWithDefaultCredential() throws IOException {
setDefaultCredential();
return this;
}

/**
* 以默认的认证信息和默认配置生成Client
*
*/
public ${serviceName?cap_first}AsyncClient createWithDefault() throws IOException {
setDefaultClientConfig();
setDefaultCredential();
return this;
}

<#list actionList as attr>
public Future
<${attr}Response> ${attr?uncap_first}(${attr}Request ${attr?uncap_first}Request, SdkCallBack
    <${attr}Response> sdkCallBack) {
        HttpTask
        <${attr}Response> httpTask = new HttpTask
            <${attr}Response>(sdkCallBack, ${attr}Response.class);
                return super.executeAsync(${attr?uncap_first}Request, httpTask);
                }

                </#list>
                }