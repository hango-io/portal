package org.hango.cloud.common.infra.operationaudit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhangbj
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    String eventVersion() default "V2.1";

    String eventName();

    String description();

    String userAgent() default "http";

    String eventType() default "userwrite";

}
