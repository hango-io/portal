package org.hango.cloud.common.infra.serviceregistry.service.impl;

import org.hango.cloud.common.infra.serviceregistry.service.IRegistryCenterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/12
 */
@Service
public class RegistryCenterServiceImpl implements IRegistryCenterService {

    @Override
    public List<String> getRegistryByServiceType(long virtualGwId, String serviceType) {
       return new ArrayList<>();
    }

}
