package org.hango.cloud.common.infra.base.meta;

import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 扩展属性提供基类，供内部使用
 * @date 2022/4/6
 */
public class CommonExtension {

    /**
     * 公共扩展属性
     */
    @TableField(exist = false)
    private Object extension;

    public Object getExtension() {
        return extension;
    }

    public void setExtension(Object extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
