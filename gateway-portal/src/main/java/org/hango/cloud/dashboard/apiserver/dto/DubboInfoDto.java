package org.hango.cloud.dashboard.apiserver.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.apiserver.meta.DubboInfo;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
public class DubboInfoDto {

    /**
     * 方法
     */
    @JSONField(name = "Method")
    @NotEmpty
    private String method;

    /**
     * 关联ID
     */
    @JSONField(name = "ObjectId")
    @NotNull
    private Long objectId;

    /**
     * 关联类型，api/route 默认路由 route
     */
    @JSONField(name = "ObjectType")
    private String objectType = Const.ROUTE;

    /**
     * 接口名
     */
    @JSONField(name = "InterfaceName")
    private String interfaceName;

    /**
     * 版本
     */
    @JSONField(name = "Version")
    private String version;

    /**
     * 是否开启自定义参数映射开关， 默认false
     */
    @JSONField(name = "CustomParamMapping")
    private boolean customParamMapping;

    /**
     * 分组
     */
    @JSONField(name = "Group")
    private String group;
    /**
     * <形参名>:<类型名>
     * Dubbo参数
     */
    @JSONField(name = "Params")
    private List<DubboParam> params = new ArrayList<>();

    public static DubboInfoDto toDto(DubboInfo info) {
        if (info == null) {
            return null;
        }
        DubboInfoDto dubboInfoDto = new DubboInfoDto();
        dubboInfoDto.setObjectId(info.getObjectId());
        dubboInfoDto.setObjectType(info.getObjectType());
        DubboMeta meta = JSON.parseObject(info.getDubboInfo(), DubboMeta.class);
        dubboInfoDto.setMethod(meta.getMethod());
        dubboInfoDto.setInterfaceName(meta.getInterfaceName());
        dubboInfoDto.setVersion(meta.getVersion());
        dubboInfoDto.setGroup(meta.getGroup());
        dubboInfoDto.setParams(meta.getParams());
        dubboInfoDto.setCustomParamMapping(meta.getCustomParamMapping());
        return dubboInfoDto;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<DubboParam> getParams() {
        return params;
    }

    public void setParams(List<DubboParam> params) {
        this.params = params;
    }

    public String getParamToStr() {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (DubboParam param : params) {
            builder.append(param.getKey()).append(":").append(param.getValue()).append(";");
        }
        return builder.substring(0, builder.lastIndexOf(";"));
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean getCustomParamMapping() {
        return customParamMapping;
    }

    public void setCustomParamMapping(boolean customParamMapping) {
        this.customParamMapping = customParamMapping;
    }

    public DubboInfo toMeta() {
        DubboInfo dubboInfo = new DubboInfo();
        dubboInfo.setObjectId(objectId);
        dubboInfo.setObjectType(objectType);
        DubboMeta meta = new DubboMeta();
        meta.setGroup(group);
        meta.setVersion(version);
        meta.setMethod(method);
        meta.setInterfaceName(interfaceName);
        meta.setParams(params);
        meta.setCustomParamMapping(customParamMapping);
        dubboInfo.setDubboInfo(JSON.toJSONString(meta));
        return dubboInfo;
    }

    public static class DubboMeta {

        /**
         * 方法
         */
        private String method;


        /**
         * 接口名
         */
        private String interfaceName;

        /**
         * 版本
         */
        private String version;

        /**
         * 分组
         */
        private String group;
        /**
         * <形参名>:<类型名>
         * Dubbo参数
         */
        private List<DubboParam> params;


        /**
         * 是否开启自定义参数映射开关， 默认false
         */
        private boolean customParamMapping;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public List<DubboParam> getParams() {
            return params;
        }

        public void setParams(List<DubboParam> params) {
            this.params = params;
        }

        public boolean getCustomParamMapping() {
            return customParamMapping;
        }

        public void setCustomParamMapping(boolean customParamMapping) {
            this.customParamMapping = customParamMapping;
        }
    }

    public static class DubboParam {
        /**
         * key
         */
        @JSONField(name = "Key")
        private String key;
        /**
         * value
         */
        @JSONField(name = "Value")
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
