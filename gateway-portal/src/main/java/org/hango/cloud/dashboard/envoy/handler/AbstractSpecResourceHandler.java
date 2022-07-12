package org.hango.cloud.dashboard.envoy.handler;

import org.hango.cloud.dashboard.envoy.innerdto.SpecResourceDto;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/22
 */
public abstract class AbstractSpecResourceHandler<T> {

    /**
     * 获取资源类型
     *
     * @return
     */
    public abstract String getResourceType();

    /**
     * 获取元数据信息
     *
     * @return
     */
    public abstract List<T> getMetas();

    /**
     * 将元数据信息转换为具体资源类
     *
     * @param t
     * @return
     */
    public abstract List<SpecResourceDto> toSpecResources(List<T> t);


}
