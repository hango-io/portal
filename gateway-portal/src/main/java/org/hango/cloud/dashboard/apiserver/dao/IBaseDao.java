package org.hango.cloud.dashboard.apiserver.dao;

import java.util.List;
import java.util.Map;

/**
 * @author Zhu Jianfeng (hzzhujianfeng@corp.netease.com)
 * @version $Id: IBaseDao.java, v 1.0 2017年3月27日 下午4:53:35
 */
public interface IBaseDao<T> {

    /**
     * 添加记录
     *
     * @param t object
     * @return added record count
     */
    long add(T t);

    /**
     * 更新记录
     *
     * @param t object
     * @return updated record count
     */
    long update(T t);

    /**
     * 删除记录
     *
     * @param t object
     * @return deleted record count
     */
    int delete(T t);

    /**
     * 查找记录
     *
     * @param id
     * @return
     */
    T get(long id);

    /**
     * 查找全部记录
     *
     * @return
     */
    List<T> findAll();

    /**
     * 根据条件查找记录集合
     *
     * @return
     */
    List<T> getRecordsByField(Map<String, Object> params);

    /**
     * 根据条件and查找记录个数
     *
     * @return
     */
    int getCountByFields(Map<String, Object> params);
}