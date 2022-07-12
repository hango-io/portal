package org.hango.cloud.dashboard.envoy.innerdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleMapMatchDto;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * api-plane交互，header、param对应的String Match
 *
 * @author hanjiahao
 */
public class EnvoyStringMatchDto {
    @JSONField(name = "Key")
    private String key;
    /**
     * 路由规则匹配方式
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "exact|prefix|regex")
    private String type;

    /**
     * 路由规则匹配值
     */
    @JSONField(name = "Value")
    private String value;

    public static List<EnvoyStringMatchDto> generateDtoFromRouteRuleDto(List<EnvoyRouteRuleMapMatchDto> matchDtos) {
        if (CollectionUtils.isEmpty(matchDtos)) {
            return new ArrayList<>();
        }
        List<EnvoyStringMatchDto> envoyStringMatchDtos = new ArrayList<>();

        matchDtos.forEach(envoyRouteRuleMatchDto -> {
            List<String> values = envoyRouteRuleMatchDto.getValue();
            //多个value以|进行分割
            String value;
            String type = envoyRouteRuleMatchDto.getType();
            if (values.size() == 1) {
                value = values.get(0);
            } else {
                if (Const.URI_TYPE_EXACT.equals(envoyRouteRuleMatchDto.getType())) {
                    values = values.stream().map(EnvoyStringMatchDto::escapeExprSpecialWord).collect(Collectors.toList());
                }
                if (Const.URI_TYPE_PREFIX.equals(envoyRouteRuleMatchDto.getType())) {
                    values = values.stream().map(EnvoyStringMatchDto::prefixStringGenerate).collect(Collectors.toList());
                }
                value = values.stream()
                        .collect(Collectors.joining("|"));
                type = Const.URI_TYPE_REGEX;
            }

            EnvoyStringMatchDto envoyStringMatchDto = new EnvoyStringMatchDto();
            envoyStringMatchDto.setKey(envoyRouteRuleMatchDto.getKey());
            envoyStringMatchDto.setType(type);
            envoyStringMatchDto.setValue(value);
            envoyStringMatchDtos.add(envoyStringMatchDto);
        });
        return envoyStringMatchDtos;
    }

    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    public static String prefixStringGenerate(String keyword) {
        return escapeExprSpecialWord(keyword) + ".*";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
