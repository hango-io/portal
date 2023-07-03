package org.hango.cloud.common.infra.base.util;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 提供文件相关方法的工具类（文件读写等）
 *
 * @author yutao04
 * @date 2021/12/27
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @param bytes    内容
     */
    public static void write(String filePath, byte[] bytes) {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("创建文件, filePath : {}", filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && parentFile.mkdirs()) {
                logger.info("创建文件夹filePath : {}", filePath);
                return;
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file, true)) {

            fos.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @param content  内容
     */
    public static void write(String filePath, String content) {
        if (StringUtils.isBlank(content)) {
            logger.info("内容为空");
            return;
        }
        write(filePath, content, true);
    }

    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @param content  内容
     * @param newline  是否换行
     */
    public static void write(String filePath, String content, boolean newline) {
        if (StringUtils.isBlank(content)) {
            logger.info("内容为空");
            return;
        }
        if (newline) {
            content += BaseConst.NEW_LINE;
        }
        write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }
}
