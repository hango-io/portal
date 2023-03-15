package org.hango.cloud.common.infra.base.util;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;

/**
 * @Author zhufengwei
 * @Date 2022/11/7
 */
public class PageUtil {

    public static final String ID = "id";

    public static final String ORDERS = "orders";

    public static <T> Page<T> of(Integer limit, Integer offset){
        if (limit == null || limit == 0){
            limit = 20;
        }
        if (offset == null){
            offset = 0;
        }
        int currentPage = offset / limit + 1;
        return new Page<>(currentPage, limit);
    }

    public static void sortHandle(RouteRuleQuery ruleInfoQuery){
        String sortKey = ruleInfoQuery.getSortKey();
        if (StringUtils.isBlank(sortKey)){
            ruleInfoQuery.setSortKey(ID);
        }else if (BaseConst.CONST_PRIORITY.equals(sortKey)){
            ruleInfoQuery.setSortKey(ORDERS);
        }
    }
}
