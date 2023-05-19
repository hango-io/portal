package org.hango.cloud.common.infra.base.invoker;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.annotation.StringCheckInList;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author yutao04
 */
public class StringSizeInListValidator implements ConstraintValidator<StringCheckInList, List<String>> {
    private int min;
    private int max;
    private Pattern pattern = null;
    private boolean unique;

    @Override
    public void initialize(StringCheckInList constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
        unique = constraintAnnotation.unique();

        String regex = constraintAnnotation.regex();
        if (!StringUtils.isEmpty(regex)) {
            pattern = Pattern.compile(regex);
        }
    }

    @Override
    public boolean isValid(List<String> strings, ConstraintValidatorContext context) {
        if (strings == null) {
            return false;
        }

        for (String str : strings) {
            if (str == null || str.length() < min || str.length() > max) {
                return false;
            }
            if (pattern != null && !pattern.matcher(str).find()) {
                return false;
            }
        }

        // 是否重复
        if (unique) {
            return strings.size() == strings.stream().distinct().count();
        }

        return true;
    }
}