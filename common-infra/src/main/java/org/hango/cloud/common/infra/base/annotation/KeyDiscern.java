package org.hango.cloud.common.infra.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 字段识别，标注于某个数据bean中个某个独有字段，当存在该字段时，首先获取存在该字段的所有bean，按顺序进转换
 * @date 2022/4/13
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyDiscern {
}
