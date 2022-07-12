package org.hango.cloud.dashboard.apiserver.service.impl.sdk;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SDK代码生成类
 *
 * @author Hu Yuchao(huyuchao@corp.netease.com)
 */
public class CodeGenerator {

    /**
     * 生成数据模型
     *
     * @param dataModelTemp 数据模型模板
     * @param apiModelList  模型信息列表
     * @param UUID          唯一标识符
     * @param serviceName   服务名称
     * @throws IOException
     * @throws TemplateException
     */
    public static void generateDataModel(Template dataModelTemp, List<CreateApiModelDto> apiModelList, String UUID, String serviceName)
            throws IOException, TemplateException {
        String responsePackName = "com.netease.cloud." + serviceName.toLowerCase() + ".model";
        String packagePath = responsePackName.replaceAll("\\.", "/");

        for (CreateApiModelDto apiModel : apiModelList) {
            Map<String, Object> dataModelRoot = new HashMap<String, Object>();
            List<String> importList = new ArrayList<String>();
            boolean listExist = false;
            String modelName = apiModel.getModelName();

            dataModelRoot.put("packageName", responsePackName);
            dataModelRoot.put("modelName", modelName);

            List<BodyParameter> modelParamList = new ArrayList<BodyParameter>();
            for (ApiParamDto modelParam : apiModel.getParams()) {
                if (modelParam.getParamTypeName().equals("Array")) {
                    listExist = true;
                }
                modelParamList.add(new BodyParameter(modelParam));
            }
            dataModelRoot.put("modelParamList", modelParamList);

            // 加入import包
            if (listExist) {
                importList.add("java.util.List");
            }
            dataModelRoot.put("importList", importList);

            // 生成代码
            File dir = new File(SdkConst.CODE_DIRECTORY + UUID + "/sourceCode/" + packagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String clientfileName = StringUtils.capitalize(modelName) + ".java";
            OutputStream clientFos;
            clientFos = new FileOutputStream(new File(dir, clientfileName));
            Writer resOut = new OutputStreamWriter(clientFos, Const.DEFAULT_ENCODING);
            dataModelTemp.process(dataModelRoot, resOut);
            clientFos.close();
            resOut.close();

        }
    }

    /**
     * 生成配置清单
     *
     * @param UUID 唯一识别码
     * @throws IOException
     */
    public static void generateManifest(String UUID) {
        File filePath = new File(SdkConst.CODE_DIRECTORY + UUID + "/sourceCode/META-INF");
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File file = new File(filePath, "MENIFEST.MF");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            String content = "Manifest-Version: 1.0\nCreated-By: com.netease.cloud\n";
            byte[] data = content.getBytes(Const.DEFAULT_ENCODING);
            fileOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 字符串处理 短横线“-”转下划线“_”
     *
     * @param inputString 输入字符串
     * @return 处理后的字符串
     */
    private static String normalize(String inputString) {
        inputString = inputString.replaceAll("-", "_");
        return inputString;
    }

}
