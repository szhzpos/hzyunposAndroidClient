package com.wyc.cloudapp.utils.http;

import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponse {
    private HttpURLConnection mHttpConn;
    private int mRsCode = HttpURLConnection.HTTP_OK;
    public HttpResponse(HttpURLConnection conn){
        this.mHttpConn = conn;
    }
    public void closeConn(){
        mHttpConn.disconnect();
    }
    public JSONObject getRsContent(){
        BufferedReader in = null;
        BufferedWriter out = null;
        InputStreamReader reader = null;
        String line;
        StringBuilder resultJson = new StringBuilder();
        JSONObject jsonRetStr = new JSONObject();
        int resCode;
        try {
            out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
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
        }
        return jsonRetStr;
    }
}
