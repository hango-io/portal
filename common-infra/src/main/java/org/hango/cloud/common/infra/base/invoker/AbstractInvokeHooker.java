package org.hango.cloud.common.infra.base.invoker;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc service 层调用Hooker，责任链
 * @date 2022/5/6
 */
public abstract class AbstractInvokeHooker<T extends CommonExtension, S extends CommonExtensionDto> implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInvokeHooker.class);
    /**
     * hooker 方法映射表
     * Map<originMethod,Pair<hookerPreMethod, hookerPostMethod>>
     * originMethod : 基类实现的方法，可参照下类中的方法
     *
     * @see org.hango.cloud.common.infra.base.service.CommonService
     * hookerPreMethod & hookerPostMethod : 增强hooker中的前置&后置方法， 可参照下类中的方法
     * @see AbstractInvokeHooker
     */
    private static final Map<String, Pair<Method, Method>> hookMethodMap = Maps.newHashMap();
    private List<Triple<String, String, String>> hookMethodTripe = Lists.newArrayList();
    protected AbstractInvokeHooker<T, S> nextHooker;

    @PostConstruct
    private void init() {
        loadHookMethodTriple();
        loadHookMethodMap();
    }

    private void loadHookMethodTriple() {
        hookMethodTripe.add(MutableTriple.of("create", "doPreCreateHook", "doPostCreateHook"));
        hookMethodTripe.add(MutableTriple.of("update", "doPreUpdateHook", "doPostUpdateHook"));
        hookMethodTripe.add(MutableTriple.of("delete", "doPreDeleteHook", "doPostDeleteHook"));
        hookMethodTripe.add(MutableTriple.of("findPage", StringUtils.EMPTY, "doFindMultiEnhancementPage"));
        hookMethodTripe.add(MutableTriple.of("findAll", StringUtils.EMPTY, "doFindMultiEnhancement"));
        hookMethodTripe.add(MutableTriple.of("get", StringUtils.EMPTY, "doFindSingleEnhancement"));
        hookMethodTripe.add(MutableTriple.of("checkCreateParam", StringUtils.EMPTY, "doCheckCreateParam"));
        hookMethodTripe.add(MutableTriple.of("checkUpdateParam", StringUtils.EMPTY, "doCheckUpdateParam"));
        hookMethodTripe.add(MutableTriple.of("checkDeleteParam", StringUtils.EMPTY, "doCheckDeleteParam"));
        hookMethodTripe.addAll(put());
    }

    private void loadHookMethodMap() {
        Method[] methods = this.getClass().getMethods();
        //禁止方法重载
        Map<String, Method> methodMap = Arrays.stream(methods).filter(m -> AbstractInvokeHooker.class.isAssignableFrom(m.getDeclaringClass())).collect(Collectors.toMap(Method::getName, Function.identity()));
        for (Triple<String, String, String> triple : hookMethodTripe) {
            Method preMethod = methodMap.get(triple.getMiddle());
            Method postMethod = methodMap.get(triple.getRight());
            hookMethodMap.put(triple.getLeft(), MutablePair.of(preMethod, postMethod));
        }
    }


    /**
     * 指定对哪个基类做增强
     * <p>
     * eg.
     * <p>
     * GatewayHooker 对 IGatewayInfoService 做增强， 则返回IGatewayInfoService
     *
     * @return
     */
    public abstract Class aimAt();

    protected List<Triple<String, String, String>> put() {
        return Collections.emptyList();
    }

    /**
     * 获取前置Hooker方法
     *
     * @param originMethod 基类方法
     * @return 前置Hooker方法
     */
    public final Method getPreMethod(String originMethod) {
        Pair<Method, Method> pair = hookMethodMap.get(originMethod);
        if (pair != null) {
            return pair.getLeft();
        }
        return null;
    }

    /**
     * 获取后置Hooker方法
     *
     * @param originMethod 基类方法
     * @return 前后Hooker方法
     */
    public final Method getPostMethod(String originMethod) {
        Pair<Method, Method> pair = hookMethodMap.get(originMethod);
        if (pair != null) {
            return pair.getRight();
        }
        return null;
    }

    public final void setNextHooker(AbstractInvokeHooker<T, S> nextHooker) {
        this.nextHooker = nextHooker;
    }

    /**
     * 创建操作前置Hook
     *
     * @param s
     */
    protected void preCreateHook(S s) {
    }

    /**
     * 执行创建操作前置Hook
     *
     * @param s
     */
    @SuppressWarnings("unused")
    public final void doPreCreateHook(S s) {
        logger.debug("execute pre create hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            nextHooker.doPreCreateHook(s);
        }
        preCreateHook(s);
    }

    /**
     * 创建操作后置Hook
     *
     * @param returnData
     */
    protected Object postCreateHook(Object returnData) {
        return returnData;
    }


    /**
     * 执行创建操作后置Hook
     *
     * @param returnData
     */
    @SuppressWarnings("unused")
    public final Object doPostCreateHook(Object returnData) {
        logger.debug("execute post create hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            returnData = nextHooker.doPostCreateHook(returnData);
        }
        return postCreateHook(returnData);
    }

    /**
     * 更新操作前置Hook
     *
     * @param s
     */
    protected void preUpdateHook(S s) {
    }

    /**
     * 执行更新操作前置Hook
     *
     * @param s
     */
    @SuppressWarnings("unused")
    public final void doPreUpdateHook(S s) {
        logger.debug("execute pre update hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            nextHooker.doPreUpdateHook(s);
        }
        preUpdateHook(s);
    }

    /**
     * 更新操作后置Hook
     *
     * @param returnData
     */
    protected Object postUpdateHook(Object returnData) {
        return returnData;
    }

    /**
     * 执行更新操作后置Hook
     *
     * @param returnData
     */
    @SuppressWarnings("unused")
    public final Object doPostUpdateHook(Object returnData) {
        logger.debug("execute post update hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            returnData = nextHooker.doPostUpdateHook(returnData);
        }
        return postUpdateHook(returnData);
    }

    /**
     * 删除操作前置Hook
     *
     * @param s
     */
    protected void preDeleteHook(S s) {
    }

    /**
     * 执行删除操作前置Hook
     *
     * @param s
     */
    @SuppressWarnings("unused")
    public final void doPreDeleteHook(S s) {
        logger.debug("execute pre delete hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            nextHooker.doPreDeleteHook(s);
        }
        preDeleteHook(s);
    }

    /**
     * 删除操作后置Hook
     *
     * @param returnData
     */
    protected Object postDeleteHook(Object returnData) {
        return returnData;
    }

    /**
     * 执行删除操作后置Hook
     *
     * @param returnData
     */
    @SuppressWarnings("unused")
    public final Object doPostDeleteHook(Object returnData) {
        logger.debug("execute post delete hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            returnData = nextHooker.doPostDeleteHook(returnData);
        }
        return postDeleteHook(returnData);
    }

    /**
     * 校验新增参数
     *
     * @param returnCode
     * @return
     */
    protected ErrorCode checkCreateParam(ErrorCode returnCode) {
        return returnCode;
    }

    /**
     * 执行校验新增参数Hook
     *
     * @param returnCode
     */
    @SuppressWarnings("unused")
    public final ErrorCode doCheckCreateParam(ErrorCode returnCode) {
        if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
            return returnCode;
        }
        logger.debug("execute check create param hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            returnCode = nextHooker.doCheckCreateParam(returnCode);
            if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
                return returnCode;
            }
        }
        return checkCreateParam(returnCode);
    }


    /**
     * 校验更新参数
     *
     * @param returnCode
     * @return
     */
    protected ErrorCode checkUpdateParam(ErrorCode returnCode) {
        return returnCode;
    }

    /**
     * 执行校验更新参数Hook
     *
     * @param returnCode
     */
    @SuppressWarnings("unused")
    public final ErrorCode doCheckUpdateParam(ErrorCode returnCode) {
        if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
            return returnCode;
        }
        logger.debug("execute check update param hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            returnCode = nextHooker.doCheckUpdateParam(returnCode);
            if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
                return returnCode;
            }
        }
        return checkUpdateParam(returnCode);
    }

    /**
     * 校验删除参数
     *
     * @param returnCode
     * @return
     */
    protected ErrorCode checkDeleteParam(ErrorCode returnCode) {
        return returnCode;
    }

    /**
     * 执行校验删除参数Hook
     *
     * @param returnCode
     */
    @SuppressWarnings("unused")
    public final ErrorCode doCheckDeleteParam(ErrorCode returnCode) {
        if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
            return returnCode;
        }
        logger.debug("execute check delete param hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            returnCode = nextHooker.doCheckDeleteParam(returnCode);
            if (!CommonErrorCode.SUCCESS.equals(returnCode)) {
                return returnCode;
            }
        }
        return checkDeleteParam(returnCode);
    }

    /**
     * 查询增强
     *
     * @param o
     * @return
     */
    protected Object findSingleEnhancement(Object o) {
        return o;
    }

    @SuppressWarnings("unused")
    public final Object doFindSingleEnhancement(Object o) {
        logger.debug("execute find enhancement hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            o = nextHooker.doFindSingleEnhancement(o);
        }
        return findSingleEnhancement(o);
    }


    protected List findMultiEnhancement(List l) {
        return l;
    }


    public final List doFindMultiEnhancement(List l) {
        logger.debug("execute find enhancement hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            l = nextHooker.doFindMultiEnhancement(l);
        }
        return findMultiEnhancement(l);
    }

    protected Page findMultiEnhancementPage(Page p) {
        return p;
    }


    public final Page doFindMultiEnhancementPage(Page p) {
        logger.debug("execute find enhancement page hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            p = nextHooker.doFindMultiEnhancementPage(p);
        }
        return findMultiEnhancementPage(p);
    }
}
