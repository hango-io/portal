package org.hango.cloud.envoy.advanced.metric.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/15
 */
public class PromResponse {

    private String status;
    private Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class Data {
        private String resultType;
        private List<Result> result;

        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public List<Result> getResult() {
            return result;
        }

        public void setResult(List<Result> result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public static class Result {
            private Map<String, String> metric;
            //query
            private List<String> value;
            //query_range
            private List<List<String>> values;

            public Map<String, String> getMetric() {
                return metric;
            }

            public void setMetric(Map<String, String> metric) {
                this.metric = metric;
            }

            public List<String> getValue() {
                return value;
            }

            public void setValue(List<String> value) {
                this.value = value;
            }

            public List<List<String>> getValues() {
                return values;
            }

            public void setValues(List<List<String>> values) {
                this.values = values;
            }

            @Override
            public String toString() {
                return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
            }
        }

    }

}
