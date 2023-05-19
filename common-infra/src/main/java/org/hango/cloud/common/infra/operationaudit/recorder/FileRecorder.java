package org.hango.cloud.common.infra.operationaudit.recorder;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.common.infra.base.util.FileUtil;
import org.hango.cloud.common.infra.operationaudit.meta.OperationAudit;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/26
 */
public class FileRecorder extends AbstractRecorder {

    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private static String pattern = "%s/audit/audit_%s.txt";

    @Override
    protected void record(OperationAudit audit, HttpServletRequest request) {
        String property = System.getProperty("catalina.home");
        String format = String.format(pattern, property, FileRecorder.format.format(System.currentTimeMillis()));
        FileUtil.write(format, JSON.toJSONString(audit));
    }
}
