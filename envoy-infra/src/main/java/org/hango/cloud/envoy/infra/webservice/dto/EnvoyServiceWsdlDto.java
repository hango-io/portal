package org.hango.cloud.envoy.infra.webservice.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlInfo;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 服务下的wsdl的binding与message信息
 */
public class EnvoyServiceWsdlDto {
    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "CreateDate")
    private long createDate;

    @JSONField(name = "ModifyDate")
    private long modifyDate;

    @JSONField(name = "ServiceId")
    private long serviceId;

    @JSONField(name = "WsdlFileName")
    private String wsdlFileName;

    @JSONField(name = "Bindings")
    private List<EnvoyServiceWsdlBindingItemDto> bindings;

    public EnvoyServiceWsdlDto(EnvoyServiceWsdlInfo envoyServiceWsdlInfo) {
        this.id = envoyServiceWsdlInfo.getId();
        this.createDate = envoyServiceWsdlInfo.getCreateDate();
        this.modifyDate = envoyServiceWsdlInfo.getModifyDate();
        this.serviceId = envoyServiceWsdlInfo.getServiceId();
        this.wsdlFileName = envoyServiceWsdlInfo.getWsdlFileName();
        this.bindings = new ArrayList<>();
        if (Objects.nonNull(envoyServiceWsdlInfo.getWsdlBindingList())) {
            envoyServiceWsdlInfo.getWsdlBindingList().forEach(item -> {
                EnvoyServiceWsdlBindingItemDto itemDto = new EnvoyServiceWsdlBindingItemDto();
                BeanUtils.copyProperties(item, itemDto);
                itemDto.setRequestAllElements(new ArrayList<>());
                itemDto.setResponseAllElements(new ArrayList<>());
                if (Objects.nonNull(item.getRequestAllElements())) {
                    item.getRequestAllElements().forEach(elementInfo -> {
                        EnvoyServiceWsdlElementDto elementDto = new EnvoyServiceWsdlElementDto();
                        BeanUtils.copyProperties(elementInfo, elementDto);
                        itemDto.getRequestAllElements().add(elementDto);
                    });
                }
                if (Objects.nonNull(item.getResponseAllElements())) {
                    item.getResponseAllElements().forEach(elementInfo -> {
                        EnvoyServiceWsdlElementDto elementDto = new EnvoyServiceWsdlElementDto();
                        BeanUtils.copyProperties(elementInfo, elementDto);
                        itemDto.getResponseAllElements().add(elementDto);
                    });
                }
                this.bindings.add(itemDto);
            });
        }
    }

    public EnvoyServiceWsdlDto() {
    }

    public EnvoyServiceWsdlDto(long id, long createDate, long modifyDate, long serviceId, List<EnvoyServiceWsdlBindingItemDto> bindings) {
        this.id = id;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.serviceId = serviceId;
        this.bindings = bindings;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getWsdlFileName() {
        return wsdlFileName;
    }

    public void setWsdlFileName(String wsdlFileName) {
        this.wsdlFileName = wsdlFileName;
    }

    public List<EnvoyServiceWsdlBindingItemDto> getBindings() {
        return bindings;
    }

    public void setBindings(List<EnvoyServiceWsdlBindingItemDto> bindings) {
        this.bindings = bindings;
    }
}
