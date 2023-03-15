package org.hango.cloud.common.infra.base.config;


import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.hango.cloud.common.infra.base.annotation.JSONPolymorphism;

import java.lang.reflect.Type;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc JSON 实现多态配置 ，搭配{@link JSONPolymorphism} 多态注解 及 {@link JSONPolymorphismDeserializer} JSON多态解析器使用
 * @date 2022/4/13
 */
public class JSONParserConfig extends ParserConfig {

    public JSONParserConfig() {
        super();
    }

    @Override
    public ObjectDeserializer getDeserializer(Class<?> clazz, Type type) {
        if (clazz.isAnnotationPresent(JSONPolymorphism.class)) {
            return new JSONPolymorphismDeserializer(this, clazz, type);
        }
        return super.getDeserializer(clazz, type);
    }
}
