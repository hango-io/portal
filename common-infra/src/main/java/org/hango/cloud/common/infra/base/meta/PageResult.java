package org.hango.cloud.common.infra.base.meta;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

import java.util.Collections;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/28
 */
public class PageResult<T> extends Result<T> {

    /**
     * 数据总数
     */
    @JSONField(name = "Total")
    private long total;

    public PageResult() {
        super(((T) Collections.emptyList()));
    }

    public PageResult(T result, long total) {
        super(result);
        this.total = total;
    }

    public static PageResult ofEmpty(){
        return new PageResult(null, 0);
    }

    public PageResult(T result, ErrorCode errorCode, long total) {
        super(result, errorCode);
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
