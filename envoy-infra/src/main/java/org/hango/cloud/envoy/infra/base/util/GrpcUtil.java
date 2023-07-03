package org.hango.cloud.envoy.infra.base.util;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            Base64.Encoder encoder = Base64.getEncoder();
            base64Code = encoder.encodeToString(bytes);
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
        executorService.execute(() -> {
            BufferedReader bf = new BufferedReader(new InputStreamReader(input));
            String result;
            try {
                while ((result = bf.readLine()) != null && error) {
                    logger.info("pb编译失败，错误信息为: {}", result);
                }
            } catch (IOException e) {
                logger.warn("读取pb编译结果的输出流时失败", e);
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
}


