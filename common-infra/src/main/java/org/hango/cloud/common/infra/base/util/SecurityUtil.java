package org.hango.cloud.common.infra.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtil {
    private static final Logger log = LoggerFactory.getLogger(SecurityUtil.class);


    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "hango-secret-key";
    public static final String AES = "AES";

    public static String encrypt(String plainText) {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        SecureRandom random = new SecureRandom();
        IvParameterSpec iv = new IvParameterSpec(random.generateSeed(16));
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encryptedData = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        }catch (Exception e){
            log.error("加密数据失败，异常信息",e);
            throw new RuntimeException(e);
        }
    }

//    public static String decrypt(String encryptedText) {
//        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
//        IvParameterSpec iv = new IvParameterSpec(INITIALIZATION_VECTOR.getBytes());
//        try {
//            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
//            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
//            return new String(decryptedData);
//        }catch (Exception e){
//            log.error("解密数据失败，异常信息",e);
//            throw new RuntimeException(e);
//        }
//    }
}
