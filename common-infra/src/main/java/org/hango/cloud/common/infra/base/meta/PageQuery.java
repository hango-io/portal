package org.hango.cloud.common.infra.base.meta;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/3/16
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PageQuery {

    /**
     * 顺序 asc/desc
     */
    @JSONField(name = "SortByValue")
    @Pattern(regexp = "|asc|desc")
    private String sortByValue;

    /**
     * 分页限制
     */
    @JSONField(name = "Limit")
    @Range(min = 1, max = 1000, message = "limit格式不合法")
    private Integer limit;

    /**
     * 起始偏移量
     */
    @JSONField(name = "Offset")
    @Min(0)
    private Integer offset;


    /**
     * 转换offset/limit to Page
     * @return
     * @param <T>
     */
    public <T> Page<T> of(){
        if (limit == null || limit == 0){
            limit = 20;
        }
        if (offset == null){
            offset = 0;
        }
        int currentPage = offset / limit + 1;
        return new Page<>(currentPage, limit);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
