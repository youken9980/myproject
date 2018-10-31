package yk.core.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class ZipUtil {

    public static final String EXT_ZIP = ".zip";
    public static final String EXT_RAR = ".rar";

    private static final String DEFAULT_FILE_NAME = "pack";

    public static String zip(String src, String password) throws Exception {
        try {
            List<String> list = new ArrayList<String>();
            list.add(src);
            return zip(list, null, password);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String zip(List<String> srcFilePaths, String destFilePath, String password) throws Exception {
        try {
            checkSrcFileExist(srcFilePaths);
            String dest = buildDestZipFilePath(srcFilePaths, destFilePath, EXT_RAR);
            ensureFilePath(dest);
            ZipParameters params = buildZipParameters(password);
            File srcFile;
            ZipFile zipFile = new ZipFile(dest);
            for (String srcFilePath : srcFilePaths) {
                srcFile = new File(srcFilePath);
                if (srcFile.isDirectory()) {
                    zipFile.addFolder(srcFile, params);
                } else {
                    zipFile.addFile(srcFile, params);
                }
            }
            return dest;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void checkSrcFileExist(List<String> srcFilePaths) throws FileNotFoundException {
        File file;
        for (String filePath : srcFilePaths) {
            if (filePath == null || filePath.trim().length() < 1) {
                throw new FileNotFoundException(filePath);
            } else {
                file = new File(filePath);
                if (!file.exists()) {
                    throw new FileNotFoundException(filePath);
                }
            }
        }
    }

    private static String buildDestZipFilePath(List<String> srcFilePaths, String destFilePath, String ext) {
        String filePath;
        boolean isDestFilePathNull = destFilePath == null || destFilePath.trim().length() < 1;
        if (srcFilePaths.size() > 1) {
            if (isDestFilePathNull) {
                throw new RuntimeException("Argument destFilePath  must not be null.");
            }
            filePath = getDestFilePath(destFilePath, DEFAULT_FILE_NAME, ext);
        } else {
            File file = new File(srcFilePaths.get(0));
            if (isDestFilePathNull) {
                filePath = file.getAbsolutePath() + ext;
            } else {
                filePath = getDestFilePath(destFilePath, file.getName(), ext);
            }
        }
        return filePath;
    }

    private static String getDestFilePath(String destFilePath, String fileName, String ext) {
        String filePath;
        if (destFilePath.endsWith(File.separator)) {
            filePath = destFilePath + fileName + ext;
        } else {
            filePath = destFilePath;
        }
        return filePath;
    }

    private static void ensureFilePath(String filePath) {
        String parent;
        if (filePath.endsWith(File.separator)) {
            parent = filePath;
        } else {
            parent = filePath.substring(0, filePath.lastIndexOf(File.separator));
        }
        File file = new File(parent);
        if (file.exists()) {
            // parent exists and file exists, then delete file
            file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } else {
            // parent does not exists, then make it
            file.mkdirs();
        }
    }

    private static ZipParameters buildZipParameters(String password) {
        ZipParameters params = new ZipParameters();
        params.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (password != null && password.length() > 0) {
            params.setEncryptFiles(true);
            params.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            params.setPassword(password);
        }
        return params;
    }
}
