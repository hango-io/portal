package org.hango.cloud.envoy.infra.serviceregistry.hooker;

import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.base.meta.CommonExtension;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;
import org.hango.cloud.common.infra.serviceregistry.hooker.AbstractRegistryCenterHooker;
import org.hango.cloud.envoy.infra.serviceregistry.service.IEnvoyServiceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistryCenterHooker extends AbstractRegistryCenterHooker<CommonExtension, CommonExtensionDto> {

  @Autowired
  private IEnvoyServiceRegistryService envoyServiceRegistryService;

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  protected List<? extends String> postGetRegistryByServiceTypeHook(List l) {
    Long virtualGwId = MethodAroundHolder.getNextParam(Long.class);
    String serviceType = MethodAroundHolder.getNextParam(String.class);
    return envoyServiceRegistryService.getRegistryTypeList(virtualGwId, serviceType);
  }
}
