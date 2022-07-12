package org.hango.cloud.dashboard.apiserver.dto.grpcdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ApiGrpcParam;

import java.io.Serializable;

/**
 * API和gRPC method之间的对应关系
 *
 * @Author: TC_WANG
 * @Date: 2019/7/2
 */
public class ApiGrpcParamDto implements Serializable {
    /**
     * pb对应的名称
     */
    @JSONField(name = "PbName")
    private String pbName;

    /**
     * package名称
     */
    @JSONField(name = "PbPackageName")
    private String pbPackageName;

    /**
     * service名称
     */
    @JSONField(name = "PbServiceName")
    private String pbServiceName;

    /**
     * method名称
     */
    @JSONField(name = "PbMethodName")
    private String pbMethodName;

    public ApiGrpcParamDto(ApiGrpcParam apiGrpcParam) {
        this.pbName = apiGrpcParam.getPbName();
        this.pbPackageName = apiGrpcParam.getPbPackageName();
        this.pbServiceName = apiGrpcParam.getPbServiceName();
        this.pbMethodName = apiGrpcParam.getPbMethodName();
    }

    public String getPbName() {
        return pbName;
    }

    public void setPbName(String pbName) {
        this.pbName = pbName;
    }

    public String getPbPackageName() {
        return pbPackageName;
    }

    public void setPbPackageName(String pbPackageName) {
        this.pbPackageName = pbPackageName;
    }

    public String getPbServiceName() {
        return pbServiceName;
    }

    public void setPbServiceName(String pbServiceName) {
        this.pbServiceName = pbServiceName;
    }

    public String getPbMethodName() {
        return pbMethodName;
    }

    public void setPbMethodName(String pbMethodName) {
        this.pbMethodName = pbMethodName;
    }
}
