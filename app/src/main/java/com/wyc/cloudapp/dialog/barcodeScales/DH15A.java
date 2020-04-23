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

public class DH15A extends AbstractBarcodeScale {
    private static final String  CHARACTER_SET = "GB2312";
    private UpdateStatusCallback mCallback;
    public DH15A(){

    }
    @Override
    public String getPort() {
        return "4001";
    }

    @Override
    public boolean down(JSONObject scales_info){
        boolean code = false;
        String ip = scales_info.getString("scale_ip"),goods_c_id = scales_info.getString("g_c_id");
        int port = scales_info.getIntValue("scale_port");
        JSONArray records = new JSONArray();
        final JSONObject object = new JSONObject();
        if (code = SQLiteHelper.getLocalParameter("scale_setting",object)){
            final String prefix = object.getString("prefix");
            if (goods_c_id.contains(AbstractBarcodeScale.CATEGORY_SEPARATE)){
                String[] ids = goods_c_id.split("\\" + AbstractBarcodeScale.CATEGORY_SEPARATE);
                for (String id : ids) {
                    code = generate_data_record(records,id, prefix);
                }
            }else{
                code = generate_data_record(records,goods_c_id, prefix);
            }
            if (code){
                down_tcp(ip,port,records);
            }
        }else{
            if (mCallback != null){
                mCallback.updata(object.getString("info"));
            }
        }
        return code;
    }

    @Override
    public void setUpdateStatus(UpdateStatusCallback o) {
        mCallback = o;
    }

    private void down_tcp(final String ip,int port,final JSONArray records){
        Logger.d("IP:%s,port:%d",ip,port);
        if (mCallback != null){
            mCallback.updata("正在下发...");
        }
        synchronized (DH15A.class){
            JSONObject record_obj = null;
            try (Socket socket = new Socket();){
                socket.connect(new InetSocketAddress(ip,port),10000);
                socket.setSoTimeout(10000);
                try (OutputStream outputStream = socket.getOutputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),CHARACTER_SET)) ){
                    outputStream.write(new byte[]{0x21,0x30,0x49,0x41,0x0D,0x0A,0x03});
                    outputStream.write(new byte[]{0x21,0x30,0x48,0x41,0x0D,0x0A,0x03});

                    for (int i = 0,size = records.size();i < size;i++){
                        record_obj = records.getJSONObject(i);
                        if (null != record_obj){
                            outputStream.write(record_obj.getString("r").getBytes(CHARACTER_SET));
                            if (mCallback != null){
                                mCallback.updata(String.format(Locale.CHINA,"正在下发<%d>...",i+1));
                            }

                            Logger.d("record_obj:%s",record_obj);
                            Logger.d("下载返回：%s",bufferedReader.readLine());
                        }
                    }
                    if (mCallback != null){
                        mCallback.updata("下发完成！");
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
                if (mCallback != null){
                    if (record_obj != null){
                        mCallback.updata(record_obj.getString("title") + " 下发失败！");
                    }else{
                        mCallback.updata("下发错误：" + e.getMessage());
                    }
                }
            }
        }
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
                final String sz_data_record = "!0V%1A%2%3%7000000%4%600000000000000000000000000000000000000000000000B%5CDE";
                JSONObject tmp_obj,record_obj;
                String plu_code,item_id,m_id,title,shelf_life,price,tmp_item_id,tmp_title;
                try {
                    final String end_code = new String(new byte[]{0x0D,0x0A,0x03},CHARACTER_SET);
                    for (int i = 0,size = goods.size();i < size;i++){
                        tmp_obj = goods.getJSONObject(i);
                        m_id = tmp_obj.getString("metering_id");
                        plu_code = Utils.substringFormRight("0000" + i + 1,4);

                        tmp_item_id = tmp_obj.getString("only_coding");
                        item_id = Utils.substringFormRight("0000000" + tmp_item_id,7);

                        price = Utils.substringFormRight("000000" + String.format(Locale.CHINA,"%.0f",tmp_obj.getDouble("retail_price") * 100 ),6);
                        shelf_life = Utils.substringFormRight("000" + tmp_obj.getString("shelf_life"),3);

                        tmp_title = tmp_obj.getString("goods_title");
                        title = get_zone_bit_code(tmp_title);

                        prefix = Utils.substringFormRight("00" + prefix,2);

                        record_obj = new JSONObject();
                        record_obj.put("r",sz_data_record.replace("%1",plu_code).replace("%2",item_id).replace("%3",price)
                                .replace("%4",shelf_life).replace("%5",title).replace("%6",prefix).replace("%7",m_id) + end_code);
                        record_obj.put("item_id",tmp_item_id);
                        record_obj.put("title",tmp_title);
                        data_records.add(record_obj);
                    }
                    code = true;
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                    err.append(e.getMessage());
                }
            }
        }
        if (!code){
            if (mCallback != null)mCallback.updata(err.toString());
        }
        return code;
    }
}
