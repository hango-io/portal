package org.hango.cloud.common.infra.base.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.Collection;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/3/16
 */
public interface ICommonDao<T> {

    String NULL_WRAPPER_ERROR_MESSAGE = "Not Find Mapper Class";

    /**
     * 获取数据库实体对应Mapper
     *
     * @return BaseMapper
     */
    BaseMapper<T> getMapper();

    /**
     * 添加记录
     *
     * @param t object
     * @return added record count
     */
    default int add(T t) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.insert(t);
    }

    /**
     * 更新记录
     *
     * @param t object
     * @return updated record count
     */
    default int update(T t) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.updateById(t);
    }

    /**
     * 删除记录
     *
     * @param t object
     * @return deleted record count
     */
    default int delete(T t) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.deleteById(t);
    }

    /**
     * 查找记录
     *
     * @param id 数据库主键ID
     * @return single record
     */
    default T get(long id) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.selectById(id);
    }

    /**
     * 查找全部记录
     *
     * @return list of records
     */
    default List<T> findAll() {
        return getRecordsByField(Wrappers.emptyWrapper());
    }

    /**
     * 查找全部记录个数
     *
     * @return count of records
     */
    default long countAll() {
        return getCountByFields(Wrappers.emptyWrapper());
    }

    /**
     * 根据条件查找记录集合
     *
     * @param wrapper 查询条件
     * @param page    分页条件
     * @return list of records by wrapper
     */
    default <P extends IPage<T>> P pageRecordsByField(Wrapper<T> wrapper, P page) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.selectPage(page, wrapper);
    }

    /**
     * 根据条件查找记录集合
     *
     * @param wrapper 查询条件
     * @return list of records by wrapper
     */
    default List<T> getRecordsByField(Wrapper<T> wrapper) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.selectList(wrapper);
    }

    /**
     * 根据条件and查找记录个数
     *
     * @param wrapper 查询条件
     * @return count of records by wrapper
     */
    default long getCountByFields(Wrapper<T> wrapper) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.selectCount(wrapper);
    }

    /**
     * 通过id列表批量获取对应的数据实体
     * @param ids
     * @return
     */
    default List<T> getByIds(Collection<Long> ids) {
        BaseMapper<T> mapper = getMapper();
        if (mapper == null) {
            throw new RuntimeException(NULL_WRAPPER_ERROR_MESSAGE);
        }
        return mapper.selectBatchIds(ids);
    }
}
