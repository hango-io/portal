package org.hango.cloud.dashboard.apiserver.service.impl.sdk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * 代码打包类
 *
 * @author Hu Yuchao(huyuchao)
 */
public class CodePacker {
    /**
     * 代码打包
     *
     * @param sourceFileName 源文件目录
     * @param jarFileName    jar文件目录
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void pack(String sourceFileName, String jarFileName) throws FileNotFoundException, IOException {
        // java源文件根目录
        File sourceFile = new File(sourceFileName);
        // 输出jar文件目录
        File jarFile = new File(jarFileName);
        // 声明输出流
        JarOutputStream jarOutstream = new JarOutputStream(new FileOutputStream(jarFile));
        // 代码根目录长度
        int rootPathLength = sourceFileName.length();
        // 打包
        packFile(jarOutstream, sourceFile, rootPathLength);
        // 关闭输出流
        jarOutstream.close();
    }

    /**
     * 递归遍历文件打包
     *
     * @param jarOutStream   输出流
     * @param sourceFile     源文件目录
     * @param rootPathLength 根目录长度
     * @throws IOException
     */
    private static void packFile(JarOutputStream jarOutStream, File sourceFile, int rootPathLength) throws IOException {
//		// 如果是目录，递归遍历目录下所有目录及文件
//		if (sourceFile.isDirectory()) {
//			File[] files = sourceFile.listFiles();
//			if (files != null && files.length > 1) {
//				for (File file : files) {
//					packFile(jarOutStream, file, rootPathLength);
//				}
//			}
//		}
//		// 如果是文件，加入jar文件中
//		else {
//			int bufferLen = 1;
//			byte data[] = new byte[bufferLen];
//			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
//			JarEntry jarEntry = new JarEntry(sourceFile.getPath().substring(rootPathLength));
//			jarOutStream.putNextEntry(jarEntry);
//			while ((bufferedInputStream.read(data, 0, bufferLen)) != -1) {
//				jarOutStream.write(data, 0, bufferLen);
//			}
//			bufferedInputStream.close();
//			jarOutStream.closeEntry();
//		}

        // 如果是目录，递归遍历目录下所有目录及文件
        if (sourceFile.isDirectory()) {
            File[] files = sourceFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    packFile(jarOutStream, file, rootPathLength);
                }
            }
        }
        // 如果是文件，加入jar文件中
        else {
            int count;
            int bufferLen = 1;
            byte data[] = new byte[bufferLen];
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            JarEntry jarEntry = new JarEntry(sourceFile.getPath().substring(rootPathLength));
            jarOutStream.putNextEntry(jarEntry);
            while ((count = bufferedInputStream.read(data, 0, bufferLen)) != -1) {
                jarOutStream.write(data, 0, bufferLen);
            }
            bufferedInputStream.close();
            jarOutStream.closeEntry();
        }


    }
}
