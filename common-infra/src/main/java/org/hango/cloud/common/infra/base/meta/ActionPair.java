package org.hango.cloud.common.infra.base.meta;

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
 * @date 2022/4/27
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class ActionPair {
    /**
     * action , used to build request query param. key as {@link BaseConst#ACTION}
     */
    private String action;

    /**
     * version , used to build request query param. key as {@link BaseConst#VERSION}
     */
    private String version;
}
