package com.jun.lineyou.utils;

import com.alibaba.fastjson.JSON;
import com.jun.lineyou.exception.HttpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 远程调用工具类
 *
 * @author Jun
 * @date 2018-12-27 18:26
 */
@Slf4j
public class HttpUtils {

    private static final String MEDIA_TYPE = "application/json";

    private static CloseableHttpAsyncClient httpClient;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000).build();
        httpClient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        httpClient.start();
    }


    public static <T> void asyncPost(String uri, T param, FutureCallback<HttpResponse> callback) {
        asyncPost(uri, null, param, callback);
    }

    /**
     * 异步HTTP请求
     *
     * @param uri     接口地址
     * @param headers 请求头
     * @param param   请求参数
     * @return
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public static <T> void asyncPost(String uri, Map<String, String> headers, T param, FutureCallback<HttpResponse> callback) {
        Assert.hasText(uri, "请求uri不能为空");

        final HttpPost httpPost = new HttpPost(uri);

        //请求头处理
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(httpPost::setHeader);
        } else {
            //默认请求类型
            httpPost.setHeader("Content-Type", MEDIA_TYPE);
        }

        //请求参数处理
        if (param instanceof String) {
            NStringEntity nStringEntity = new NStringEntity((String) param, StandardCharsets.UTF_8);

            httpPost.setEntity(nStringEntity);
        } else if (param instanceof Map) {
            Map<String, String> params = (Map<String, String>) param;

            List<NameValuePair> nvps = new ArrayList<>();

            params.forEach((k, v) -> nvps.add(new BasicNameValuePair(k, v)));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        } else if (param == null) {

        } else {
            throw new IllegalArgumentException("不支持的参数类型:" + param);
        }

        httpClient.execute(httpPost, callback);
    }

    /**
     * 异步Get请求
     *
     * @param uri     请求地址
     * @param headers 请求头
     * @param param   请求参数
     * @param callback 回调函数
     */
    public static void asyncGet(String uri, Map<String, String> headers, Map<String, String> param, FutureCallback<HttpResponse> callback) {
        Assert.hasText(uri, "请求uri不能为空");

        //请求体拼接
        if (!CollectionUtils.isEmpty(param)) {
            uri += "?";

            StringBuilder apiBuilder = new StringBuilder(uri);
            for (Map.Entry<String, String> entry : param.entrySet()) {
                try {
                    apiBuilder
                            .append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), "utf8"))
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage(), e);
                }
            }
            uri = apiBuilder.toString();

            uri = uri.substring(0, uri.length() - 1);
        }

        HttpGet httpGet = new HttpGet(uri);

        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(httpGet::setHeader);
        }

        httpClient.execute(httpGet, callback);
    }

    /**
     * http get 方法
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     */
    public static String get(String url, Map<String, Object> params) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            //请求体拼接
            if (!CollectionUtils.isEmpty(params)) {
                url += "?";

                StringBuilder apiBuilder = new StringBuilder(url);
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    apiBuilder
                            .append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode((String) entry.getValue(), "utf-8"))
                            .append("&");
                }
                url = apiBuilder.toString();

                url = url.substring(0, url.length() - 1);
            }

            HttpGet httpGet = new HttpGet(url);

            String result = httpClient.execute(httpGet, setResponseHandler());

            //对结果字符做判断
            if (StringUtils.isEmpty(result)) {
                throw new HttpException("api: " + url + "调用结果为空");
            }

            return result;
        } catch (IOException e) {
            throw new HttpException("api:" + url + "调用失败", e);
        }
    }

    /**
     * 同步POST请求工具
     *
     * @param uri     接口地址
     * @param param   请求参数，支持 post_raw, post_form
     * @param headers 请求头，默认添加 <code>Header:"Content-Type : application/json:charset=utf8"</code>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T post(String uri, Map<String, String> headers, Object param, Class<T> clazz) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(uri);

            //http头
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach(httpPost::setHeader);
            } else {
                //默认请求类型
                httpPost.setHeader("Content-Type", MEDIA_TYPE);
            }

            /*
                参数处理
             */
            if (param instanceof String) {
                StringEntity stringEntity = new StringEntity((String) param, StandardCharsets.UTF_8);

                // 置入参数
                httpPost.setEntity(stringEntity);
            } else if (param instanceof Map) {
                Map<String, Object> params = (Map<String, Object>) param;

                List<NameValuePair> nvps = new ArrayList<>();

                params.forEach((k, v) -> nvps.add(new BasicNameValuePair(k, (String) v)));

                // 置入参数
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf8"));
            } else {
                throw new HttpException("方法参数仅支持 String 和 Map<String,Object> 类型");
            }

            // 获得结果
            String result = httpClient.execute(httpPost, setResponseHandler());

            //对结果字符做判断
            if (StringUtils.isEmpty(result)) {
                throw new HttpException("uri: " + uri + "调用结果为空");
            }

            if (String.class.isAssignableFrom(clazz)) {
                return (T) result;
            }

            return JSON.parseObject(result, clazz);
        } catch (IOException e) {
            throw new HttpException("uri:" + uri + "调用失败", e);
        }
    }

    /**
     * 定制响应处理
     *
     * @return
     */
    private static ResponseHandler<String> setResponseHandler() {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("http错误响应状态: " + status);
            }
        };
    }
}
