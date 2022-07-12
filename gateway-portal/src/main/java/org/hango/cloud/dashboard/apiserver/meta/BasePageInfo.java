package org.hango.cloud.dashboard.apiserver.meta;

import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/11
 */
public class BasePageInfo {

    @JSONField(serialize = false)
    protected int limit = 20;

    @JSONField(serialize = false)
    protected int offset = 0;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @JSONField(serialize = false)
    public Pageable getPageable() {
        return new QPageRequest(Math.floorDiv(offset, limit), limit);
    }

}
