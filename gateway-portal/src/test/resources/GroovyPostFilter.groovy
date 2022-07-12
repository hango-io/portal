import com.netease.cloud.nce.api.gateway.dynamicFilter.GatewayFilter
import com.netease.cloud.nce.api.gateway.dynamicFilter.context.RequestContext
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletResponse

@Component
public class GroovyPostFilter extends GatewayFilter {

    @Override
    boolean shouldFilter() {
        return true;
    }

    @Override
    Object run() {
        println "This is post groovyfilter"

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        response.addHeader("GW", "APIGW");

        return null
    }

    @Override
    String filterType() {
        return "post"
    }

    @Override
    int filterOrder() {
        return 5
    }

}
