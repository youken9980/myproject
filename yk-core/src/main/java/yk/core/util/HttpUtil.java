package yk.core.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class HttpUtil extends AbstractHttpUtil {

    public HttpUtil() {
        httpClient = HttpClients.custom()
                .setConnectionManager(getConnectionManager())
                .setDefaultRequestConfig(getRequestConfig())
                .build();
    }

    @Override
    protected RequestConfig getRequestConfig() {
        return getRequestConfigBuilder().build();
    }
}
