package com.wyc.cloudapp.utils.http;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class HttpUtils {
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
        return sendPost(getHttpClient(),url,param,json);
    }

    //connectTime 单位秒
    public static JSONObject sendPost(final String url, @NonNull final String param,int connectTime,boolean json) {
        return sendPost(createClient(connectTime * 1000,0,0,false),url,param,json);
    }

    private static JSONObject sendPost(final OkHttpClient okHttpClient,final String url, @NonNull final String param, boolean json) {//json 请求返回数据类型 true 为json格式 否则为XML
        final JSONObject content = new JSONObject();
        try(Response response = okHttpClient.newCall(createRequestForForm(url,param)).execute()){
            int code = response.code();
            if (code != HttpURLConnection.HTTP_OK){
                content.put("flag",0);
                content.put("info",response.message());
            }else {
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
            }
            content.put("rsCode",code);
        }catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            if (e instanceof IOException)
                content.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);

            content.put("flag",0);
            content.put("info",e.getMessage());
        }
        return content;
    }

    public static Call sendAsyncPost(final String url, @NonNull final String param){
        return getHttpClient().newCall(createRequestForForm(url,param));
    }
    public static Call sendAsyncGet(final String url){
        final Request request = new Request.Builder().url(url).build();
        return getHttpClient().newCall(request);
    }
    private static Request createRequestForForm(final String url, @NonNull final String param){
        final RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), param);
        return new Request.Builder().url(url).post(body).build();
    }

    private static OkHttpClient createClient(long connectTimeout,long writeTimeout,long readTimeout, boolean bRetry){
        final OkHttpClient.Builder builder = getHttpClient().newBuilder();
        if (connectTimeout > 0){
            builder.dns(new TimeOutDns(connectTimeout));
            builder.connectTimeout(connectTimeout,TimeUnit.MILLISECONDS);
        }
        if (writeTimeout > 0)builder.writeTimeout(writeTimeout,TimeUnit.MILLISECONDS);
        if (readTimeout > 0)builder.readTimeout(writeTimeout,TimeUnit.MILLISECONDS);
        builder.retryOnConnectionFailure(bRetry);
        return builder.build();
    }

    private static class TimeOutDns implements Dns{
        private final long mTimeOut;
        public TimeOutDns(long timeout) {
            this.mTimeOut = timeout;
        }
        @Override
        public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
            try {
                final FutureTask<List<InetAddress>> task = new FutureTask<>(() -> Arrays.asList(InetAddress.getAllByName(hostname)));
                new Thread(task).start();
                return task.get(mTimeOut, TimeUnit.MILLISECONDS);
            } catch (Exception var4) {
                final UnknownHostException unknownHostException = new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
                unknownHostException.initCause(var4);
                throw unknownHostException;
            }
        }
    }

    public static boolean checkRequestSuccess(final JSONObject object){
        if (null == object)return false;
        boolean code = object.getIntValue("flag") == 1;
        if (!code){
            MyDialog.toastMessage(object.getString("info"));
        }
        return code;
    }
    public static boolean checkBusinessSuccess(final JSONObject object){
        return null != object && "y".equals(object.getString("status"));
    }
}
