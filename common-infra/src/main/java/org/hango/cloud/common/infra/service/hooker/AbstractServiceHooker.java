package org.hango.cloud.common.infra.service.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.meta.ServiceInfo;
import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/5
 */
public abstract class AbstractServiceHooker<T extends ServiceInfo, S extends ServiceDto> extends AbstractInvokeHooker<T, S> {

    @Override
    public Class aimAt() {
        return ServiceInfoServiceImpl.class;
    }

    @Override
    public int getOrder() {
        return 0;
    }


    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        triples.add(MutableTriple.of("findAllServiceByProjectId", StringUtils.EMPTY, BaseConst.DO_FIND_MULTI_ENHANCEMENT));
        triples.add(MutableTriple.of("findAllServiceByProjectIdLimit", StringUtils.EMPTY, BaseConst.DO_FIND_MULTI_ENHANCEMENT));
        triples.add(MutableTriple.of("findAllServiceByDisplayName", StringUtils.EMPTY, BaseConst.DO_FIND_MULTI_ENHANCEMENT));
        triples.add(MutableTriple.of("findAllAuthServiceIdListByDisplayName", StringUtils.EMPTY, BaseConst.DO_FIND_MULTI_ENHANCEMENT));
        triples.add(MutableTriple.of("getServiceByServiceName", StringUtils.EMPTY, BaseConst.DO_FIND_SINGLE_ENHANCEMENT));
        triples.add(MutableTriple.of("getServiceByServiceNameAndProject", StringUtils.EMPTY, BaseConst.DO_FIND_SINGLE_ENHANCEMENT));
        triples.add(MutableTriple.of("describeDisplayName", StringUtils.EMPTY, BaseConst.DO_FIND_SINGLE_ENHANCEMENT));
        triples.add(MutableTriple.of("getServiceDtoList", StringUtils.EMPTY, BaseConst.DO_FIND_MULTI_ENHANCEMENT));
        return triples;
    }
}
