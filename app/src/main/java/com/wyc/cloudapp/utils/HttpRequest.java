package com.wyc.cloudapp.utils;

import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.Logger;

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

	public HttpRequest() {///构造函数

	}
/*	public static void close(){
        connection.disconnect();//可以断开连接 抛出 Socket closed 异常
    }

	private static HttpURLConnection connection = null;*/

    public static JSONObject sendGet(String url) {
        String result = "",line = null;
        BufferedReader in = null;
        JSONObject jsonRetStr = new JSONObject();

        Logger.d(url);

        try {

            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestMethod("GET");
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            while ((line = in.readLine()) != null) {
                result += line;
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
            } catch (Exception e2) {
                e2.printStackTrace();
            }
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

    public static JSONObject sendPost(String url, Object param,String hmac_sha1,boolean json) {
        BufferedReader in = null;
        BufferedWriter out = null;
        InputStreamReader reader = null;
        String line = "";
        StringBuilder resultJson = new StringBuilder();
        JSONObject jsonRetStr = new JSONObject();
        try {
            URL realUrl = new URL(url);

            HttpURLConnection conn =(HttpURLConnection)realUrl.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");///  application/soap+xml; charset=utf-8
            conn.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("accept", "*/*");
            if(hmac_sha1 != null)conn.setRequestProperty("Content-MD5", hmac_sha1);
            conn.setAllowUserInteraction(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.connect();///如果没有打开连接则在getOutputStream()打开  

            if (param != null){
                out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),StandardCharsets.UTF_8));
                out.write(param.toString());
                out.flush();
            }
            int resCode = conn.getResponseCode();
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
        } catch (IOException | XmlPullParserException | JSONException e) {
            try {
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info", e.toString());
                e.printStackTrace();
            }catch ( JSONException je ){
                e.printStackTrace();
            }
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
                if (reader != null)
                    reader.close();
            }catch(IOException ex){
                out = null;
                reader = null;
                in = null;
                ex.printStackTrace();
            }
        }
        return jsonRetStr;
    }


    public static JSONObject sendPost_hz(String url, Object param,String hmac_sha1,boolean json) {
        BufferedReader in = null;
        BufferedWriter out = null;
        InputStreamReader reader = null;
        InputStream inputStream = null;
        String line = "";
        StringBuilder result = new StringBuilder();
        StringBuilder resultJson = new StringBuilder();
        JSONObject jsonRetStr = new JSONObject();

        Logger.d("requestParam:%s",param);

        try {
            URL realUrl = new URL(url);

            HttpURLConnection conn =(HttpURLConnection)realUrl.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");///  application/soap+xml; charset=utf-8
            conn.setRequestProperty("user-agent","Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("accept", "*/*");
            if(hmac_sha1 != null)conn.setRequestProperty("Content-MD5", hmac_sha1);
            conn.setAllowUserInteraction(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.connect();///如果没有打开连接则在getOutputStream()打开

            if (param != null){
                out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),StandardCharsets.UTF_8));
                out.write(param.toString());
                out.flush();
            }
            int resCode = conn.getResponseCode();
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
        } catch (IOException | XmlPullParserException | JSONException e) {
            try {
                jsonRetStr.put("flag", 0);
                jsonRetStr.put("info", e.toString());
                e.printStackTrace();
            }catch ( JSONException je ){
                e.printStackTrace();
            }
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
                if (reader != null)
                    reader.close();
            }catch(IOException ex){
                out = null;
                reader = null;
                ex.printStackTrace();
            }
        }
        return jsonRetStr;
    }
}