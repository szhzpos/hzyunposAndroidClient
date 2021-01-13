package com.wyc.cloudapp.utils.http;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.utils.Utils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpUtils {
    private static OkHttpClient HTTP_CLIENT = null;
    public static OkHttpClient getHttpClient() {
        if (HTTP_CLIENT == null){
            synchronized (HttpUtils.class){
                if (HTTP_CLIENT == null){
                    HTTP_CLIENT = new OkHttpClient();
                }
            }
        }
        return HTTP_CLIENT;
    }

    public static JSONObject sendPost(final String url, @NonNull final String param, boolean json) {//json 请求返回数据类型 true 为json格式 否则为XML
        final JSONObject content = new JSONObject();
        final RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), param);
        final Request request = new Request.Builder().url(url).post(body).build();
        try(Response response = getHttpClient().newCall(request).execute()){
            final ResponseBody responseBody = response.body();
            content.put("flag",1);
            if (responseBody != null){
                if (json){
                    content.put("info",responseBody.string());
                }else {
                    try (Reader reader = new InputStreamReader(responseBody.byteStream(),StandardCharsets.UTF_8)){
                        final Map<String,String> map = Utils.parseXml(reader);
                        content.put("info", JSON.toJSON(map));
                    }
                }
            }else {
                content.put("info","");
            }
        }catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            content.put("flag",0);
            content.put("info",e.getMessage());
        }
        return content;
    }

    public static Call sendAsyncPost(final String url, @NonNull final String param){
        final RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), param);
        final Request request = new Request.Builder().url(url).post(body).build();
        return getHttpClient().newCall(request);
    }
}
