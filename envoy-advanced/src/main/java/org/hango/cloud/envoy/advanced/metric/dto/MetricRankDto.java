package org.hango.cloud.envoy.advanced.metric.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/28
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@With
@Builder
public class MetricRankDto {

    @JSONField(name = "Name")
    private String name;

    @JSONField(name = "Count")
    private int count;

    @JSONField(name = "Id")
    private long id;
}
