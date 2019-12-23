package com.spacex.rule.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    public static String httpGet(String url, Map<String,String> params) {
        String responseStr = null;
        HttpGet get = null;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            URIBuilder uriBuilder = new URIBuilder(url);
            List<NameValuePair> list = new LinkedList<>();
            for(Map.Entry<String, String> a:params.entrySet()){
                NameValuePair pair = new BasicNameValuePair(a.getKey(),a.getValue());
                list.add(pair);
            }
            uriBuilder.addParameters(list);
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
//            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
            get = new HttpGet(uriBuilder.build());
            // 构造消息头
            get.setHeader("Content-type", "application/text; charset=utf-8");
            get.setHeader("Connection", "Close");

            HttpResponse response = httpClient.execute(get);

            // 检验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK){
                System.out.println("请求出错: "+statusCode);
            }else{
                //TODO 处理请求
                responseStr = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(get != null){
                try {
                    get.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseStr;
    }

    public static String httpPostWithJson(String url, String json){
        String responseStr = null;
        HttpPost post = null;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            post.setHeader("Connection", "Close");

            // 构建消息实体
            StringEntity entity = new StringEntity(json, Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);

            // 检验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK){
                System.out.println("请求出错: "+statusCode);
            }else{
                //TODO 处理请求
                responseStr = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(post != null){
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseStr;
    }
}
