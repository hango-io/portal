package org.hango.cloud.common.infra.base.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/16
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@With
public class PageTimeQueryDto extends TimeQueryDto{


    @JSONField(name = "Limit")
    private int limit = 20;

    @JSONField(name = "Offset")
    private int offset = 0;
}
