package org.hango.cloud.common.infra.base.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.CollectionUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/1/12
 */
@MappedJdbcTypes(JdbcType.VARBINARY)
@MappedTypes({List.class})
public abstract class ListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType)
            throws SQLException {
        String content = CollectionUtils.isEmpty(parameter) ? null : JSON.toJSONString(parameter);
        ps.setString(i, content);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return this.getListByJsonArrayString(rs.getString(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return this.getListByJsonArrayString(rs.getString(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return this.getListByJsonArrayString(cs.getString(columnIndex));
    }

    protected List<T> getListByJsonArrayString(String content) {
        return StringUtils.isBlank(content) ? new ArrayList<>() : JSON.parseObject(content, this.specificType());
    }

    /**
     * 具体类型，由子类提供
     *
     * @return 具体类型
     */
    protected abstract TypeReference<List<T>> specificType();
}

