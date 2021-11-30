package com.wyc.cloudapp.utils.http;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.xmlpull.v1.XmlPullParserException;

public final class HttpRequest {
    private volatile HttpURLConnection mPostConn,mGetConn;
    private int mConnTimeOut = 30000,mReadTimeOut = 30000;
    private int mPostCode = HttpURLConnection.HTTP_BAD_REQUEST,mGetCode = HttpURLConnection.HTTP_BAD_REQUEST;
    private OnRequestListener mListener;

    public interface OnRequestListener{
        void onSize(long size);
    }
    public void setRequestListener(OnRequestListener listener){
        mListener = listener;
    }
    public enum CLOSEMODE{
        GET,POST,BOTH
    }
	public void clearConnection(CLOSEMODE mode){
	    switch (mode){
            case GET:
                if (mGetConn != null){
                    mGetConn.disconnect();
                    mGetConn = null;
                }
                break;
            case POST:
                if (mPostConn != null){
                    mPostConn.disconnect();
                    mPostConn = null;
                }
                break;
            case BOTH:
                if (mGetConn != null){
                    mGetConn.disconnect();
                    mGetConn = null;
                }
                if (mPostConn != null){
                    mPostConn.disconnect();
                    mPostConn = null;
                }
                break;
        }
    }
    public int getHttpCode(CLOSEMODE mode){
        int code = HttpURLConnection.HTTP_BAD_REQUEST;
        switch (mode){
            case GET:
                code = mGetCode;
                break;
            case POST:
                code = mPostCode;
                break;
        }
        return code;
    }
    public synchronized JSONObject sendGet(final String url) {
        String line;
        final StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        final JSONObject content = new JSONObject();
        try {
            URL url_obj = new URL(url);
            mGetConn = (HttpURLConnection)url_obj.openConnection();
            mGetConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            mGetConn.setUseCaches(false);
            mGetConn.setConnectTimeout(mConnTimeOut);
            mGetConn.setReadTimeout(mReadTimeOut);
            mGetConn.setRequestMethod("GET");
            mGetConn.connect();

            mGetCode = mGetConn.getResponseCode();
            if(mGetCode != HttpURLConnection.HTTP_OK){
                content.put("flag", 0);
                content.put("info",mGetConn.getResponseMessage());
                InputStream inputStream = mGetConn.getErrorStream();
                if (inputStream != null)inputStream.close();
            }else {
                in = new BufferedReader(new InputStreamReader(mGetConn.getInputStream(),StandardCharsets.UTF_8));
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                content.put("flag", 1);
                content.put("info",result.toString());
            }
            content.put("rsCode", mGetCode);
        } catch (IOException | JSONException e) {
            content.put("flag", 0);
            content.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);
            content.put("info", e.toString());
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            clearConnection(CLOSEMODE.GET);
        }
        return content;
    }
	public synchronized JSONObject getFile( Object store_file,String url){
        final JSONObject content = new JSONObject();
        File download_file = null;
        final byte[] buffer = new byte[1024];
        int lenght = 0;

        try {
            final URL url_obj = new URL(url);
            mGetConn =(HttpURLConnection)url_obj.openConnection();
            //mGetConn.setRequestProperty("Content-Type", "application/vnd.android.package-archive");
            mGetConn.setUseCaches(false);
            mGetConn.setConnectTimeout(mConnTimeOut);
            mGetConn.setReadTimeout(mReadTimeOut);
            mGetConn.setRequestMethod("GET");
            mGetConn.connect();
            mGetCode = mGetConn.getResponseCode();
            if(mGetCode != HttpURLConnection.HTTP_OK){
                content.put("flag", 0);
                content.put("info",mGetConn.getResponseMessage());
                InputStream inputStream = mGetConn.getErrorStream();
                if (inputStream != null)inputStream.close();
            }else{
                content.put("flag",1);
                if (store_file instanceof File){//可能产生效率问题，后续跟进优化
                    download_file = (File) store_file;
                }else {
                    download_file = new File(store_file.toString());
                }
                try ( BufferedInputStream in = new BufferedInputStream(mGetConn.getInputStream()); FileOutputStream  fileOutputStream = new FileOutputStream(download_file);){
                    while ((lenght = in.read(buffer)) != -1){
                        fileOutputStream.write(buffer,0,lenght);
                    }
                }
            }
            content.put("rsCode", mGetCode);
        }catch (IOException | JSONException e){
            content.put("flag",0);
            content.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);
            content.put("info","下载失败！ " + e.getLocalizedMessage());
        }finally {
            clearConnection(CLOSEMODE.GET);
        }
	    return content;
    }
    public synchronized JSONObject getFileForPost(final String url,@NonNull final String param,Object store_file){
        final JSONObject content = new JSONObject();
        File download_file = null;
        final byte[] buffer = new byte[1024];
        int lenght = 0,file_size = 0;

        try {
            final URL url_obj = new URL(url);
            mPostConn =(HttpURLConnection)url_obj.openConnection();
            //mGetConn.setRequestProperty("Content-Type", "application/vnd.android.package-archive");
            mPostConn.setUseCaches(false);
            mPostConn.setConnectTimeout(mConnTimeOut);
            mPostConn.setReadTimeout(mReadTimeOut);
            mPostConn.setRequestMethod("POST");
            mPostConn.connect();

            try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(mPostConn.getOutputStream(),StandardCharsets.UTF_8));) {
                out.write(param);
                out.flush();
            }
            mPostCode = mPostConn.getResponseCode();
            if(mPostCode != HttpURLConnection.HTTP_OK){
                content.put("flag", 0);
                content.put("info",mPostConn.getResponseMessage());
                InputStream inputStream = mPostConn.getErrorStream();
                if (inputStream != null)inputStream.close();
            }else{
                content.put("flag",1);
                if (store_file instanceof File){//可能产生效率问题，后续跟进优化
                    download_file = (File) store_file;
                }else {
                    download_file = new File(store_file.toString());
                }
                try ( BufferedInputStream in = new BufferedInputStream(mPostConn.getInputStream()); FileOutputStream  fileOutputStream = new FileOutputStream(download_file);){
                    while ((lenght = in.read(buffer)) != -1){
                        fileOutputStream.write(buffer,0,lenght);
                        if (mListener != null)mListener.onSize(file_size += lenght);
                    }
                }
            }
            content.put("rsCode", mPostCode);
        }catch (IOException | JSONException e){
            content.put("flag",0);
            content.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);
            content.put("info","下载失败！ " + e.getLocalizedMessage());
        }finally {
            clearConnection(CLOSEMODE.POST);
        }
        return content;
    }

    public synchronized JSONObject uploadFileForPost(final String url,File store_file){
        final JSONObject content = new JSONObject();
        final byte[] buffer = new byte[1024];
        int lenght = 0,file_size = 0;
        BufferedReader in = null;
        String line;
        final StringBuilder result = new StringBuilder();

        try {
            final URL url_obj = new URL(url);
            mPostConn =(HttpURLConnection)url_obj.openConnection();
            mPostConn.setUseCaches(false);
            mPostConn.setConnectTimeout(mConnTimeOut);
            mPostConn.setReadTimeout(mReadTimeOut);
            mPostConn.setRequestMethod("POST");
            mPostConn.connect();

            try (BufferedOutputStream o = new BufferedOutputStream(mPostConn.getOutputStream()); FileInputStream fileOutputStream = new FileInputStream(store_file)){
                while ((lenght = fileOutputStream.read(buffer)) != -1){
                    o.write(buffer,0,lenght);
                    if (mListener != null)mListener.onSize(file_size += lenght);
                }
            }
            try (InputStream inputStream = mPostConn.getInputStream()){
                mPostCode = mPostConn.getResponseCode();
                if(mPostCode != HttpURLConnection.HTTP_OK){
                    content.put("flag", 0);
                    content.put("info",mPostConn.getResponseMessage());
                    InputStream errorStream = mPostConn.getErrorStream();
                    if (errorStream != null)errorStream.close();
                }else{
                    InputStreamReader reader = new InputStreamReader(inputStream,StandardCharsets.UTF_8);
                    in = new BufferedReader(reader);
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    content.put("flag", 1);
                    content.put("info",Utils.unicode2StringWithStringBuilder(result).toString());
                }
                content.put("rsCode", mPostCode);
            }
        }catch (IOException | JSONException e){
            content.put("flag",0);
            content.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);
            content.put("info","上传错误：" + e.getLocalizedMessage());
        }finally {
            clearConnection(CLOSEMODE.POST);
        }
        return content;
    }

    public synchronized JSONObject sendPost(final String url,@NonNull final String param,boolean json) {//json 请求返回数据类型 true 为json格式 否则为XML
        OutputStream out = null;
        InputStreamReader reader = null;
        final StringBuilder result = new StringBuilder(1024 * 4);
        final JSONObject content = new JSONObject();
        try {
            final URL url_obj = new URL(url);
            mPostConn = (HttpURLConnection)url_obj.openConnection();
            mPostConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            mPostConn.setAllowUserInteraction(true);
            mPostConn.setUseCaches(false);
            mPostConn.setConnectTimeout(mConnTimeOut);
            mPostConn.setReadTimeout(mReadTimeOut);
            mPostConn.setRequestMethod("POST");
            mPostConn.setDoOutput(true);
            mPostConn.setDoInput(true);
            mPostConn.connect();///如果没有打开连接则在getOutputStream()打开

            out = mPostConn.getOutputStream();
            out.write(param.getBytes(StandardCharsets.UTF_8));

            mPostCode = mPostConn.getResponseCode();

            if(mPostCode != HttpURLConnection.HTTP_OK){
                content.put("flag", 0);
                content.put("info",mPostConn.getResponseMessage());
                InputStream inputStream = mPostConn.getErrorStream();
                if (inputStream != null)inputStream.close();
            }else{
                reader = new InputStreamReader(mPostConn.getInputStream(),StandardCharsets.UTF_8);
                if (json){
                    final char[] chs = new char[1024];
                    int ch;
                    while ((ch = reader.read(chs)) != -1){
                        result.append(chs,0,ch);
                    }
                    content.put("flag", 1);
                    content.put("info",result.toString());
                }else {
                    Map<String,String> map = Utils.parseXml(reader);
                    content.put("flag", 1);
                    content.put("info", JSON.toJSON(map));
                }
            }
            content.put("rsCode", mPostCode);
        } catch (IOException | XmlPullParserException | JSONException e) {
            content.put("flag", 0);
            if (e instanceof IOException)
                content.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);
            content.put("info", e.toString());
            e.printStackTrace();
        }
        finally{
            try{
                if(out != null){
                    out.close();
                }
                if (reader != null)
                    reader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            clearConnection(CLOSEMODE.POST);
        }
        return content;
    }

    public HttpRequest setConnTimeOut(int connTimeOut) {
        this.mConnTimeOut = connTimeOut;
        return this;
    }
    public HttpRequest setReadTimeOut(int readTimeOut){
        this.mReadTimeOut = readTimeOut;
        return this;
    }

    public  static String generate_request_parma(JSONObject json , String apiKey){
        final Map<String,String> map = new HashMap<>(),sortMap;
        final StringBuilder builder = new StringBuilder();
        String signStr = null,k,v;
        for (final String key : json.keySet()) {
            map.put(key, json.getString(key));
        }
        sortMap = new TreeMap<>(map);
        for (Map.Entry<String, String> s : sortMap.entrySet()) {
            k = s.getKey();
            v = s.getValue();
            if (builder.length() != 0){
                builder.append("&");
            }
            builder.append(k).append("=").append(v);
        }
        signStr = builder + apiKey;
        return builder.append("&sign=").append(Utils.getMD5(signStr.getBytes())).toString();
    }
}