package org.hango.cloud.envoy.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/3/31
 */
@Getter
@Setter
public class ResultDTO implements Serializable {
    private static final long serialVersionUID = 7074581622243968756L;

    /**
     * 总数量（刷新成功+刷新失败+无需刷新）
     */
    @JSONField(name = "TotalCount")
    private Integer totalCount;

    /**
     * 刷新成功数量
     */
    @JSONField(name = "SuccessCount")
    private Integer successCount;

    /**
     * 错误数量
     */
    @JSONField(name = "ErrorCount")
    private Integer errorCount;

    /**
     * 错误服务名称
     */
    @JSONField(name = "ErrorName")
    private List<String> errorName;

    public static ResultDTO of(Integer totalCount, Integer successCount, List<String> errorName){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setTotalCount(totalCount);
        resultDTO.setSuccessCount(successCount);
        resultDTO.setErrorCount(errorName.size());
        resultDTO.setErrorName(errorName);
        return resultDTO;
    }

    public static ResultDTO of(Integer totalCount, List<String> errorName){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setTotalCount(totalCount);
        resultDTO.setSuccessCount(totalCount - errorName.size());
        resultDTO.setErrorCount(errorName.size());
        resultDTO.setErrorName(errorName);
        return resultDTO;
    }
}
