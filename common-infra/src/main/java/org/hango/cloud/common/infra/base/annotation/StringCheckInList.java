package org.hango.cloud.common.infra.base.annotation;

import org.hango.cloud.common.infra.base.invoker.StringSizeInListValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yutao04
 */
@Constraint(validatedBy = StringSizeInListValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StringCheckInList {
    String message() default "Invalid string in the list";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String regex() default "";
    boolean unique() default false;
}