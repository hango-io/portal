package org.hango.cloud.dashboard.apiserver.service.impl.sdk;

public class SdkConst {

    // SDK生成过程异常错误信息
    public static String API_NOT_FOUND = "Api not found";
    public static String SERVICE_NOT_FOUND = "Service not found";
    public static String MODEL_TEMPLATE_NOT_FOUND = "Model template not found";
    public static String CLIENT_TEMPLATE_NOT_FOUND = "Client temolate not found";
    public static String TEMPLATE_SYNTAX_ERROR = "Template syntax error";
    public static String WRITE_FILE_ERROR = "Write file error";
    public static String PACK_FILE_NOT_FOUND = "Pack file not found";
    public static String TEMPLATE_LOAD_FIALED = "Failed to load template";

    // 一般常量
    public static String CODE_DIRECTORY = SdkConst.class.getResource("/").getPath() + "codeGenerate/";
    public static String SERVICE_API_GENERATE_SUCCESS = "success";
    public static boolean SINGLE_API_SDK = true;
    public static boolean MULTIPLE_API_SDK = false;
    public static int NORMAL_PARAM_TYPE = 0;

}
