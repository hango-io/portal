package org.hango.cloud.common.infra.serviceproxy.meta;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/3/17
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceProxyQuery extends PageQuery {

    /**
     * 服务名称/服务别名检索条件，全匹配
     */
    @JSONField(name = "Pattern")
    private String pattern;


    /**
     * 服务名称/服务别名/域名 检索条件，全匹配
     */
    @JSONField(name = "Condition")
    private String condition;


    /**
     * 服务发布所属虚拟网关id
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;


    /**
     * 服务发布所属虚拟网关id
     */
    @JSONField(name = "Protocol")
    private String protocol;

    /**
     * 服务名称集合
     */
    @JSONField(name = "NameList")
    private List<String> nameList;


    /**
     * 发布所属项目id
     */
    @Builder.Default
    private long projectId = ProjectTraceHolder.getProId();
}
