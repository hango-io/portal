package org.hango.cloud.dashboard.apiserver.service.impl.exportImport;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.service.JsonConvertService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

public class BaseJsonConvertService<T> implements JsonConvertService<T> {
    private final static String DEFAULT_FILENAME = "exportfile";

    @Override
    public <T> String exportToJson(T data) {
        return JSON.toJSONString(data);
    }

    @Override
    public <T> T importFromJson(String data, Class<T> type) {
        return JSON.parseObject(data, type);
    }

    @Override
    public <T> List<T> importFromJsonArray(String data, Class<T> type) {
        return JSON.parseArray(data, type);
    }


    /**
     * 将导出的json文件输出到客户端
     *
     * @param response   http response
     * @param jsonString json字符串
     * @param fileName   文件名，默认为"exportfile"
     */
    public void downloadJsonFile(HttpServletResponse response, String jsonString, String fileName) {
        try (BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream())) {
            String downloadFileName = (StringUtils.isEmpty(fileName)) ? DEFAULT_FILENAME : fileName;
            downloadFileName = downloadFileName + "-" + System.currentTimeMillis() + ".json";
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + downloadFileName);
            output.write(jsonString.getBytes("UTF-8"));
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取上传的Json文件的内容
     */
    public String getJsonStringFromUploadedFile(MultipartFile file) {
        String serviceJson = null;
        try {
            serviceJson = new String(file.getBytes(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serviceJson;
    }

}
