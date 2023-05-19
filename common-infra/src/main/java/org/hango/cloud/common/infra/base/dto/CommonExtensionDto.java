package org.hango.cloud.common.infra.base.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.annotation.JSONPolymorphism;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 扩展属性提供基类，暴露外部数据
 * @date 2022/4/6
 */
@JSONPolymorphism
public class CommonExtensionDto {

    /**
     * 公共扩展属性
     */
    @JSONField(name = "Extension", serialize = false)
    private Object extension;

    public Object getExtension() {
        return extension;
    }

    public void setExtension(Object extension) {
        this.extension = extension;
    }

    public static <R extends CommonExtensionDto, R1 extends CommonExtensionDto> R cast(R1 r1) {
        String content = JSON.toJSONString(r1);
        return JSON.parseObject(content, new TypeReference<R>() {
        });
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
