package yk.core.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
abstract public class AbstractHttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final int TIMEOUT = 1000 * 60 * 3;
    private static final int BUFFER_SIZE = 1024;
    private static final int STATUS_CODE_OK = 200;

    CloseableHttpClient httpClient;

    public String doGet(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(get);
        return parse(response);
    }

    public String doPost(String url, Map<String, String> params) throws IOException {
        HttpPost post = new HttpPost(url);
        if (params != null && params.size() > 0) {
            List<NameValuePair> liPairs = convertNameValuePair(params);
            post.setEntity(new UrlEncodedFormEntity(liPairs, CHARSET_UTF_8));
        }
        CloseableHttpResponse response = httpClient.execute(post);
        return parse(response);
    }

    private List<NameValuePair> convertNameValuePair(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        List<NameValuePair> liPairs = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            liPairs.add(new BasicNameValuePair(key, params.get(key)));
        }
        return liPairs;
    }

    private String parse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String encoding = getEncoding(entity);
        String content = getContent(entity, encoding);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != STATUS_CODE_OK) {
            LOGGER.debug(content);
            throw new RuntimeException("Response code is: " + statusCode);
        }
        return content;
    }

    private String getEncoding(HttpEntity entity) {
        String encoding;
        Header header = entity.getContentEncoding();
        if (header != null) {
            encoding = header.getValue();
            if (encoding == null || encoding.trim().length() < 1) {
                encoding = CHARSET_UTF_8;
            }
        } else {
            encoding = CHARSET_UTF_8;
        }
        return encoding;
    }

    private String getContent(HttpEntity entity, String charset) throws IOException {
        InputStream is = null;
        InputStreamReader isr = null;
        try {
            is = entity.getContent();
            if (is == null) {
                return null;
            }
            StringBuilder stb = new StringBuilder();
            char[] buff = new char[BUFFER_SIZE];
            isr = new InputStreamReader(is, charset);
            int len;
            while ((len = isr.read(buff)) != -1) {
                stb.append(buff, 0, len);
            }
            return stb.toString();
        } finally {
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    }

    protected Registry<ConnectionSocketFactory> getRegistry() {
        return null;
    }

    PoolingHttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager manager;
        Registry<ConnectionSocketFactory> registry = getRegistry();
        if (registry == null) {
            manager = new PoolingHttpClientConnectionManager();
        } else {
            manager = new PoolingHttpClientConnectionManager(registry);
        }
        manager.setDefaultConnectionConfig(getConnectionConfig());
        manager.setDefaultMaxPerRoute(5);
        manager.setMaxTotal(10);
        return manager;
    }

    private ConnectionConfig getConnectionConfig() {
        return ConnectionConfig.custom()
                .setCharset(Charset.forName(CHARSET_UTF_8))
                .build();
    }

    RequestConfig.Builder getRequestConfigBuilder() {
        return RequestConfig.custom()
//                .setProxy(getProxy())
                .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                .setSocketTimeout(TIMEOUT)
                .setConnectTimeout(TIMEOUT);
    }

    private HttpHost getProxy() {
        return new HttpHost("10.16.0.223", 8080, "http");
    }

    abstract protected RequestConfig getRequestConfig();
}
