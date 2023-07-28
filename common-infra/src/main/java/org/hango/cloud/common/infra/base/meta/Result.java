package org.hango.cloud.common.infra.base.meta;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.LogTraceUUIDHolder;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/28
 */
@Getter
@Setter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 8373441741538019848L;

    @JSONField(serialize = false)
    private ErrorCode errorCode;

    /**
     * 业务码
     */
    @JSONField(name = "Code")
    private String code;

    /**
     * 数据集
     */
    @JSONField(name = "Result")
    private T result;

    /**
     * 数据集
     */
    @JSONField(name = "Message")
    private String message;

    /**
     * 请求ID
     */
    @JSONField(name = "RequestId")
    private String requestId;

    public Result() {
        this(null);
    }


    public Result(T result) {
        this(result, null);
    }

    public static Result err(ErrorCode errorCode) {
        return new Result(null, errorCode);
    }


    public Result(T result, ErrorCode errorCode) {
        this.errorCode = errorCode == null ? CommonErrorCode.SUCCESS : errorCode;
        this.code = this.errorCode.getCode();
        this.message = this.errorCode.getMessage();
        this.requestId = LogTraceUUIDHolder.getUUIDId();
        this.result = result;
    }
}
