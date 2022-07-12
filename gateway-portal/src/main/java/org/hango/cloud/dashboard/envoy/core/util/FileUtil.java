package org.hango.cloud.dashboard.envoy.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hango.cloud.dashboard.envoy.core.constant.FileConst.ANNOTATION_1;
import static org.hango.cloud.dashboard.envoy.core.constant.FileConst.ANNOTATION_2;

/**
 * 提供文件相关方法的工具类（文件读写等）
 *
 * @author yutao04
 * @date 2021/12/27
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 获取resources下的指定文件内容，忽略注释
     * 注释标识在FileConst类扩展
     *
     * @param fileName 文件名（例如plugin/setting.conf）
     * @return 忽略了注释的内容行集合
     * @throws FileNotFoundException 若文件为找到则抛出异常
     */
    public static List<String> getFileLinesIgnoreAnnotations(String fileName) throws FileNotFoundException {
        return getLinesOfFileUnderResources(fileName, ANNOTATION_1, ANNOTATION_2);
    }

    /**
     * 获取任意指定文件内容
     *
     * @param fileName       文件名（例如/root/a.conf or tomcat/b.conf）
     * @param beIgnoredChars 指定符号开头的行被忽略（可以不传）
     * @return 内容行集合
     * @throws FileNotFoundException 若文件为找到则抛出异常
     */
    public static List<String> getFileLines(String fileName, String... beIgnoredChars) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException(fileName + " is missing under resources!");
        } else if (!file.canRead()) {
            throw new SecurityException(fileName + " is not allowed for Gportal to read, you have no right to access!");
        } else {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                return getLinesFromStream(fileName, is, beIgnoredChars);
            } catch (IOException e) {
                logger.error("[File operate] file [{}] missing in getFileLines!", fileName);
            } finally {
                closeStream(is);
            }
            // 读文件异常场景返回空集合
            return Collections.emptyList();
        }
    }

    /**
     * 获取resources下的指定文件内容
     *
     * @param fileName       文件名（例如plugin/setting.conf）
     * @param beIgnoredChars 指定符号开头的行被忽略（可以不传）
     * @return 内容行集合
     * @throws FileNotFoundException 若文件为找到则抛出异常
     */
    public static List<String> getLinesOfFileUnderResources(String fileName, String... beIgnoredChars)
            throws FileNotFoundException {
        ClassPathResource classPathResource = new ClassPathResource(fileName);
        if (!classPathResource.exists()) {
            throw new FileNotFoundException(fileName + " is missing under resources!");
        }
        InputStream is = null;
        try {
            is = classPathResource.getInputStream();
            return getLinesFromStream(fileName, is, beIgnoredChars);
        } catch (IOException e) {
            logger.error("[File operate] file [{}] missing in getLinesOfFileUnderResources!", fileName);
        } finally {
            closeStream(is);
        }
        // 读文件异常场景返回空集合
        return Collections.emptyList();
    }

    /**
     * 通用的关闭流方法
     * 例：FileUtil.closeStream(s1, s2, s3, ..., sn)
     *
     * @param streams IO资源
     */
    public static void closeStream(Closeable... streams) {
        for (Closeable stream : streams) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.warn("[File operate] IO stream close file, this is negligible");
                }
            }
        }
    }

    private static List<String> getLinesFromStream(String fileName, InputStream is, String[] beIgnoredChars) {
        // 文本行集合
        List<String> fileContentLines = new ArrayList<>();
        BufferedReader bufferedReader = null;
        String line;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            O_L:
            while ((line = bufferedReader.readLine()) != null) {
                for (String beIgnoredStr : beIgnoredChars) {
                    if (line.startsWith(beIgnoredStr)) {
                        continue O_L;
                    }
                }
                fileContentLines.add(line);
            }
        } catch (IOException e) {
            logger.error("[File operate] file [{}] missing in getLinesFromStream!", fileName);
        } finally {
            closeStream(bufferedReader);
        }
        return fileContentLines;
    }
}
