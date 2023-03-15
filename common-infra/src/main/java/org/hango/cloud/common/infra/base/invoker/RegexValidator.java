package org.hango.cloud.common.infra.base.invoker;


import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.annotation.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/9/5 17:58
 **/
public class RegexValidator implements ConstraintValidator<Regex, Object> {

    private static final Logger logger = LoggerFactory.getLogger(RegexValidator.class);

    String condition;
    String regex;

    @Override
    public void initialize(Regex constraintAnnotation) {
        condition = constraintAnnotation.condition();
        regex = constraintAnnotation.regex();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if (obj == null){
            return true;
        }
        try {
            return doValid(obj);
        } catch (Exception e) {
            logger.error("获取正则信息异常，condition:{}, regex:{}", condition, regex, e);
            return false;
        }

    }

    private boolean doValid(Object obj) throws Exception{
        if (!needValid(obj, condition)){
            return true;
        }
        List<String> regexList = parseRegexValue(obj, regex);
        for (String regex : regexList) {
            if (!vaildRegex(regex)){
                return false;
            }
        }
        return true;
    }

    private boolean vaildRegex(String regex){
        try {
            Pattern.compile(regex);
            return true;
        } catch (Exception e) {
            logger.warn("非法的正则表达式,regex:{}", regex);
        }
        return false;
    }

    public List<String> parseRegexValue(Object obj, String regex) throws Exception{
        if (StringUtils.isEmpty(regex)){
            return Collections.singletonList((String) obj);
        }
        Class<?> clazz = obj.getClass();
        Field matchCondition = clazz.getDeclaredField(regex);
        matchCondition.setAccessible(true);
        Object regexObj = matchCondition.get(obj);
        if (regexObj instanceof List){
            return (List<String>) regexObj;
        }else {
            return Collections.singletonList((String) regexObj);
        }
    }

    private boolean needValid(Object obj, String condition) throws Exception{
        if (StringUtils.isEmpty(condition)){
            return true;
        }
        String[] split = condition.split("=");
        if (split.length != 2){
            return false;
        }
        Class<?> clazz = obj.getClass();
        Field matchCondition = clazz.getDeclaredField(split[0]);
        matchCondition.setAccessible(true);
        String matchValue = (String)matchCondition.get(obj);
        return split[1].contains(matchValue);
    }
}
