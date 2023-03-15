//package org.hango.cloud.common-infra.infra.base.service.impl;
//
//import org.hango.cloud.common-infra.infra.base.annotation.HookerAspect;
//import org.hango.cloud.common-infra.infra.base.errorcode.CommonErrorCode;
//import org.hango.cloud.common-infra.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common-infra.infra.base.meta.CommonExtension;
//import org.hango.cloud.common-infra.infra.base.meta.CommonExtensionDto;
//import org.hango.cloud.common-infra.infra.base.service.CommonService;
//
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author zhangbj
// * @version 1.0
// * @Type
// * @Desc
// * @date 2022/8/11
// */
//@HookerAspect
//public class CommonServiceImpl<T extends CommonExtension, S extends CommonExtensionDto> implements CommonService<T, S> {
//
//    /**
//     * 创建数据
//     *
//     * @param s
//     * @return
//     */
//    @HookerAspect(preHookMethod = "", postHookMethod = "")
//    @Override
//    public long create(S s) {
//        return 0L;
//    }
//
//
//    /**
//     * 修改数据
//     *
//     * @param s
//     * @return
//     */
//    @HookerAspect(preHookMethod = "", postHookMethod = "")
//    @Override
//    public long update(S s) {
//        return 0L;
//    }
//
//    /**
//     * 删除数据
//     *
//     * @param id
//     */
//    @HookerAspect(preHookMethod = "", postHookMethod = "")
//    @Override
//    public void delete(Long id) {
//    }
//
//    /**
//     * 获取所有数据
//     *
//     * @return
//     */
//    @HookerAspect(postHookMethod = "doFindEnhancement")
//    @Override
//    public List<? extends S> findAll() {
//        return Collections.emptyList();
//    }
//
//    /**
//     * 分页获取所有数据
//     *
//     * @param limit
//     * @param offset
//     * @return
//     */
//    @Override
//    public List<? extends S> findAll(long offset, long limit) {
//        return Collections.emptyList();
//    }
//
//    /**
//     * 获取数据总数
//     *
//     * @return
//     */
//    @Override
//    public long countAll() {
//        return 0L;
//    }
//
//    /**
//     * 通过Id获取数据信息
//     *
//     * @param id
//     * @return
//     */
//    @Override
//    public S get(long id) {
//        return null;
//    }
//
//
//    /**
//     * 转化为显示层
//     *
//     * @param t
//     * @return
//     */
//    @Override
//    public S toView(T t) {
//        return null;
//    }
//
//    /**
//     * 转化为元数据
//     *
//     * @param s
//     * @return
//     */
//    @Override
//    public T toMeta(S s) {
//        return null;
//    }
//
//    /**
//     * 校验新增参数
//     *
//     * @param s
//     * @return
//     */
//    @Override
//    public ErrorCode checkCreateParam(S s) {
//        return CommonErrorCode.Success;
//    }
//
//    /**
//     * 校验更新参数
//     *
//     * @param s
//     * @return
//     */
//    @Override
//    public ErrorCode checkUpdateParam(S s) {
//        return CommonErrorCode.Success;
//    }
//
//    /**
//     * 校验删除参数
//     *
//     * @param s
//     * @return
//     */
//    @Override
//    public ErrorCode checkDeleteParam(S s) {
//        return CommonErrorCode.Success;
//    }
//}
