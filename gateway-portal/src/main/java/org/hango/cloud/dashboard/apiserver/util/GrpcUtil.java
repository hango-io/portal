package org.hango.cloud.dashboard.apiserver.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.protobuf.TextFormat.escapeBytes;

/**
 * 网关上也有GrpcUtil，两者包含的方法不同，配合使用
 *
 * @author TC_WANG
 * @data 2019/6/24
 */
public class GrpcUtil {

    /**
     * protoc编译时一定要指定，且是绝对路径
     */
    public static final String PROTO_PATH = "/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/";
    public static final String SERVICE_DESCRIPTOR = "serviceDescriptor";
    public static final String METHOD_DESCRIPTOR = "methodDescriptor";
    public static final String METHOD_INPUT_TYPE = "inputType";
    public static final String METHOD_OUTPUT_TYPE = "outType";
    private static final Logger logger = LoggerFactory.getLogger(GrpcUtil.class);
    /**
     * 用来获取protoc执行结果的线程池
     */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * pb文件和description文件转为base64字符串
     *
     * @throws Exception
     */
    public static String fileToBase64(File file) {
        InputStream in = null;
        String base64Code = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64Code = new BASE64Encoder().encode(bytes);
        } catch (FileNotFoundException e) {
            logger.info("读取文件时，该文件不存在", e);
        } catch (IOException e) {
            logger.info("读取文件时发生异常", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                logger.info("关闭文件流时发生异常", e);
            }
        }
        return base64Code;
    }

    /**
     * 将base64字符串转换为byte[]，构造descriptor对象时，入参可选择byte[]
     *
     * @param base64Code
     * @return
     * @throws Exception
     */
    public static byte[] base64ToByte(String base64Code) {
        byte[] bytes = null;
        try {
            bytes = new BASE64Decoder().decodeBuffer(base64Code);
        } catch (IOException e) {
            logger.warn("将base64字符串转换为byte数组失败，base64为{}", base64Code, e);
        }
        return bytes;
    }

    /**
     * 将byte[]转换为string，提供给前端预览pb文件
     *
     * @param bytes
     * @return
     */
    public static String bytesToString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (IOException e) {
            logger.warn("将bytes[]转换为string时失败.", e);
        }
        return null;
    }


    /**
     * 将base64字符串转换为byte[]，然后生成pb文件
     *
     * @param base64Code 加密的base64
     * @param targetPath 保存的文件路径
     */
    public static void decoderBase64ToFile(String base64Code, String targetPath) {
        FileOutputStream out = null;
        try {
            byte[] buffer = base64ToByte(base64Code);
            if (buffer != null) {
                out = new FileOutputStream(targetPath);
                out.write(buffer);

            } else {
                logger.info("base64转换失败，此时不写入文件，targetPath: {}", targetPath);
            }
        } catch (IOException e) {
            logger.info("写入文件时发生异常，targetPath: {}", targetPath, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.info("关闭文件流时发生异常，targetPath: {}", targetPath, e);
                }
            }
        }
    }

    /**
     * 编译pb文件，要先安装protoc编译依赖
     *
     * @param sourcePath pb文件列表
     * @param targetPath description文件
     */
    public static boolean compilePBToDescription(String sourcePath, String targetPath) {
        String protocCMD = "/usr/local/protobuf/bin/protoc -I/usr/local/protobuf/grpc-gateway-master/third_party/googleapis  --descriptor_set_out=" + targetPath
                + "  " + sourcePath + "  --proto_path=" + PROTO_PATH;

        logger.info("编译的命令为：{}", protocCMD.toString());
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(protocCMD.toString());
            process.waitFor();
            int exitValue = process.exitValue();

            if (exitValue != 0) {
                InputStream inputStream = process.getErrorStream();
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                logger.info("pb编译失败，错误信息为{}", new String(bytes, "UTF-8"));
                return false;
            } else {
                logger.info("pb编译成功");
                return true;
            }
        } catch (Exception e) {
            logger.warn("编译pb文件时发生异常", e);
            return false;
        }
    }


    /**
     * 编译pb文件，要先安装protoc编译依赖
     *
     * @param sourcePath pb文件列表
     * @param targetPath description文件
     */
    public static boolean compilePBToDescription(String targetPath, List<String> sourcePath) {
        StringBuilder protocCMD = new StringBuilder("/usr/local/protobuf/bin/protoc  --include_imports --include_source_info -I/usr/local/protobuf/grpc-gateway-master/third_party/googleapis  --descriptor_set_out=");
        protocCMD.append(targetPath + " ");
        sourcePath.stream().forEach(path -> protocCMD.append(path + " "));
        protocCMD.append("--proto_path=");
        protocCMD.append(PROTO_PATH);

        logger.info("编译的命令为：{}", protocCMD.toString());
        Process process;
        InputStream successInputStream = null;
        InputStream errorInputStream = null;
        try {
            process = Runtime.getRuntime().exec(protocCMD.toString());

            /**
             * 通过两个线程分别读取protoc执行的输出流，防止因输出流缓存区太小而protoc输出信息很大的时候导致缓冲区被填满，
             * exec()线程被永远阻塞导致应用程序完全hang住
             */
            successInputStream = process.getInputStream();
            errorInputStream = process.getErrorStream();
            printCompilePBToDescriptionMessage(successInputStream, false);
            printCompilePBToDescriptionMessage(errorInputStream, true);
            process.waitFor();

            int exitValue = process.exitValue();
            if (exitValue != 0) {
                return false;
            } else {
                logger.info("pb编译成功");
                return true;
            }
        } catch (Exception e) {
            logger.warn("编译pb文件时发生异常", e);
            return false;
        } finally {
            try {
                if (successInputStream != null) {
                    successInputStream.close();
                }
                if (errorInputStream != null) {
                    errorInputStream.close();
                }
            } catch (Exception e) {
                logger.warn("关闭pb编译结果的输出流时失败", e);
            }
        }
    }

    /**
     * 用于读取protoc执行的结果
     *
     * @param input
     * @param error
     */
    private static void printCompilePBToDescriptionMessage(final InputStream input, boolean error) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                BufferedReader bf = new BufferedReader(new InputStreamReader(input));
                String result;
                try {
                    while ((result = bf.readLine()) != null && error) {
                        logger.info("pb编译失败，错误信息为: {}", result);
                    }
                } catch (IOException e) {
                    logger.warn("读取pb编译结果的输出流时失败", e);
                }
            }
        });
    }

    /**
     * 读取description文件
     *
     * @param targetPath description文件
     */
    public static DescriptorProtos.FileDescriptorSet readDescriptionToDescriptor(String targetPath) throws Exception {
        DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(new FileInputStream(targetPath));
        return descriptorSet;
    }

    /**
     * 通过byte[]获得FileDescriptorSet对象
     *
     * @param bytes description文件转换为的字节数组
     */
    public static DescriptorProtos.FileDescriptorSet readDescriptionToDescriptor(byte[] bytes) throws Exception {
        DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(bytes);
        return descriptorSet;
    }

    /**
     * 获取API对应的ServiceDescriptor和MethodDescriptor，缓存(TODO)
     *
     * @param bytes
     * @param serviceDescriptorName
     * @param methodDescriptorName
     * @return
     */
    public static Map<String, Object> getServiceMethodMessageDescriptor(byte[] bytes, String serviceDescriptorName, String methodDescriptorName) {
        Map<String, Object> descriptorMap = new HashMap<>();

        //获取Descriptor对象（每个元素都对应一个Descriptor对象）
        DescriptorProtos.FileDescriptorSet descriptorSet;

        //message对应的Descriptor
        Descriptors.Descriptor pbDescritpor = null;

        //Service对应的Descriptor
        Descriptors.ServiceDescriptor serviceDescritpor = null;

        //Method对应的Descriptor
        Descriptors.MethodDescriptor methodDescriptor = null;

        try {
            descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(bytes);
            for (DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {
                Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fdp, new Descriptors.FileDescriptor[]{});
                logger.info("package is " + fileDescriptor.getFile().getPackage());

                for (Descriptors.ServiceDescriptor descriptor : fileDescriptor.getServices()) {
                    if (descriptor.getName().equals(serviceDescriptorName)) {
                        logger.info("service名为 " + serviceDescriptorName + " 的服务已找到");
                        serviceDescritpor = descriptor;
                        descriptorMap.put(SERVICE_DESCRIPTOR, serviceDescritpor);
                        for (Descriptors.MethodDescriptor methodDescriptorTemp : descriptor.getMethods()) {
                            if (methodDescriptorTemp.getName().equals(methodDescriptorName)) {
                                logger.info(methodDescriptorName + " 方法已找到，且入参类型是否为stream " + methodDescriptorTemp.toProto().getClientStreaming() + "和返回值类型是否为stream " + methodDescriptorTemp.toProto().getServerStreaming());
                                methodDescriptor = methodDescriptorTemp;
                                descriptorMap.put(METHOD_DESCRIPTOR, methodDescriptor);
                                descriptorMap.put(METHOD_INPUT_TYPE, methodDescriptor.getInputType());
                                descriptorMap.put(METHOD_OUTPUT_TYPE, methodDescriptor.getOutputType());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("读取Descriptor对象时发生异常.", e);
        }
        return descriptorMap;
    }

    /**
     * 将message对象转换为json
     *
     * @param pbDescriptor
     * @return
     * @throws Exception
     */
    public static String convertMessageToJson(Descriptors.Descriptor pbDescriptor) {
        DynamicMessage.Builder pbBuilder = DynamicMessage.newBuilder(pbDescriptor);
        try {
            return com.google.protobuf.util.JsonFormat.printer().includingDefaultValueFields().print(pbBuilder.build());
        } catch (InvalidProtocolBufferException e) {
            logger.info("为message生成json字符串时失败", e);
            return null;
        }
    }

    /**
     * 读取pb中package、service、method信息，返回的map结构为：
     * <p>
     * {
     * "PackageName":"com.helloworld",
     * "ServiceList":[
     * {
     * "ServiceName":"Greeter1",
     * "MethodList":[
     * {
     * "MethodName":"method1",
     * "InputType":"",
     * "OutPutType":""
     * }
     * ]
     * }
     * ]
     * }
     *
     * @param base64Code
     * @return
     */
    public static Map<String, Object> getPackageServiceMethodMap(String base64Code, DescriptorProtos.FileDescriptorSet descriptorSet) {
        Map<String, Object> packageServiceMethodMap = new HashMap<>();
        if (descriptorSet == null) {
            byte[] bytes = base64ToByte(base64Code);
            try {
                descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                logger.warn("将byte[]转换为FileDescriptorSet时发生异常", e);
                return packageServiceMethodMap;
            }
        }

        for (DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {
            Descriptors.FileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = Descriptors.FileDescriptor.buildFrom(fdp, new Descriptors.FileDescriptor[]{});
            } catch (Descriptors.DescriptorValidationException e) {
                logger.warn("从descriptorSet获取FileDescriptorProto转换为FileDescriptorSet时发生异常", e);
                return packageServiceMethodMap;
            }
            packageServiceMethodMap.put("PackageName", fileDescriptor.getFile().getPackage());
            List<Map<String, Object>> serviceList = new ArrayList<>();
            packageServiceMethodMap.put("ServiceList", serviceList);

            for (Descriptors.ServiceDescriptor serviceDescriptor : fileDescriptor.getServices()) {
                Map<String, Object> serviceMethodMap = new HashMap<>();
                serviceMethodMap.put("ServiceName", serviceDescriptor.getName());
                List<Map<String, String>> methodList = new ArrayList<>();
                serviceMethodMap.put("MethodList", methodList);
                for (Descriptors.MethodDescriptor methodDescriptor : serviceDescriptor.getMethods()) {
                    Map<String, String> method = new HashMap<>();
                    if (methodDescriptor.toProto().getServerStreaming() || methodDescriptor.toProto().getClientStreaming()) {
                        //过滤到所有入参和返回值为stream的方法
                        continue;
                    }
                    method.put("MethodName", methodDescriptor.getName());
                    method.put("InputType", convertMessageToJson(methodDescriptor.getInputType()));
                    method.put("OutputType", convertMessageToJson(methodDescriptor.getOutputType()));
                    methodList.add(method);
                }
                serviceList.add(serviceMethodMap);
            }
        }
        return packageServiceMethodMap;
    }


    /**
     * 读取pb中service信息，返回的格式为package.service
     *
     * @param descriptorSet
     * @return
     */
    public static List<String> getPackageServiceList(DescriptorProtos.FileDescriptorSet descriptorSet, String destProtoName) {
        if (descriptorSet == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> packageServiceList = new ArrayList<>();
        for (DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {
            if (!fdp.getName().equals(destProtoName)) {
                continue;
            }
            Descriptors.FileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = Descriptors.FileDescriptor.buildFrom(fdp, new Descriptors.FileDescriptor[]{});
            } catch (Descriptors.DescriptorValidationException e) {
                logger.warn("从descriptorSet获取FileDescriptorProto转换为FileDescriptorSet时发生异常", e);
                return packageServiceList;
            }

            for (Descriptors.ServiceDescriptor serviceDescriptor : fileDescriptor.getServices()) {
                packageServiceList.add(fileDescriptor.getFile().getPackage() + "." + serviceDescriptor.getName());
            }
        }
        return packageServiceList;
    }


    /**
     * 获取pb中所有的MethodDescriptor对象
     *
     * @param descriptorSet
     * @return
     */
    public static Map<String, List<Descriptors.MethodDescriptor>> getPackageServiceMethodMap(DescriptorProtos.FileDescriptorSet descriptorSet) {
        Map<String, List<Descriptors.MethodDescriptor>> serviceMethodMap = new HashMap<>();
        List<Descriptors.MethodDescriptor> methodDescriptorList = new ArrayList<>();

        for (DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {
            Descriptors.FileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = Descriptors.FileDescriptor.buildFrom(fdp, new Descriptors.FileDescriptor[]{});
            } catch (Descriptors.DescriptorValidationException e) {
                logger.warn("从descriptorSet获取FileDescriptorProto转换为FileDescriptorSet时发生异常", e);
                return serviceMethodMap;
            }

            for (Descriptors.ServiceDescriptor serviceDescriptor : fileDescriptor.getServices()) {
                for (Descriptors.MethodDescriptor methodDescriptor : serviceDescriptor.getMethods()) {
                    if (methodDescriptor.toProto().getServerStreaming() || methodDescriptor.toProto().getClientStreaming()) {
                        //过滤到所有入参和返回值为stream的方法
                        continue;
                    }
                    methodDescriptorList.add(methodDescriptor);
                }
                serviceMethodMap.put(serviceDescriptor.getName(), methodDescriptorList);
            }
        }
        return serviceMethodMap;
    }

    /**
     * 使用这个方法读取HttpRule的定义时，要求option中定义的一定是google.api.http，其唯一标识为72295728
     * Map的格式为：
     * {72295728:{6:"/v1/messages/{message_id}",7:"message"}}
     *
     * @param methodDescriptor
     * @throws Exception
     */
    public static Map getUrlMap(Descriptors.MethodDescriptor methodDescriptor) {
        UnknownFieldSet unknownFieldSet = methodDescriptor.toProto().getOptions().getUnknownFields();
        Map<Integer, Object> map = new HashMap<>();
        Map<Integer, Object> urlMap = new HashMap<>();
        Map<Integer, UnknownFieldSet.Field> fieldMap = unknownFieldSet.asMap();
        for (Map.Entry<Integer, UnknownFieldSet.Field> entry : fieldMap.entrySet()) {
            //仅处理option中为google.api.http定义的情况
            if (entry.getKey() == 72295728) {
                List<ByteString> byteStringList = entry.getValue().getLengthDelimitedList();

                for (ByteString byteString : byteStringList) {

                    UnknownFieldSet message = null;
                    try {
                        message = UnknownFieldSet.parseFrom(byteString);
                    } catch (Exception e) {
                        String s = escapeBytes(byteString);
                        urlMap.put(entry.getKey(), s);
                        logger.debug("转换为UnknownFieldSet时发生异常", e);
                        continue;
                    }

                    for (Map.Entry<Integer, UnknownFieldSet.Field> entryTemp : message.asMap().entrySet()) {
                        List<ByteString> byteStringListTemp = entryTemp.getValue().getLengthDelimitedList();

                        for (ByteString byteStringTemp : byteStringListTemp) {
                            try {
                                UnknownFieldSet messageTemp = UnknownFieldSet.parseFrom(byteStringTemp);
                                Map<String, Object> tempMap = getUrlMapForAdditionalBindings(messageTemp);
                                urlMap.put(entryTemp.getKey(), tempMap);
                            } catch (Exception e) {
                                String s = escapeBytes(byteStringTemp);
                                urlMap.put(entryTemp.getKey(), s);
                                logger.debug("转换为UnknownFieldSet时发生异常", e);
                            }
                        }
                    }
                }
                map.put(entry.getKey(), urlMap);
                break;
            }
        }
        return map;
    }

    /**
     * 获取method对应的url定义
     * method: "GET"
     * path: "/test"
     * body: ""
     *
     * @param methodDescriptor
     * @return
     */
    public static Map<String, Object> getUrlDefineForEachMethod(Descriptors.MethodDescriptor methodDescriptor) {
        String method = null;
        String path = null;
        String body = null;
        Map<String, Object> urlDefineMap = new HashMap<>();
        Map<Integer, Object> map = getUrlMap(methodDescriptor);
        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            if (entry.getKey() == 72295728) {
                Map<Integer, Object> methodAndPathMap = (Map) entry.getValue();
                for (Map.Entry<Integer, Object> entryTemp : methodAndPathMap.entrySet()) {
                    int pattern = entryTemp.getKey();
                    switch (pattern) {
                        case 2:
                            method = RequestMethod.GET.name();
                            path = (String) entryTemp.getValue();
                            break;
                        case 3:
                            method = RequestMethod.PUT.name();
                            path = (String) entryTemp.getValue();
                            break;
                        case 4:
                            method = RequestMethod.POST.name();
                            path = (String) entryTemp.getValue();
                            break;
                        case 5:
                            method = RequestMethod.DELETE.name();
                            path = (String) entryTemp.getValue();
                            break;
                        case 7:
                            //7表示的是body
                            body = (String) entryTemp.getValue();
                            break;
                        case 11:
                            //11表示的是另一个API的定义。目前不支持
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (map.size() > 0) {
            urlDefineMap.put("method", method);
            urlDefineMap.put("path", path);
            urlDefineMap.put("body", body);
        }
        return urlDefineMap;
    }

    /**
     * 返回pb中所有option中定义的API定义，
     * 其中Map的格式为:
     * method: "GET"
     * path: "/test"
     * body: "message"
     * <p>
     * https://github.com/googleapis/googleapis/blob/master/google/api/http.proto
     *
     * @param descriptorSet
     * @return
     */
    public static List<Map<String, Object>> getUrlDefine(DescriptorProtos.FileDescriptorSet descriptorSet) {
        Map<String, List<Descriptors.MethodDescriptor>> serviceMethodDescriptorMap = getPackageServiceMethodMap(descriptorSet);

        List<Map<String, Object>> urlList = new ArrayList<>();

        String serviceName = null;
        String packageName = null;
        Map<String, Object> urlDefineMap;

        for (DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {
            Descriptors.FileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = Descriptors.FileDescriptor.buildFrom(fdp, new Descriptors.FileDescriptor[]{});
            } catch (Descriptors.DescriptorValidationException e) {
                logger.info("从descriptorSet获取FileDescriptorProto转换为FileDescriptorSet时发生异常", e);
                return null;
            }
            packageName = fileDescriptor.getFile().getPackage();
        }
        for (Map.Entry<String, List<Descriptors.MethodDescriptor>> entry : serviceMethodDescriptorMap.entrySet()) {
            //获取服务名称
            serviceName = entry.getKey();
            List<Descriptors.MethodDescriptor> methodDescriptorList = entry.getValue();
            for (Descriptors.MethodDescriptor methodDescriptor : methodDescriptorList) {
                urlDefineMap = getUrlDefineForEachMethod(methodDescriptor);
                if (urlDefineMap.size() == 0) {
                    continue;
                }

                urlDefineMap.put("InputType", methodDescriptor.getInputType());
                urlDefineMap.put("OutputType", methodDescriptor.getOutputType());
                urlDefineMap.put("methodName", methodDescriptor.getName());
                urlDefineMap.put("serviceName", serviceName);
                urlDefineMap.put("packageName", packageName);
                urlList.add(urlDefineMap);
            }
        }

        return urlList;
    }


    /**
     * 递归处理option中包含additional_bindings的情况
     *
     * @param unknownFieldSet
     * @return
     * @throws Exception
     */
    public static Map getUrlMapForAdditionalBindings(UnknownFieldSet unknownFieldSet) throws Exception {
        Map<Integer, Object> urlMap = new HashMap<>();
        boolean flag = false;
        Map<Integer, UnknownFieldSet.Field> fieldMap = unknownFieldSet.asMap();
        for (Map.Entry<Integer, UnknownFieldSet.Field> entry : fieldMap.entrySet()) {
            List<ByteString> byteStringList = entry.getValue().getLengthDelimitedList();

            for (ByteString byteString : byteStringList) {

                UnknownFieldSet message = null;
                try {
                    message = UnknownFieldSet.parseFrom(byteString);
                } catch (Exception e) {
                    logger.info("读取option定义时，转换为UnknownFieldSet时发生异常: {},可忽略", e.getMessage());
                    String s = escapeBytes(byteString);
                    urlMap.put(entry.getKey(), s);
                    continue;
                }

                for (Map.Entry<Integer, UnknownFieldSet.Field> entryTemp : message.asMap().entrySet()) {
                    List<ByteString> byteStringListTemp = entryTemp.getValue().getLengthDelimitedList();

                    for (ByteString byteStringTemp : byteStringListTemp) {
                        try {
                            UnknownFieldSet messageTemp = UnknownFieldSet.parseFrom(byteStringTemp);
                            Map<String, Object> tempMap = getUrlMapForAdditionalBindings(messageTemp);
                            urlMap.put(entryTemp.getKey(), tempMap);
                        } catch (Exception e) {
                            logger.info("读取option定义时，转换为UnknownFieldSet时发生异常: {},可忽略", e.getMessage());
                            String s = escapeBytes(byteStringTemp);
                            urlMap.put(entryTemp.getKey(), s);
                        }
                    }
                }
            }
        }
        return urlMap;
    }


    /**
     * 判断Field是否是基本类型，暂不支持枚举类型
     * <p>
     * 转换为javaType的对应关系为：
     * <p>
     * DOUBLE(Descriptors.FieldDescriptor.JavaType.DOUBLE),
     * FLOAT(Descriptors.FieldDescriptor.JavaType.FLOAT),
     * INT64(Descriptors.FieldDescriptor.JavaType.LONG),
     * UINT64(Descriptors.FieldDescriptor.JavaType.LONG),
     * INT32(Descriptors.FieldDescriptor.JavaType.INT),
     * FIXED64(Descriptors.FieldDescriptor.JavaType.LONG),
     * FIXED32(Descriptors.FieldDescriptor.JavaType.INT),
     * BOOL(Descriptors.FieldDescriptor.JavaType.BOOLEAN),
     * STRING(Descriptors.FieldDescriptor.JavaType.STRING),
     * GROUP(Descriptors.FieldDescriptor.JavaType.MESSAGE),
     * MESSAGE(Descriptors.FieldDescriptor.JavaType.MESSAGE),
     * BYTES(Descriptors.FieldDescriptor.JavaType.BYTE_STRING),
     * UINT32(Descriptors.FieldDescriptor.JavaType.INT),
     * ENUM(Descriptors.FieldDescriptor.JavaType.ENUM),
     * SFIXED32(Descriptors.FieldDescriptor.JavaType.INT),
     * SFIXED64(Descriptors.FieldDescriptor.JavaType.LONG),
     * SINT32(Descriptors.FieldDescriptor.JavaType.INT),
     * SINT64(Descriptors.FieldDescriptor.JavaType.LONG);
     *
     * @param field
     * @return
     */
    public static boolean judgeBasicType(Descriptors.FieldDescriptor field) {
        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.FLOAT || field.getJavaType() == Descriptors.FieldDescriptor.JavaType.DOUBLE ||
                field.getJavaType() == Descriptors.FieldDescriptor.JavaType.INT || field.getJavaType() == Descriptors.FieldDescriptor.JavaType.LONG ||
                field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
            return true;
        }
        return false;
    }

    /**
     * 获取Message对应的queryString集合，仅处理基本类型的参数
     *
     * @param pbDescriptor
     * @return
     */
    public static Map<String, String> getQueryStringMap(Descriptors.Descriptor pbDescriptor) {
        Map<String, String> queryStringMap = new HashMap<>();
        DynamicMessage.Builder pbBuilder = DynamicMessage.newBuilder(pbDescriptor);
        List<Descriptors.FieldDescriptor> fieldList = pbBuilder.getDescriptorForType().getFields();

        for (Descriptors.FieldDescriptor field : fieldList) {
            //如果field类型为基本类型
            if (judgeBasicType(field)) {
                switch (field.getJavaType()) {
                    case FLOAT:
                        queryStringMap.put(field.getName(), "Float");
                        break;
                    case INT:
                        queryStringMap.put(field.getName(), "Int");
                        break;
                    case LONG:
                        queryStringMap.put(field.getName(), "Long");
                        break;
                    case DOUBLE:
                        queryStringMap.put(field.getName(), "Double");
                        break;
                    default:
                        queryStringMap.put(field.getName(), "String");
                }
            }
        }
        return queryStringMap;
    }

    /**
     * 获取path中的变量集合
     *
     * @param apiPath
     * @return
     */
    public static Set<String> getPathVariable(String apiPath) {
        Set<String> pathVariableSet = new HashSet<>();
        String[] apiPathPartList = apiPath.split("/");
        for (int i = 0; i < apiPathPartList.length; i++) {
            String apiPathPart = apiPathPartList[i];
            if (apiPathPart.startsWith("{") && apiPathPart.endsWith("}")) {
                pathVariableSet.add(apiPathPart.substring(apiPathPart.indexOf("{") + 1, apiPathPart.lastIndexOf("}")));
            }
        }

        return pathVariableSet;
    }

}


