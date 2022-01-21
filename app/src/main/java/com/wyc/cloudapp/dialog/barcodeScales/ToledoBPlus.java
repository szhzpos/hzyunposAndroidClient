package com.wyc.cloudapp.dialog.barcodeScales;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mt.rt.aoapi.AOScale;
import com.mt.rt.aoapi.DataType;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.barcodeScales
 * @ClassName: ToledobPlus
 * @Description: 托利多bPlus
 * @Author: wyc
 * @CreateDate: 2022-01-20 14:08
 * @UpdateUser: 更新者
 * @UpdateDate: 2022-01-20 14:08
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ToledoBPlus extends AbstractBarcodeScaleImp {
    private final AOScale mAOScale;
    public ToledoBPlus(){
        mAOScale = new AOScale(new MyHandler(this));
    }

    @Override
    public String getPort() {
        return "3001";
    }

    @Override
    public boolean down(@NonNull JSONObject scale_info) {

        final String ip = scale_info.getString("scale_ip"),goods_c_id = scale_info.getString("g_c_id");
        final int port = scale_info.getIntValue("scale_port");
        final JSONArray records = new JSONArray();
        final JSONArray barcodeRecords = new JSONArray();

        boolean code;
        final JSONObject object = new JSONObject();
        if (code = SQLiteHelper.getLocalParameter("scale_setting",object)){
            final String prefix = object.getString("prefix");
            if (goods_c_id.contains(AbstractBarcodeScaleImp.CATEGORY_SEPARATE)){
                String[] ids = goods_c_id.split("\\" + AbstractBarcodeScaleImp.CATEGORY_SEPARATE);
                for (String id : ids) {
                    code = generate_data_record(records,barcodeRecords,id, prefix);
                }
            }else{
                code = generate_data_record(records,barcodeRecords,goods_c_id, prefix);
            }
            if (code){

                final org.json.JSONObject data = new org.json.JSONObject();
                final org.json.JSONObject barcodeData = new org.json.JSONObject();
                try {
                    barcodeData.put("barcodes",new org.json.JSONArray(JSON.toJSONString(barcodeRecords)));
                    data.put("data",new org.json.JSONArray(JSON.toJSONString(records)));
                    dow_lib(ip,port,data,barcodeData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }else{
            if (mCallback != null){
                mCallback.OnShow(object.getString("info"));
            }
        }
        return code;
    }

    private boolean generate_data_record(@NonNull final JSONArray data_records,@NonNull final JSONArray barcode_data_records, final String goods_c_id, String prefix){
        boolean code = false;
        StringBuilder err = new StringBuilder();
        String category_id = SQLiteHelper.getString("select category_id from shop_category where path like '%" + goods_c_id +"%'",err);
        if (null != category_id){
            category_id = category_id.replace("\r\n",",");
            final JSONArray goods = SQLiteHelper.getListToJson("select only_coding,barcode,retail_price,metering_id,goods_title,shelf_life from barcode_info where type = 2 and (goods_status = '1' and barcode_status = '1') and category_id in (" + category_id +")",err);
            if (goods != null){
                JSONObject object,obj;
                int metering_id;
                int barcodeId;
                for (int i = 0,size = goods.size();i < size;i ++){
                    obj = goods.getJSONObject(i);

                    metering_id = obj.getIntValue("metering_id");

                    object = new JSONObject();
                    object.put("plu",obj.getString("only_coding"));

                    final JSONArray names = new JSONArray();
                    names.add(obj.getString("goods_title"));
                    object.put("descriptions",names);

                    final JSONObject priceA = new JSONObject();
                    priceA.put("value",obj.getString("retail_price"));
                    priceA.put("uom",metering_id == 1 ? "PCS" : "KGM");
                    priceA.put("type","BasePrice");

                    object.put("priceA",priceA);

                    final JSONObject sellBy = new JSONObject();
                    sellBy.put("value",obj.getString("shelf_life"));
                    sellBy.put("unit","day");
                    sellBy.put("printable",true);

                    object.put("sellBy",sellBy);

                    barcodeId = obj.getIntValue("only_coding");
                    object.put("barcode",i + 1);
                    object.put("labels",new JSONArray());

                    data_records.add(object);

                    //条码数据
                    object = new JSONObject();
                    object.put("id",i + 1);
                    object.put("type", "EAN13");
                    object.put("definition",prefix + "PPPPPBBBBBC");

                    barcode_data_records.add(object);

                }
                code = true;
            }
        }
        if (!code){
            if (mCallback != null)mCallback.OnShow(err.toString());
        }
        return code;
    }
    private void dow_lib(final String ip,final int port,org.json.JSONObject data,org.json.JSONObject barcodeData){
        final List<Pair<String, Integer>> scales = new ArrayList<>();
        scales.add(new Pair<>(ip,port));

        Logger.d(barcodeData);
        mAOScale.write(scales, DataType.BARCODE,barcodeData);

        while (mAOScale.isWriteBusy()){
            LockSupport.parkNanos(1000 * 1000 * 100);
        }

        Logger.d(data);
        mAOScale.write(scales, DataType.PLUET,data);
    }

    private final static class MyHandler extends Handler{
        private final ToledoBPlus mPlus;
        private MyHandler(ToledoBPlus plus){
            super(Looper.getMainLooper());
            mPlus = plus;
        }
        private void showMsg(String msg){
            if (mPlus.mCallback != null)mPlus.mCallback.OnShow(msg);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AOScale.EVT_COMM_TRANSMITION_BEGIN:
                    // 开始向一台电子秤写入数据
                    showMsg("开始下发...");
                    break;
                case AOScale.EVT_COMM_TRANSMITION_END:
                    // 结束向一台电子秤写入数据
                    showMsg("下发成功！");
                    break;
                case AOScale.EVT_COMM_WAITE_ALL_END:
                    // 向所有电子秤的写入均已结束
                    // 调用如下函数获取所有的结果
                    List<Integer> retList = mPlus.mAOScale.getWriteErrorCode();
                    break;
                case AOScale.EVT_COMM_WAITE_DATA_TOOLONG:
                    // 待传输的数据太多，应减少后重试
                    showMsg("待传输的数据太多，应减少后重试");
                    break;
                case AOScale.EVT_COMM_SCALE_REPORT_ERROR:
                    // 电子秤回应错误
                    showMsg("电子秤回应错误");
                    break;
                case AOScale.EVT_COMM_SENDDATA_SECCESS:
                    // 成功向电子秤写入数据
                    showMsg("正在写入数据...");
                    break;
                case AOScale.EVT_COMM_SENDDATA_EXCEPTION:
                    // 向电子秤写入数据时出错（捕获到异常），可以检查日志查找错误原因
                    showMsg("向电子秤写入数据时出错（捕获到异常），可以检查日志查找错误原因");
                    break;
                case AOScale.EVT_COMM_RECVDATA_UNCOMPLETE:
                    // 读取到的电子秤响应数据不完整
                    showMsg("读取到的电子秤响应数据不完整");
                    break;
                case AOScale.EVT_COMM_RECVDATA_UNKNOWN:
                    // 读取到的电子秤响应数据不可识别
                    showMsg("读取到的电子秤响应数据不可识别");
                    break;
                case AOScale.EVT_COMM_RECVDATA_EXCEPTION:
                    // 读取电子秤响应数据时出错（捕获到异常），可以检查日志查找错误原因
                    showMsg("读取电子秤响应数据时出错（捕获到异常），可以检查日志查找错误原因");
                    break;
                case AOScale.EVT_COMM_RECVDATA_TIMEOUT:
                    // 读取电子秤响应数据时超时
                    showMsg("读取电子秤响应数据时超时");
                    break;
                case AOScale.EVT_SOCKET_OPEN_FAILED:
                    // 初始化和电子秤的连接失败
                    showMsg("初始化和电子秤的连接失败");
                    break;
                case AOScale.EVT_SOCKET_CONNECT_FAILED:
                    // 连接电子秤失败
                    showMsg("连接电子秤失败");
                    break;
                case AOScale.EVT_SOCKET_CONNECT_EXCEPTION:
                    // 连接电子秤时出错（捕获到异常），可以检查日志查找错误原因
                    showMsg("连接电子秤时出错（捕获到异常），可以检查日志查找错误原因");
                    break;
                case AOScale.EVT_SOCKET_DISCONNECT_EXCEPTION:
                    // 断开和电子秤的连接时出错（捕获到异常），可以检查日志查找错误原因
                    showMsg("断开和电子秤的连接时出错（捕获到异常），可以检查日志查找错误原因");
                    break;
                case AOScale.EVT_PARSE_JSON_EXCEPTION:
                    // 解析传入的JSON数据时出错，可以检查日志查找错误原因
                    showMsg("解析传入的JSON数据时出错，可以检查日志查找错误原因");
                    break;
                case AOScale.EVT_GENERAL_EXCEPTION:
                    // 捕获到其它异常，可以检查日志查找错误原因
                    showMsg("捕获到其它异常，可以检查日志查找错误原因");
                    break;
                case AOScale.EVT_SCALE_COUNT_IS_FULL:
                    // 同时传输的电子秤太多，应减少参数中的电子秤数量后重试
                    showMsg("同时传输的电子秤太多，应减少参数中的电子秤数量后重试");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        mAOScale.cleanup();
        Logger.d("%s has finalized",getClass().getSimpleName());
    }
}
