package org.hango.cloud.dashboard.apiserver.service.impl.restfulSdk;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.BodyParameter;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.CodePacker;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.HeaderParameter;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.SdkConst;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

@Service
public class RestfulCodeGenerator {
    private static Logger logger = LoggerFactory.getLogger(RestfulCodeGenerator.class);
    private static String pacakgePrefix = "com.netease.cloud.";
    private static String packagePost = ".model";
    private static String clientPackagePost = ".client";
    private static String sdkPrefix = "java-sdk-";
    private static String sdkPost = ".jar";

    /**
     * 根据ftl
     *
     * @param requestTemp
     * @param apiInfo
     * @param apiQueryString
     * @param apiRequestHeaders
     * @param apiRequestBody
     * @param UUID
     * @throws IOException
     * @throws TemplateException
     */
    public static void generateRequest(Template requestTemp, ApiInfo apiInfo, ServiceInfo serviceInfo, List<ApiBody> apiQueryString, List<ApiHeader> apiRequestHeaders,
                                       List<ApiBody> apiRequestBody, String UUID) throws IOException, TemplateException {
        // 模板根数据总表
        Map<String, Object> requestRoot = new HashMap<String, Object>();
        // 导入包列表
        List<String> importList = new ArrayList<String>();
        boolean listExist = false;
        String className = RestfulSdkUtils.getApiClassName(apiInfo);
        requestRoot.put("className", className);
        // 加入包名
        String serviceName = serviceInfo.getServiceName();
        //去除服务标识中的特殊字符，仅保留数字和字母（兼容适配服务标识路由）
        serviceName = serviceName.replaceAll("[^a-zA-Z0-9]", "");
        String requestPackName = pacakgePrefix + serviceName.toLowerCase() + packagePost;
        String packagePath = requestPackName.replaceAll("\\.", "/");
        requestRoot.put("packageName", requestPackName);

        // 加入构造参数
        String uri = apiInfo.getApiPath();
        String apiMethod = apiInfo.getApiMethod();
        requestRoot.put("uri", uri);
        requestRoot.put("serviceName", serviceName);
        requestRoot.put("apiMethod", apiMethod);

        //加入request类的path参数
        List<BodyParameter> requestPathStringList = new ArrayList<>();
        requestRoot.put("requestPathStringList", RestfulSdkUtils.getPathParams(requestPathStringList, uri));

        // 加入request类QueryString参数
        List<BodyParameter> requestQueryStringList = new ArrayList<BodyParameter>();
        for (ApiBody apiBody : apiQueryString) {
            if (apiBody.getParamType().equals("Array")) {
                listExist = true;
            }
            requestQueryStringList.add(new BodyParameter(apiBody));
        }
        requestRoot.put("requestQueryStringList", requestQueryStringList);

        // 加入request类header参数
        List<HeaderParameter> requestHeaderList = new ArrayList<HeaderParameter>();
        for (ApiHeader apiHeader : apiRequestHeaders) {
            requestHeaderList.add(new HeaderParameter(normalize(apiHeader.getParamName()), normalize(apiHeader.getParamValue())));
        }
        requestRoot.put("requestHeaderList", requestHeaderList);

        // 加入request类body参数
        List<BodyParameter> requestBodyList = new ArrayList<BodyParameter>();
        for (ApiBody apiBody : apiRequestBody) {
            if (apiBody.getParamType().equals("Array")) {
                listExist = true;
            }
            requestBodyList.add(new BodyParameter(apiBody));
        }
        requestRoot.put("requestBodyList", requestBodyList);

        // 加入import包
        if (listExist) {
            importList.add("java.util.List");
        }
        requestRoot.put("importList", importList);

        // 生成代码
        File dir = new File(SdkConst.CODE_DIRECTORY + UUID + "/sourceCode/" + packagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //request文件名
        String reqfileName = className + "Request.java";
        OutputStream reqFos;
        reqFos = new FileOutputStream(new File(dir, reqfileName));
        Writer reqOut = new OutputStreamWriter(reqFos, Const.DEFAULT_ENCODING);
        requestTemp.process(requestRoot, reqOut);
        reqFos.close();
        reqOut.close();
    }

    /**
     * 生成请求client客户端
     *
     * @param clientTemp
     * @param apiInfoList
     * @param serviceName
     * @param UUID
     * @throws TemplateException
     * @throws IOException
     */
    public static void generateClient(Template clientTemp, List<ApiInfo> apiInfoList, String serviceName, String UUID)
            throws TemplateException, IOException {
        // 根模板数据总表
        Map<String, Object> clientRoot = new HashMap<String, Object>();
        //去除服务标识中的特殊字符，仅保留数字和字母（兼容适配服务标识路由）
        serviceName = serviceName.replaceAll("[^a-zA-Z0-9]", "");
        String responsePackName = pacakgePrefix + serviceName.toLowerCase() + clientPackagePost;
        String packagePath = responsePackName.replaceAll("\\.", "/");
        clientRoot.put("packageName", responsePackName);
        clientRoot.put("serviceName", serviceName);

        // 加入所属服务所有API操作列表
        List<String> apiNameList = new ArrayList<String>();
        for (ApiInfo apiInfo : apiInfoList) {
            apiNameList.add(RestfulSdkUtils.getApiClassName(apiInfo));
        }
        clientRoot.put("apiNameList", apiNameList);

        // 生成代码
        File dir = new File(SdkConst.CODE_DIRECTORY + UUID + "/sourceCode/" + packagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //类名称，首字母大写
        String clientfileName = StringUtils.capitalize(serviceName) + "Client.java";
        OutputStream clientFos = new FileOutputStream(new File(dir, clientfileName));
        Writer resOut = new OutputStreamWriter(clientFos, Const.DEFAULT_ENCODING);
        clientTemp.process(clientRoot, resOut);
        clientFos.close();
        resOut.close();
    }

    public static void generateResponse(Template responseTemp, ApiInfo apiInfo, ServiceInfo serviceInfo, List<ApiHeader> apiResponseHeaders,
                                        List<ApiBody> apiResponseBody, String UUID) throws TemplateException, IOException {
        // 根模板数据总表
        Map<String, Object> responseRoot = new HashMap<String, Object>();

        // 导入包列表
        List<String> importList = new ArrayList<String>();
        boolean listExist = false;
        String className = RestfulSdkUtils.getApiClassName(apiInfo);
        responseRoot.put("className", className);
        // 加入包名
        String serviceName = serviceInfo.getServiceName();
        //去除服务标识中的特殊字符，仅保留数字和字母（兼容适配服务标识路由）
        serviceName = serviceName.replaceAll("[^a-zA-Z0-9]", "");
        String responsePackName = pacakgePrefix + serviceName.toLowerCase() + packagePost;
        String packagePath = responsePackName.replaceAll("\\.", "/");
        responseRoot.put("packageName", responsePackName);
        // 加入名称
        responseRoot.put("serviceName", serviceName);

        // 加入response类header参数
        List<HeaderParameter> responseHeaderList = new ArrayList<HeaderParameter>();
        for (ApiHeader apiHeader : apiResponseHeaders) {
            responseHeaderList.add(new HeaderParameter(normalize(apiHeader.getParamName()), normalize(apiHeader.getParamValue())));
        }
        responseRoot.put("responseHeaderList", responseHeaderList);

        // 加入response类body参数
        List<BodyParameter> responseBodyList = new ArrayList<BodyParameter>();
        for (ApiBody apiBody : apiResponseBody) {
            if (apiBody.getParamType().equals("Array")) {
                listExist = true;
            }
            String paramName = apiBody.getParamName();
            if (!paramName.equals("RequestId")) {
                responseBodyList.add(new BodyParameter(apiBody));
            }
        }
        responseRoot.put("responseBodyList", responseBodyList);

        // 加入import包
        if (listExist) {
            importList.add("java.util.List");
        }
        responseRoot.put("importList", importList);

        // 生成代码
        File dir = new File(SdkConst.CODE_DIRECTORY + UUID + "/sourceCode/" + packagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String resfileName = className + "Response.java";
        OutputStream resFos;
        resFos = new FileOutputStream(new File(dir, resfileName));
        Writer resOut = new OutputStreamWriter(resFos, Const.DEFAULT_ENCODING);
        responseTemp.process(responseRoot, resOut);
        resFos.close();
        resOut.close();
    }

    public static String pack(ApiInfo apiInfo, ServiceInfo serviceInfo, String uuid, boolean singleApiFlag) {
        String jarName;
        //去除服务标识中的特殊字符，仅保留数字和字母（兼容适配服务标识路由）
        String serviceName = serviceInfo.getServiceName().toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        if (singleApiFlag) {
            jarName = sdkPrefix + serviceName + "-" + RestfulSdkUtils.getApiClassName(apiInfo).toLowerCase() + sdkPost;
        } else {
            jarName = sdkPrefix + serviceName + sdkPost;
        }
        String codePath = SdkConst.CODE_DIRECTORY + uuid + "/sourceCode";
        String jarPath = SdkConst.CODE_DIRECTORY + uuid + "/jar/" + jarName;
        File file = new File(SdkConst.CODE_DIRECTORY + uuid + "/jar");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            CodePacker.pack(codePath, jarPath);
        } catch (Exception e) {
            logger.error("生成jar文件正常，打包jar文件出现异常：{}", e);
            return null;
        }
        return jarName;
    }

    private static String normalize(String inputString) {
        if (StringUtils.isBlank(inputString)) return StringUtils.EMPTY;
        inputString = inputString.replaceAll("-", "_");
        return inputString;
    }
}
