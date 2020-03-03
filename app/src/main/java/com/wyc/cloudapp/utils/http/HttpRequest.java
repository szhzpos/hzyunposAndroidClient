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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

public final class HttpRequest {
    private static final ThreadLocal<HttpURLConnection> THREAD_LOCAL = new ThreadLocal<>();
	public static void clearConnection(){
        HttpURLConnection conn = THREAD_LOCAL.get();
        if (conn != null){
            conn.disconnect();
            THREAD_LOCAL.remove();
        }
    }
    public static JSONObject sendGet(final String url) {
        String line;
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        JSONObject jsonRetStr = new JSONObject();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            THREAD_LOCAL.set(conn);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("GET");
            conn.connect();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8));
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            jsonRetStr.put("flag", 1);
            jsonRetStr.put("info", result);
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
            clearConnection();
        }
        return jsonRetStr;
    }


	public static JSONObject getFile( String store_filePath,String url){
        JSONObject jsonRetStr = new JSONObject();

        byte[] buffer = new byte[1024];
        int lenght = 0;

        try {
            URL realUrl = new URL(url);
            HttpURLConnection connection =(HttpURLConnection)realUrl.openConnection();
            THREAD_LOCAL.set(connection);
            connection.setRequestProperty("Content-Type", "application/vnd.android.package-archive");///  application/soap+xml; charset=utf-8
            connection.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestMethod("GET");
            connection.connect();
            try ( BufferedInputStream in = new BufferedInputStream(connection.getInputStream()); FileOutputStream  fileOutputStream = new FileOutputStream(new File(store_filePath));){
                while ((lenght = in.read(buffer)) != -1){
                    fileOutputStream.write(buffer,0,lenght);
                }
            }
        }catch (IOException e){
            try {
                jsonRetStr.put("flag",0);
                jsonRetStr.put("info","下载失败！ " + e.getLocalizedMessage());
            }catch (JSONException je ){
                Logger.e("下载文件JSON异常：%s",je.getLocalizedMessage());
            }
        }
	    return jsonRetStr;
    }

    public static JSONObject sendPost(final String url,@NonNull final String param,boolean json) {//json 请求返回数据类型 true 为json格式 否则为XML
        BufferedReader in = null;
        BufferedWriter out = null;
        InputStreamReader reader = null;
        String line;
        StringBuilder resultJson = new StringBuilder();
        JSONObject jsonRetStr = new JSONObject();
        int resCode;
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            THREAD_LOCAL.set(conn);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");///  application/soap+xml; charset=utf-8
            conn.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("accept", "*/*");
            conn.setAllowUserInteraction(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.connect();///如果没有打开连接则在getOutputStream()打开

            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),StandardCharsets.UTF_8));
            out.write(param);
            out.flush();
            resCode = conn.getResponseCode();

            if(resCode != HttpURLConnection.HTTP_OK){
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info","httpCode:" + resCode);
            }else{
                reader = new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8);
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
            jsonRetStr.put("rsCode", resCode);
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
            clearConnection();
        }
        return jsonRetStr;
    }
    public static JSONObject sendPost(final String url,@NonNull final String param,boolean json,HttpURLConnection outConn) {//json 请求返回数据类型 true 为json格式 否则为XML
        BufferedReader in = null;
        BufferedWriter out = null;
        InputStreamReader reader = null;
        String line;
        StringBuilder resultJson = new StringBuilder();
        JSONObject jsonRetStr = new JSONObject();
        int resCode;
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            outConn = conn;
            THREAD_LOCAL.set(conn);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");///  application/soap+xml; charset=utf-8
            conn.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("accept", "*/*");
            conn.setAllowUserInteraction(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.connect();///如果没有打开连接则在getOutputStream()打开

            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),StandardCharsets.UTF_8));
            out.write(param);
            out.flush();
            resCode = conn.getResponseCode();

            if(resCode != HttpURLConnection.HTTP_OK){
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info","httpCode:" + resCode);
            }else{
                reader = new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8);
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
            jsonRetStr.put("rsCode", resCode);
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
            clearConnection();
        }
        return jsonRetStr;
    }
}