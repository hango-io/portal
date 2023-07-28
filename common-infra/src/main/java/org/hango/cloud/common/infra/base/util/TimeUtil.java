package org.hango.cloud.common.infra.base.util;

import org.hango.cloud.common.infra.base.meta.BaseConst;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @Author zhufengwei
 * @Date 2023/6/27
 */
public class TimeUtil {

    public static Long getMillTime(LocalDateTime localDateTime){
        if (localDateTime == null){
            return null;
        }
        ZoneId zoneId = ZoneId.of(BaseConst.ZONE_ID);
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }
}
