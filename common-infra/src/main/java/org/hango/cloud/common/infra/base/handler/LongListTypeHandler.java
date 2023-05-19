package org.hango.cloud.common.infra.base.handler;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SYMBOL_COMMA;


/**
 * Long集合类型处理（以逗号拼接）
 *
 * @author yutao04
 */
@MappedTypes({List.class})
public class LongListTypeHandler extends ListTypeHandler<Long> {

    @Override
    protected TypeReference<List<Long>> specificType() {
        return new TypeReference<List<Long>>() {
        };
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        String content = CollectionUtils.isEmpty(parameter) ? null : CommonUtil.genStringBaseLongList(parameter, SYMBOL_COMMA);
        ps.setString(i, content);
    }

    @Override
    protected List<Long> getListByJsonArrayString(String content) {
        return StringUtils.isBlank(content) ? new ArrayList<>() : CommonUtil.splitStringToLongList(content, SYMBOL_COMMA);
    }
}