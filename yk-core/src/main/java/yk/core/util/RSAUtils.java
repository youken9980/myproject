package yk.core.util;

/** *//**
 * <p>
 * RSA公钥/私钥/签名工具包
 * </p>
 * <p>
 * 罗纳德·李维斯特（Ron [R]ivest）、阿迪·萨莫尔（Adi [S]hamir）和伦纳德·阿德曼（Leonard [A]dleman）
 * </p>
 * <p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 * </p>
 *
 * @author IceWee
 * @date 2012-4-26
 * @version 1.0
 */

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    private static final String AGENCYFEE_PUBLIC_KEY = "/pem/agencyfee_public_key.pem";
    private static final String AGENCYFEE_PRIVATE_KEY = "/pem/agencyfee_private_key.pem";

    private static final String CHAR_ENCODE = "UTF-8";
    private static final String KEY_PAIR_GENERATOR = "RSA";
    private static final String  SIGN_ALGORITHMS = "SHA1WithRSA";
    private static final String CIPHER_PADDING = "RSA/ECB/PKCS1Padding";     //RSA/ECB/PKCS1Padding        NoPadding

    private static final RSAPublicKey agencyfee_pubilc_key = getPublicKey(AGENCYFEE_PUBLIC_KEY);
    private static final RSAPrivateKey agencyfee_private_key = getPrivateKey(AGENCYFEE_PRIVATE_KEY);

    /**
     *  add by mqy
     * @date 2018-12-26
     * 第三方代收费解密接口
     */
    public static String agencyFeeEncrypt(String data) throws Exception{
        return encryptByPublicKey(data, agencyfee_pubilc_key);
    }
    /**
     *  add by mqy
     * @date 2018-12-26
     * 第三方代收费加密接口
     */
    public static String agencyFeeDecrypt(String data) throws Exception {
        return decryptByPrivateKey(data, agencyfee_private_key);
    }

    public static RSAPublicKey getPublicKey(String file) {
        try {
            Resource resource = new ClassPathResource(file);
            String publicKeyStr = loadPublicKeyFromFile(resource.getInputStream());
            return loadPublicKey(publicKeyStr);
        } catch (Exception ee) {
            return null;
        }
    }

    public static RSAPrivateKey getPrivateKey(String file) {
        try {
            Resource resource = new ClassPathResource(file);
            String privateKeyStr = loadPrivateKeyFromFile(resource.getInputStream());
            return loadPrivateKey(privateKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 公钥加密
     * @param data
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String encryptByPublicKey(String data, RSAPublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 模长
        int maxBlockSize = publicKey.getModulus().bitLength() / 8  - 11;
        // 加密数据长度 <= 模长-11, 如果明文长度大于模长-11则要分组加密
        byte[] plantText = data.getBytes(Charset.forName(CHAR_ENCODE));

        int inputLen = plantText.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                out.write(cipher.doFinal(plantText, offSet, maxBlockSize));
            } else {
                out.write(cipher.doFinal(plantText, offSet, inputLen - offSet));
            }
            i++;
            offSet = i * maxBlockSize;
        }
        return Base64Encoder(out.toByteArray());
    }

    /**
     * 私钥解密
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String data, RSAPrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //模长 如果密文长度大于模长则要分组解密
        int maxBlockSize = privateKey.getModulus().bitLength() / 8;
        byte[] cipherText = Base64Decoder(data);

        if (cipherText.length <= maxBlockSize) {
            return new String(cipher.doFinal(cipherText), Charset.forName(CHAR_ENCODE));
        } else {
            ByteArrayInputStream bis = new ByteArrayInputStream(cipherText);
            ByteArrayOutputStream bos = new  ByteArrayOutputStream();
            byte[] dd = new byte[maxBlockSize];
            int blockSize = bis.read(dd);
            while (blockSize > 0) {
                bos.write(cipher.doFinal(dd));
                blockSize = bis.read(dd);
            }
            return new String(bos.toByteArray(), Charset.forName(CHAR_ENCODE));
        }
    }

    /**
     * 从文件中输入流中加载公钥
     * @throws Exception 加载公钥时产生的异常
     */
    private static String loadPublicKeyFromFile(InputStream in) throws Exception{
        try {
            BufferedReader br= new BufferedReader(new InputStreamReader(in));
            String readLine= null;
            StringBuilder sb= new StringBuilder();
            while((readLine= br.readLine())!=null){
                if(readLine.charAt(0)=='-'){
                    continue;
                }else{
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }


    /**
     * 从字符串中加载公钥
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    private static RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception{
        try {
            byte[] buffer= Base64Decoder(publicKeyStr);
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从文件中加载私钥
     * @return 是否成功
     * @throws Exception
     */
    private static String loadPrivateKeyFromFile(InputStream in) throws Exception{
        try {
            BufferedReader br= new BufferedReader(new InputStreamReader(in));
            String readLine= null;
            StringBuilder sb= new StringBuilder();
            while((readLine= br.readLine())!=null){
                if(readLine.charAt(0)=='-'){
                    continue;
                }else{
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }

    private static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception{
        try {
            byte[] buffer= Base64Decoder(privateKeyStr);
            PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory= KeyFactory.getInstance(KEY_PAIR_GENERATOR);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    public static String Base64Encoder(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] Base64Decoder(String data){
        try{
            return (new BASE64Decoder()).decodeBuffer(data);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("公司编码="+RSAUtils.agencyFeeEncrypt("2018"));
        System.out.println("IC表号="+RSAUtils.agencyFeeEncrypt("0300000016000000"));
        System.out.println("表号="+RSAUtils.agencyFeeEncrypt("A01981803050005"));
        System.out.println("meterType="+RSAUtils.agencyFeeEncrypt("W"));
        System.out.println("普表表型="+RSAUtils.agencyFeeEncrypt("C"));
        System.out.println("普表表号="+RSAUtils.agencyFeeEncrypt("A01198010111211"));
        System.out.println("合同号="+RSAUtils.agencyFeeEncrypt("3808476463"));
        System.out.println("普表公司代码="+RSAUtils.agencyFeeEncrypt("1109"));
        System.out.println("ptnCode="+RSAUtils.agencyFeeEncrypt("A"));
        System.out.println("hostname="+RSAUtils.agencyFeeEncrypt("hostname"));
        System.out.println("money="+RSAUtils.agencyFeeEncrypt("999"));
        System.out.println("支付类型="+RSAUtils.agencyFeeEncrypt("A"));

        System.out.println("解密明文="+RSAUtils.agencyFeeDecrypt("EdclAQnKJ3Z2CUv+7GOwEkRLA8mQsY3OFDMrPk3vxp7a9LXipX9vnpVuyF1t5Y6zxug3uaOXFRd6mgG5t2GoJ+yz2XoTNlqEN5EfwLj0DzICq8Xw9ahwJpjGsNbZ+/WmVfZefuC5zKAVTWhhxrgK4uXxXrsC1ch+enYtzenkVEo="));
    }
}
