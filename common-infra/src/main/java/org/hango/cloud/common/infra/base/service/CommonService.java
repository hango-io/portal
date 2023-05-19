package org.hango.cloud.common.infra.base.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.base.meta.PageQuery;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/5/6
 */
public interface CommonService<T extends CommonExtension, S extends CommonExtensionDto> {


    /**
     * 创建数据
     *
     * @param s
     * @return
     */

    default long create(S s) {
        return 0;
    }


    /**
     * 修改数据
     *
     * @param s
     * @return
     */

    default long update(S s) {
        return 0;
    }

    /**
     * 删除数据
     *
     * @param s
     */

    default void delete(S s) {
        return;
    }

    /**
     * 获取所有数据
     *
     * @return
     */

    default List<? extends S> findAll() {
        return Collections.emptyList();
    }

    /**
     * 分页获取所有数据
     * <p>
     * 该方法即将废弃，请使用如下方法
     *
     * @param limit
     * @param offset
     * @return
     * @see CommonService#pageAll(PageQuery)
     */
    @Deprecated
    default List<? extends S> findAll(long offset, long limit) {
        return Collections.emptyList();
    }

    /**
     * 分页获取所有数据
     *
     * @param query 查询参数
     * @return
     */
    default Page<S> pageAll(PageQuery query) {
        return  new Page<>();
    }

    /**
     * 分页数据实体bean转换为DTO
     *
     * @param o
     * @return
     */
    default Page<S> toPageView(Page<? extends T> o) {
        Page<S> page = Page.of(o.getCurrent(), o.getSize(), o.getTotal());
        page.setRecords(o.getRecords().stream().map(this::toView).collect(Collectors.toList()));
        return page;
    }

    /**
     * 获取数据总数
     * <p>
     * 该方法即将废弃，请使用如下方法
     *
     * @return
     * @see CommonService#pageAll(PageQuery)
     */
    @Deprecated
    default long countAll() {
        return 0;
    }

    /**
     * 通过Id获取数据信息
     *
     * @param id
     * @return
     */

    default S get(long id) {
        return null;
    }


    /**
     * 转化为显示层,该方法必须实现
     *
     * @param t
     * @return
     */
    default S toView(T t) {
        return null;
    }


    /**
     * 转化为元数据，方法必须实现
     *
     * @param s
     * @return
     */
    default T toMeta(S s) {
        return null;
    }

    /**
     * 校验新增参数
     *
     * @param s
     * @return
     */

    default ErrorCode checkCreateParam(S s) {
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 校验更新参数
     *
     * @param s
     * @return
     */

    default ErrorCode checkUpdateParam(S s) {
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 校验删除参数
     *
     * @param s
     * @return
     */

    default ErrorCode checkDeleteParam(S s) {
        return CommonErrorCode.SUCCESS;
    }

}
