package yk.core.util;

import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class NetUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);
    private static final int BUFFER_SIZE = 1024 * 128;

    private static final String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)";
    private static final int TIMEOUT = 1000 * 60 * 3;
    private static final boolean USE_PROXY = false;
    private static final String PROXY_HOST = "10.16.0.223";
    private static final int PROXY_PORT = 8080;

    public static Document wget(String url) {
        Connection conn;
        try {
            conn = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT);
            if (USE_PROXY) {
                conn = conn.proxy(PROXY_HOST, PROXY_PORT);
            }
            return conn.get();
        } catch (IOException e) {
            LOGGER.error("{}: {}", e.getMessage(), url, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void downloadImage(String imgUrl, String localFile) {
        URLConnection conn;
        InputStream is = null;
        BufferedInputStream bis = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        File file;
        File parent;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            conn = new URL(imgUrl).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(TIMEOUT);
            is = conn.getInputStream();
            bis = new BufferedInputStream(is);
            file = new File(localFile);
            parent = file.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new RuntimeException("mkdirs failure: " + localFile);
                }
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, BUFFER_SIZE);
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            LOGGER.debug("{} --> {}", imgUrl, localFile);
        } catch (IOException e) {
            LOGGER.error("{}: {} --> {}", e.getMessage(), imgUrl, localFile, e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }
    }
}
