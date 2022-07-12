package org.hango.cloud.dashboard.apiserver.dto.alertdto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlertRuleListResponse {

    @JsonProperty("Result")
    private PageResult result;

    public PageResult getResult() {
        return result;
    }

    public void setResult(PageResult result) {
        this.result = result;
    }

    public static class PageResult {
        private List<AlertRuleDto> result;

        private long total;

        private int offset;

        private int limit;

        public List<AlertRuleDto> getResult() {
            return result;
        }

        public void setResult(List<AlertRuleDto> result) {
            this.result = result;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

}
