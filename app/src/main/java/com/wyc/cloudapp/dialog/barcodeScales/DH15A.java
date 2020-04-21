package com.wyc.cloudapp.dialog.barcodeScales;

import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;

public class DH15A extends AbstractBarcodeScale {
    private static final String  CHARACTER_SET = "GB2312";
    public DH15A(){

    }
    @Override
    public String getPort() {
        return "4001";
    }

    @Override
    public boolean down(JSONObject scales_info,StringBuilder err){
        boolean code = true;
        String ip = scales_info.optString("scale_ip"),goods_c_id = scales_info.optString("g_c_id");
        int port = scales_info.optInt("scale_port");
        JSONArray records;
        if (goods_c_id.contains(AbstractBarcodeScale.CATEGORY_SEPARATE)){
            String[] ids = goods_c_id.split("\\" + AbstractBarcodeScale.CATEGORY_SEPARATE);
            JSONArray tmp;
            records = new JSONArray();
            for (String id : ids) {
                tmp = generate_data_record(id, "24", err);
                if (null != tmp) {
                    Utils.moveJsonArray(tmp,records);
                }
            }
        }else{
            records = generate_data_record(goods_c_id,"24",err);
        }
        if (records != null){
            try {
                down_tcp(ip,port,records);
            } catch (IOException e) {
                e.printStackTrace();
                err.append(e.getMessage());
                code = false;
            }
        }else
            code = false;

        return code;
    }

    private void down_tcp(final String ip,int port,JSONArray records) throws IOException{
        Logger.d("IP:%s,port:%d",ip,port);
        synchronized (DH15A.class){
            try (Socket socket = new Socket();){
                socket.connect(new InetSocketAddress(ip,port),3000);
                socket.setSoTimeout(5000);
                try (OutputStream outputStream = socket.getOutputStream(); InputStream inputStream = socket.getInputStream()){
                    outputStream.write(new byte[]{0x21,0x30,0x49,0x41,0x0D,0x0A,0x03});
                    outputStream.write(new byte[]{0x21,0x30,0x48,0x41,0x0D,0x0A,0x03});

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    byte[] in_ = new byte[inputStream.available()];
                    inputStream.read(in_);
                    Logger.d("删除返回：%s",new String(in_,CHARACTER_SET));

/*                    String sz_record = "";
                    for (int i = 0,size = records.length();i < size;i++){
                        sz_record = records.optString(i);
                        outputStream.write(sz_record.getBytes(CHARACTER_SET));

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        byte[] in = new byte[inputStream.available()];
                        inputStream.read(in);
                        Logger.d("sz_record:%s",sz_record);
                        Logger.d("下载返回：%s",new String(in,CHARACTER_SET));

                    }*/
                }
            }
        }
    }

    private JSONArray generate_data_record(final String goods_c_id,String prefix,final StringBuilder err){
        JSONArray data_records = null;
        String category_id = SQLiteHelper.getString("select category_id from shop_category where path like '%" + goods_c_id +"%'",err);
        if (null != category_id){
            category_id = category_id.replace("\r\n",",");
            JSONArray goods = SQLiteHelper.getListToJson("select only_coding,retail_price,metering_id,goods_title,shelf_life from barcode_info where type = 2 and (goods_status = '1' and barcode_status = '1') and category_id in (" + category_id +")",err);
            if (goods != null){
                //plu(0001~4000) %1,item_id(7) %2 货号，price(6) %3 价格， shelf_life(3) %4 保质期， title %5 商品名称，prefix(2) %6 前缀 m_id(1) %7 <0 计重,1计分>
                final String sz_data_record = "!0V%1A%2%3%70000000%4%600000000000000000000000000000000000000000000000B%5CDE";
                JSONObject object;
                String plu_code,item_id,m_id,title,shelf_life,price;
                try {
                    data_records = new JSONArray();
                    final String end_code = new String(new byte[]{0x0D,0x0A,0x03},CHARACTER_SET);
                    for (int i = 0,size = goods.length();i < size;i++){
                        object = goods.getJSONObject(i);
                        m_id = object.getString("metering_id");
                        plu_code = Utils.substringFormRight("0000" + i + 1,4);
                        item_id = Utils.substringFormRight("0000000" + object.getString("only_coding"),7);
                        price = Utils.substringFormRight("000000" + String.format(Locale.CHINA,"%.0f",object.getDouble("retail_price") * 100 ),6);
                        shelf_life = Utils.substringFormRight("000" + object.getString("shelf_life"),3);
                        title = get_zone_bit_code(object.getString("goods_title"));
                        prefix = Utils.substringFormRight("00" + prefix,2);

                        data_records.put(sz_data_record.replace("%1",plu_code).replace("%2",item_id).replace("%3",price)
                                .replace("%4",shelf_life).replace("%5",title).replace("%6",prefix).replace("%7",m_id) + end_code);
                    }
                }catch (JSONException | UnsupportedEncodingException e){
                    e.printStackTrace();
                    data_records = null;
                    if (err != null)err.append(e.getMessage());
                }
            }
        }
        return data_records;
    }
}
