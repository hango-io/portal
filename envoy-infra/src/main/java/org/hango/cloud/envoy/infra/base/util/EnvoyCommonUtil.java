package org.hango.cloud.envoy.infra.base.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/1/4
 */
public class EnvoyCommonUtil {


    /**
     * 分离dubbo igv{interface:group:version}
     * <p>
     * xxxService ===> new String[]{"xxxService","",""}
     * xxxService:xxxGroup:xxxVersion ===> new String[]{"xxxService","xxxGroup","xxxVersion"}
     * xxxService:xxxGroup ===> new String[]{"xxxService","xxxGroup",""}
     * xxxService::xxxVersion ===> new String[]{"xxxService","","xxxVersion"}
     *
     * @param igv interface:group:version
     * @return String[]{@interfaceName, @group, @version}
     */
    public static String[] splitIgv(String igv) {
        String[] result = new String[3];
        if (StringUtils.isBlank(igv)) {
            return result;
        }
        if (StringUtils.endsWith(igv, BaseConst.DUBBO_SERVICE_SUFFIX)) {
            igv = StringUtils.substring(igv, NumberUtils.INTEGER_ZERO,igv.length() - BaseConst.DUBBO_SERVICE_SUFFIX.length());
        }
        String[] split = igv.split(":");
        for (int i = 0; i < result.length; i++) {
            result[i] = split.length > i ? split[i] : StringUtils.EMPTY;
        }
        return result;
    }

    public static String file2Str(MultipartFile file) {
        if (file == null) {
            return StringUtils.EMPTY;
        }
        try {
            return new String(file.getBytes());
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }

    }
}
