package org.hango.cloud.common.advanced.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/5/24
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@With
public class MetaServiceDto implements Serializable {

    private static final long serialVersionUID = 3210297446810293263L;


    /**
     * 服务协议1:http 2:dubbo 4:grpc
     */
    @JSONField(name = "ServiceType")
    private String serviceType;
    /**
     * 服务名称
     */
    @JSONField(name = "Name")
    private String name;

    /**
     * 项目ID
     */
    @JSONField(name = "ProjectId")
    private String projectId;


    /**
     * 语言
     */
    @JSONField(name = "Language")
    private String language;
}
