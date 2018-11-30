package com.hunter.wallet.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by DT0814 on 2018/8/4.
 */

public class HTTPUtils {

    public static String doGetSync(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        return call.execute().body().string();
    }

    public static String doPostSync(String url, FormBody formBody) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(formBody).build();
        Call call = okHttpClient.newCall(request);
        return call.execute().body().string();
    }

    public static <T> T doGetSync1(String url, Class<T> resultType) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        String json = call.execute().body().string();
        return JsonUtils.jsonToPojo(json, resultType);
    }

    public static <T> T doPostSync1(String url, FormBody formBody, Class<T> resultType)
            throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(formBody).build();
        Call call = okHttpClient.newCall(request);
        String json = call.execute().body().string();
        return JsonUtils.jsonToPojo(json, resultType);
    }

    public static <T> T getUtils(String urlStr, Class<T> resultType) {
        URL url = null;
        try {
            url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                if ((line = bf.readLine()) != null) {
                    sb.append(line);
                }
                T t = JsonUtils.jsonToPojo(sb.toString(), resultType);
                return t;
            } else {
                Log.e("getUtils", "查询失败");
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T ETHPriceResult(String urlStr, Class<T> resultType) {
        URL url = null;
        try {
            url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                if ((line = bf.readLine()) != null) {
                    sb.append(line);
                }
                T t = JsonUtils.jsonToPojo(sb.toString().substring(1, sb.length() - 1), resultType);
                return t;
            } else {
                Log.e("getUtils", "查询失败");
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> getList(String urlStr, Class<T> resultType) {
        URL url = null;
        try {
            url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                if ((line = bf.readLine()) != null) {
                    sb.append(line);
                }
                List<T> t = JsonUtils.jsonToList(sb.toString(), resultType);
                return t;
            } else {
                Log.e("getUtils", "查询失败");
                return null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
