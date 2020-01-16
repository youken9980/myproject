package yk.core.util;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5计算工具
 *
 * @author 杨剑
 * @date 2019/12/24
 */
public class MD5Util {

	private static final String ALGORITHM = "MD5";

	private static MessageDigest messageDigest;
	static {
		try {
			messageDigest = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String getMD5(String filePath) {
		long beginTime = System.currentTimeMillis();
		String md5 = null;
		try (MappedBiggerFileReader reader = new MappedBiggerFileReader(filePath)) {
			int length;
			while ((length = reader.read()) != -1) {
				messageDigest.update(reader.getBuffer(), 0, length);
			}
			md5 = new String(Hex.encodeHex(messageDigest.digest()));
			long endTime = System.currentTimeMillis();
			System.out.println(String.format("MD5: %s, file size: %s, cost time: %sms, file: %s", md5, reader.getFileLength(), (endTime - beginTime), filePath));
			return md5;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		String filePath = "/Users/youken/Destiny/Games/[三国无双]/Dynasty.Warriors.9-CODEX/codex-dynasty.warriors.9.iso";
		MD5Util.getMD5(filePath);
	}
}
