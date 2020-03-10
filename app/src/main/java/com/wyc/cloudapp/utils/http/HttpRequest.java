package com.wyc.cloudapp.utils.http;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

public final class HttpRequest {
    private volatile HttpURLConnection mPostConn,mGetConn;
    private int mConnTimeOut = 30000,mReadTimeOut = 30000;
    private int mPostCode = HttpURLConnection.HTTP_BAD_REQUEST,mGetCode = HttpURLConnection.HTTP_BAD_REQUEST;
    public enum CLOSEMODE{
        GET,POST,BOTH
    }
	public void clearConnection(CLOSEMODE mode){
	    switch (mode){
            case GET:
                if (mGetConn != null){
                    mGetConn.disconnect();
                }
                break;
            case POST:
                if (mPostConn != null){
                    mPostConn.disconnect();
                }
                break;
            case BOTH:
                if (mGetConn != null){
                    mGetConn.disconnect();
                }
                if (mPostConn != null){
                    mPostConn.disconnect();
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
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        JSONObject jsonRetStr = new JSONObject();
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
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info",mGetConn.getResponseMessage());
            }else {
                in = new BufferedReader(new InputStreamReader(mGetConn.getInputStream(),StandardCharsets.UTF_8));
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                jsonRetStr.put("flag", 1);
                jsonRetStr.put("info", result);
            }
            jsonRetStr.put("rsCode", mGetCode);
        } catch (IOException | JSONException  e) {
            try {
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info", e.toString());
            }catch (JSONException  je){
                je.printStackTrace();
            }
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
        return jsonRetStr;
    }
	public synchronized JSONObject getFile( Object store_file,String url){
        JSONObject jsonRetStr = new JSONObject();
        File download_file = null;
        byte[] buffer = new byte[1024];
        int lenght = 0;

        try {
            URL url_obj = new URL(url);
            mGetConn =(HttpURLConnection)url_obj.openConnection();
            mGetConn.setRequestProperty("Content-Type", "application/vnd.android.package-archive");
            mGetConn.setUseCaches(false);
            mGetConn.setConnectTimeout(mConnTimeOut);
            mGetConn.setReadTimeout(mReadTimeOut);
            mGetConn.setRequestMethod("GET");
            mGetConn.connect();
            mGetCode = mGetConn.getResponseCode();
            if(mGetCode != HttpURLConnection.HTTP_OK){
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info",mGetConn.getResponseMessage());
            }else{
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
            jsonRetStr.put("flag",1);
            jsonRetStr.put("rsCode", mGetCode);
        }catch (IOException | JSONException e){
            try {
                jsonRetStr.put("flag",0);
                jsonRetStr.put("info","httpCode:" + mGetCode);
                jsonRetStr.put("info","下载失败！ " + e.getLocalizedMessage());
            }catch (JSONException je ){
                je.printStackTrace();
                Logger.e("下载文件JSON异常：%s",je.getLocalizedMessage());
            }
        }finally {
            clearConnection(CLOSEMODE.GET);
        }
	    return jsonRetStr;
    }
    public synchronized JSONObject sendPost(final String url,@NonNull final String param,boolean json) {//json 请求返回数据类型 true 为json格式 否则为XML
        BufferedReader in = null;
        BufferedWriter out = null;
        InputStreamReader reader = null;
        String line;
        StringBuilder resultJson = new StringBuilder();
        JSONObject jsonRetStr = new JSONObject();
        try {
            URL url_obj = new URL(url);
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

            out = new BufferedWriter(new OutputStreamWriter(mPostConn.getOutputStream(),StandardCharsets.UTF_8));
            out.write(param);
            out.flush();

            mPostCode = mPostConn.getResponseCode();

            if(mPostCode != HttpURLConnection.HTTP_OK){
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info",mPostConn.getResponseMessage());
            }else{
                reader = new InputStreamReader(mPostConn.getInputStream(),StandardCharsets.UTF_8);
                if (json){
                    in = new BufferedReader(reader);
                    while ((line = in.readLine()) != null) {
                        resultJson.append(line);
                    }
                    jsonRetStr.put("flag", 1);
                    jsonRetStr.put("info", resultJson);
                }else {
                    Map<String,String> map = Utils.parseXml(reader);
                    jsonRetStr.put("flag", 1);
                    jsonRetStr.put("info", new JSONObject(map));
                }
            }
            jsonRetStr.put("rsCode", mPostCode);
        } catch (IOException | XmlPullParserException | JSONException e) {
            try {
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("rsCode", HttpURLConnection.HTTP_BAD_REQUEST);
                jsonRetStr.put("info", e.toString());
                e.printStackTrace();
            }catch ( JSONException je ){
                e.printStackTrace();
            }
        }
        finally{
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
                if (reader != null)
                    reader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
            clearConnection(CLOSEMODE.POST);
        }
        return jsonRetStr;
    }

    public HttpRequest setConnTimeOut(int connTimeOut) {
        this.mConnTimeOut = connTimeOut;
        return this;
    }
    public HttpRequest setReadTimeOut(int readTimeOut){
        this.mReadTimeOut = readTimeOut;
        return this;
    }

    public  static String generate_request_parm(JSONObject json ,String apiKey) throws JSONException {
        Map<String,String> map = new HashMap<>();
        Map<String, String> sortMap = new TreeMap<String, String>();
        StringBuilder builder = new StringBuilder();
        String signStr = null;

        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            map.put(key, json.getString(key));
        }
        sortMap.putAll(map);
        for (Map.Entry<String, String> s : sortMap.entrySet()) {
            String k = s.getKey();
            String v = s.getValue();
            if (builder.length() != 0){
                builder.append("&");
            }
            builder.append(k).append("=").append(v);
        }
        signStr = builder + apiKey;

        return builder.append("&sign=").append(Utils.getMD5(signStr.getBytes())).toString();
    }
}