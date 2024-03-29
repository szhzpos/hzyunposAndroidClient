package com.wyc.cloudapp.dialog.barcodeScales;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.data.SQLiteHelper;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;

public class DH15A extends AbstractBarcodeScaleImp {
    @Override
    public String getPort() {
        return "4001";
    }
    @Override
    public boolean down(final JSONObject scale_info){
        boolean code;
        String ip = scale_info.getString("scale_ip"),goods_c_id = scale_info.getString("g_c_id");
        int port = scale_info.getIntValue("scale_port");
        JSONArray records = new JSONArray();
        final JSONObject object = new JSONObject();
        if (code = SQLiteHelper.getLocalParameter("scale_setting",object)){
            final String prefix = object.getString("prefix");
            if (goods_c_id.contains(AbstractBarcodeScaleImp.CATEGORY_SEPARATE)){
                String[] ids = goods_c_id.split("\\" + AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                for (String id : ids) {
                    code = generate_data_record(records,id, prefix);
                }
            }else{
                code = generate_data_record(records,goods_c_id, prefix);
            }
            if (code){
                code = down_tcp(ip,port,records);
            }
        }else{
            if (mCallback != null){
                mCallback.OnShow(object.getString("info"));
            }
        }
        return code;
    }

    private boolean down_tcp(final String ip,int port,final JSONArray records){
        boolean code  = true;
        Logger.d("IP:%s,port:%d",ip,port);
        if (mCallback != null){
            mCallback.OnShow("正在下发...");
        }
        synchronized (DH15A.class){
            JSONObject record_obj = null;
            String ret_code,parse_code,plu_code;
            try (Socket socket = new Socket()){
                socket.connect(new InetSocketAddress(ip,port),10000);
                socket.setSoTimeout(10000);
                try (OutputStream outputStream = socket.getOutputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),CHARACTER_SET)) ){
                    int loop_cnt = 0;
                    outputStream.write(new byte[]{0x21,0x30,0x49,0x41,0x0D,0x0A,0x03});
                    outputStream.flush();
                    while (!bufferedReader.ready() && loop_cnt <= 30){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        loop_cnt ++;
                    }
                    ret_code = bufferedReader.readLine();
                    parse_code = ret_code.substring(0,ret_code.lastIndexOf('a'));
                    if (!parse_code.equals("0ia")){
                        throw new IOException("清除PLU失败<"+ ret_code +">");
                    }

                    if (loop_cnt != 0)loop_cnt = 0;

                    for (int i = 0,size = records.size();i < size;i++){
                        record_obj = records.getJSONObject(i);
                        if (null != record_obj){
                            if (mCallback != null){
                                mCallback.OnShow(String.format(Locale.CHINA,"正在下发<%d>...",i+1));
                            }
                            outputStream.write(record_obj.getString("r").getBytes(CHARACTER_SET));
                            outputStream.flush();
                            while (!bufferedReader.ready() && loop_cnt <= 30){
                                Logger.d("循环等待：%d",loop_cnt);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                loop_cnt ++;
                            }
                            ret_code = bufferedReader.readLine();
                            parse_code = ret_code.substring(ret_code.indexOf("v") + 1,ret_code.indexOf("a"));
                            plu_code = Utils.getNullStringAsEmpty(record_obj,"plu");
                            if (!plu_code.equals(parse_code)){
                                throw new IOException(String.format(Locale.CHINA,"<%s>下发失败，返回<%s>",record_obj.getString("title"),ret_code));
                            }
                        }
                    }
                    if (mCallback != null){
                        mCallback.OnShow("下发完成！");
                    }
                }
            }catch (IOException e){
                code = false;
                e.printStackTrace();
                if (mCallback != null){
                    mCallback.OnShow("下发错误：" + e.getMessage());
                }
            }
        }
        return code;
    }

    private boolean generate_data_record(@NonNull final JSONArray data_records, final String goods_c_id, String prefix){
        boolean code = false;
        StringBuilder err = new StringBuilder();
        String category_id = SQLiteHelper.getString("select category_id from shop_category where path like '%" + goods_c_id +"%'",err);
        if (null != category_id){
            category_id = category_id.replace("\r\n",",");
            JSONArray goods = SQLiteHelper.getListToJson("select only_coding,retail_price,metering_id,goods_title,shelf_life from barcode_info where type = 2 and (goods_status = '1' and barcode_status = '1') and category_id in (" + category_id +")",err);
            if (goods != null){
                //plu(0001~4000) %1,item_id(7) %2 货号，price(6) %3 价格， shelf_life(3) %4 保质期， title %5 商品名称，prefix(2) %6 前缀 m_id(1) %7 <0 计重,1计分>
                final String sz_data_record = "!0V%1A%2%3%7000000%4%600000000000000000000000000000000000000000000000B%5CDE";//考虑用StringBuilder
                final StringBuilder stringBuilder = new StringBuilder();

                JSONObject tmp_obj,record_obj;
                String plu_code,item_id,m_id,title,shelf_life,price,tmp_item_id,tmp_title;
                try {
                    final String end_code = new String(new byte[]{0x0D,0x0A,0x03},CHARACTER_SET);
                    long start = System.currentTimeMillis();
                    Logger.d("start:%d",start);
                    for (int i = 0,size = goods.size();i < size;i++){
                        tmp_obj = goods.getJSONObject(i);
                        m_id = tmp_obj.getString("metering_id");


                        tmp_item_id = tmp_obj.getString("only_coding");
                        plu_code = Utils.substringFormRight("0000" + tmp_item_id,4);
                        item_id = Utils.substringFormRight("0000000" + tmp_item_id,7);

                        price = Utils.substringFormRight("000000" + String.format(Locale.CHINA,"%.0f",tmp_obj.getDouble("retail_price") * 100 ),6);
                        shelf_life = Utils.substringFormRight("000" + tmp_obj.getString("shelf_life"),3);

                        tmp_title = tmp_obj.getString("goods_title");
                        title = get_zone_bit_code(tmp_title);

                        prefix = Utils.substringFormRight("00" + prefix,2);

                        stringBuilder.delete(0,stringBuilder.length()).append(sz_data_record);
                        int index = stringBuilder.indexOf("%1");
                        stringBuilder.replace(index,index + 2,plu_code);

                        index = stringBuilder.indexOf("%2");
                        stringBuilder.replace(index,index + 2,item_id);

                        index = stringBuilder.indexOf("%3");
                        stringBuilder.replace(index,index + 2,price);

                        index = stringBuilder.indexOf("%4");
                        stringBuilder.replace(index,index + 2,shelf_life);

                        index = stringBuilder.indexOf("%5");
                        stringBuilder.replace(index,index + 2,title);

                        index = stringBuilder.indexOf("%6");
                        stringBuilder.replace(index,index + 2,prefix);

                        index = stringBuilder.indexOf("%7");
                        stringBuilder.replace(index,index + 2,m_id);

                        record_obj = new JSONObject();
                        record_obj.put("r",stringBuilder.append(end_code).toString());

                        record_obj.put("plu",plu_code);
                        record_obj.put("item_id",tmp_item_id);
                        record_obj.put("title",tmp_title);

                        data_records.add(record_obj);
                    }
                    Logger.d("end:%d",System.currentTimeMillis() - start);
                    code = true;
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                    err.append(e.getMessage());
                }
            }
        }
        if (!code){
            if (mCallback != null)mCallback.OnShow(err.toString());
        }
        return code;
    }
}
