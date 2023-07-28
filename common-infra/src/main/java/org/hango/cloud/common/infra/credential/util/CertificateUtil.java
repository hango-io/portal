package org.hango.cloud.common.infra.credential.util;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;

public class CertificateUtil {
    private static final String X509 = "X.509";
    private static final String ZERO = "0";
    private static final String SHA = "SHA-1";
    private static final String CN = "CN";


    public static CertificateInfoPO resolve(String content) throws Exception{
        InputStream is = new ByteArrayInputStream(content.getBytes());
        CertificateFactory fact = CertificateFactory.getInstance(X509);
        X509Certificate certificate = (X509Certificate) fact.generateCertificate(is);
        CertificateInfoPO certificateInfoPO = new CertificateInfoPO();
        certificateInfoPO.setContent(content);
        String domain = certificate.getSubjectDN().getName();
        String agencyName = certificate.getIssuerX500Principal().getName();
        certificateInfoPO.setHost(parseCnName(domain));
        certificateInfoPO.setSignature(getThumbprint(certificate.getSignature()));
        certificateInfoPO.setIssuingAgency(parseCnName(agencyName));
        certificateInfoPO.setIssuingTime(certificate.getNotBefore().getTime());
        certificateInfoPO.setExpiredTime(certificate.getNotAfter().getTime());
        return certificateInfoPO;
    }

    public static boolean isValidity(String content) {
        InputStream is = new ByteArrayInputStream(content.getBytes());
        try {
            CertificateFactory fact = CertificateFactory.getInstance(X509);
            X509Certificate certificate = (X509Certificate) fact.generateCertificate(is);
            certificate.checkValidity();
        } catch (CertificateException e) {
            return false;
        }
        return true;
    }

    private static String getThumbprint(byte[] signature) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA);
            md.update(signature);
            List<String> thumbprintList = new ArrayList<>();
            for (byte b : md.digest()) {
                thumbprintList.add(byteToHex(b));
            }
            return String.join(SYMBOL_COLON, thumbprintList).toLowerCase();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String byteToHex(byte b){
        String hex = Integer.toHexString(b & 0xFF);
        if(hex.length() < 2){
            hex = ZERO + hex;
        }
        return hex;
    }

    /**
     * 解析证书名
     * @param 'CN=example.com, O=example Inc.'
     * @return example.com
     */
    public static String parseCnName(String nameList){
        if (StringUtils.isBlank(nameList)){
            return null;
        }
        for (String nameStr : nameList.split(SYMBOL_COMMA)) {
            if (nameStr.trim().startsWith(CN)){
                String[] name = nameStr.split(SYMBOL_EQUAL);
                if (name.length >= 2){
                    return name[1].trim();
                }
            }
        }
        return null;
    }

}
