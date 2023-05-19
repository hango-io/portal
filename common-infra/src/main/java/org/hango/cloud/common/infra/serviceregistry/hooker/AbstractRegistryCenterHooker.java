package org.hango.cloud.common.infra.serviceregistry.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.serviceregistry.service.impl.RegistryCenterServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractRegistryCenterHooker<T extends CommonExtension, S extends CommonExtensionDto> extends AbstractInvokeHooker<T, S> {

  private static final Logger logger = LoggerFactory.getLogger(AbstractRegistryCenterHooker.class);

  @Override
  public Class aimAt() {
    return RegistryCenterServiceImpl.class;
  }

  @Override
  protected List<Triple<String, String, String>> put() {
    List<Triple<String, String, String>> triples = Lists.newArrayList();
    triples.add(MutableTriple
        .of("getRegistryByServiceType", "",
            "doPostGetRegistryByServiceTypeHook"));
    return triples;
  }

  /**
   * 获取数据面对应注册中心中的服务后置Hook
   *
   * @param l
   * @return
   */
  protected List<? extends String> postGetRegistryByServiceTypeHook(List l) {
    return l;
  }

  /**
   * 执行获取数据面对应注册中心的服务后置Hook
   *
   * @param l
   * @return
   */
  public final List<? extends String> doPostGetRegistryByServiceTypeHook(List l) {
    logger.debug("execute post get registry hook ,hook is {}", this.getClass().getName());
    if (nextHooker != null && nextHooker instanceof AbstractRegistryCenterHooker) {
      l = ((AbstractRegistryCenterHooker<CommonExtension, CommonExtensionDto>) nextHooker)
          .doPostGetRegistryByServiceTypeHook(l);
    }
    return postGetRegistryByServiceTypeHook(l);
  }
}
