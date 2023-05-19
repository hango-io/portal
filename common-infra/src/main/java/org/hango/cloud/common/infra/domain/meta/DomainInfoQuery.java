package org.hango.cloud.common.infra.domain.meta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */
@Getter
@Setter
@Builder
public class DomainInfoQuery extends PageQuery {

    /**
     * 域名id列表
     */
    List<Long> ids;
    /**
     * 域名
     */
    private String host;
    /**
     * 域名状态，管理中/只用于关联
     */
    private String status;

    /**
     * 协议
     */
    private String protocol;
    /**
     * 项目id
     */
    private List<Long> projectIds;
    /**
     * 模糊查询条件（host）
     */
    private String pattern;

}
