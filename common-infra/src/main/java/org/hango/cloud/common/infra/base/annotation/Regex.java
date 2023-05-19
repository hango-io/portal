package org.hango.cloud.common.infra.base.annotation;


import org.hango.cloud.common.infra.base.invoker.RegexValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {RegexValidator.class})
public @interface Regex {
    String condition() default "{}";
    String regex() default "{}";
    String message() default "{}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
