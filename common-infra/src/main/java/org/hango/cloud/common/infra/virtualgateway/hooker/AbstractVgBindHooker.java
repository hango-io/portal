package org.hango.cloud.common.infra.virtualgateway.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayProjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 虚拟网关绑定Hooker
 * @date 2022/10/28
 */
public class AbstractVgBindHooker<T extends CommonExtension, S extends VirtualGatewayBindDto> extends AbstractInvokeHooker<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVgBindHooker.class);

    @Override
    public Class aimAt() {
        return VirtualGatewayProjectImpl.class;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        triples.add(MutableTriple.of("bindProject","doBindProject", StringUtils.EMPTY));
        triples.add(MutableTriple.of("unbindProject", "doUnbindProject", StringUtils.EMPTY));
        triples.add(MutableTriple.of("getBindList", StringUtils.EMPTY, "doGetBindListEnhancement"));
        triples.add(MutableTriple.of("getProjectScope", StringUtils.EMPTY, "doGetProjectScope"));

        return triples;
    }

    protected void bindProject(VirtualGatewayBindDto virtualGatewayBindDto) {
        // 未使用，无需实现
    }


    @SuppressWarnings("unused")
    public final void doBindProject(VirtualGatewayBindDto virtualGatewayBindDto) {
        logger.debug("execute find project bind hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            if (AbstractVgBindHooker.class.isAssignableFrom(nextHooker.getClass())) {
                ((AbstractVgBindHooker<T, S>) nextHooker).doBindProject(virtualGatewayBindDto);
            }
        }
        bindProject(virtualGatewayBindDto);
    }

    protected void unbindProject(long virtualGwId, long projectId) {
        // 未使用，无需实现
    }


    @SuppressWarnings("unused")
    public final void doUnbindProject(long virtualGwId, long projectId) {
        logger.debug("execute find project unbind hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            if (AbstractVgBindHooker.class.isAssignableFrom(nextHooker.getClass())) {
                ((AbstractVgBindHooker<T, S>) nextHooker).doUnbindProject(virtualGwId, projectId);
            }
        }
        unbindProject(virtualGwId, projectId);
    }

    protected List getBindListEnhancement(List l) {
        return l;
    }

    @SuppressWarnings("unused")
    public final List doGetBindListEnhancement(List l) {
        logger.debug("execute find enhancement hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            if (AbstractVgBindHooker.class.isAssignableFrom(nextHooker.getClass())) {
                l = ((AbstractVgBindHooker<T, S>) nextHooker).doGetBindListEnhancement(l);
            }
        }
        return getBindListEnhancement(l);
    }


    protected PermissionScopeDto getProjectScope(PermissionScopeDto returnData){
        return returnData;
    }

    @SuppressWarnings("unused")
    public final PermissionScopeDto doGetProjectScope(PermissionScopeDto returnData) {
        logger.debug("execute get projectScope hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null) {
            if (AbstractVgBindHooker.class.isAssignableFrom(nextHooker.getClass())) {
               returnData = ((AbstractVgBindHooker<T, S>) nextHooker).doGetProjectScope(returnData);
            }
        }
        return getProjectScope(returnData);
    }




}
