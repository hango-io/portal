package org.hango.cloud.common.infra.cache.meta;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author zhufengwei
 * @Date 2023/6/26
 */
@Getter
@Setter
@TableName("hango_cache_info")
public class CacheInfo implements Serializable {

    private static final long serialVersionUID = -1301978370588261382L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;


    @TableField(fill = FieldFill.INSERT)
    private Long createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 缓存key
     */
    private String cacheKey;

    /**
     * 缓存value
     */
    private String cacheValue;
}
