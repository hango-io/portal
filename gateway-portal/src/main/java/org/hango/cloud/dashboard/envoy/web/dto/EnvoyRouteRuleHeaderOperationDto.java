package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/3/9
 */
public class EnvoyRouteRuleHeaderOperationDto {

    /**
     * 请求
     */
    @JSONField(name = "RequestOperation")
    @Valid
    private RequestOperation requestOperation;

    public RequestOperation getRequestOperation() {
        return requestOperation;
    }

    public void setRequestOperation(RequestOperation requestOperation) {
        this.requestOperation = requestOperation;
    }

    public class RequestOperation {

        @JSONField(name = "Add")
        @NotNull
        private Map<String, String> add;

        public Map<String, String> getAdd() {
            return add;
        }

        public void setAdd(Map<String, String> add) {
            this.add = add;
        }
    }


}
