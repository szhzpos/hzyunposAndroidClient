package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.serialScales.AbstractSerialScaleImp;
import com.wyc.cloudapp.utils.Utils;

import android_serialport_api.SerialPortFinder;

public class PeripheralSettingFragment extends AbstractParameterFragment {
    private static final String mTitle = "外设设置";
    private ArrayAdapter<String> mSerialPortAdapter;
    private JSONArray mProTypes;
    public PeripheralSettingFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject loadContent() {

        get_or_show_serialScale_setting(false);
        return null;
    }

    @Override
    public boolean saveContent() {
        final String p_id_key  = "parameter_id",p_c_key = "parameter_content",p_desc_key = "parameter_desc";
        final JSONArray array = new JSONArray();
        final StringBuilder err = new StringBuilder();

        final JSONObject content = new JSONObject();

        content.put(p_id_key, "serial_port_scale");
        content.put(p_c_key, get_or_show_serialScale_setting(true));
        content.put(p_desc_key, "串口秤设置");
        array.add(content);

        if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err,1)){
            MyDialog.ToastMessage(null,err.toString(),mContext,null);
        }else{
            MyDialog.ToastMessage(null,"保存成功！",mContext,null);
        }

        return false;
    }

    @Override
    protected void viewCreated(boolean created) {
        if (created){
            findViewById(R.id.save).setOnClickListener(v->saveContent());
            //初始化
            initSerialScale();

            //加载参数
            loadContent();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.peripheral_setting_content_layout,container);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void initSerialScale(){
        final Spinner pro_type = findViewById(R.id.pro_type),ser_port = findViewById(R.id.ser_port);
        mSerialPortAdapter = new ArrayAdapter<>(mContext,R.layout.drop_down_style);
        final ArrayAdapter<String> proTypeAdaper = new ArrayAdapter<>(mContext,R.layout.drop_down_style);
        mSerialPortAdapter.setDropDownViewResource(R.layout.drop_down_style);
        proTypeAdaper.setDropDownViewResource(R.layout.drop_down_style);

        //协议类型
        mProTypes = AbstractSerialScaleImp.generateProductType();
        JSONObject tmp_obj;
        for (int i = 0,size = mProTypes.size();i < size;i++){
            tmp_obj = mProTypes.getJSONObject(i);
            if (null != tmp_obj){
                proTypeAdaper.add(Utils.getNullStringAsEmpty(tmp_obj,"name"));
            }
        }
        pro_type.setAdapter(proTypeAdaper);

        //端口
        final SerialPortFinder mSerialPortFinder = new SerialPortFinder();

        final String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        mSerialPortAdapter.add("NONE");
        for(String value : entryValues){
            mSerialPortAdapter.add(value);
        }
        ser_port.setAdapter(mSerialPortAdapter);

    }
    private JSONObject get_or_show_serialScale_setting(boolean way){
        JSONObject object;
        final Spinner pro_type_s = findViewById(R.id.pro_type),ser_port_s = findViewById(R.id.ser_port);
        if (way){
            object = mProTypes.getJSONObject(pro_type_s.getSelectedItemPosition());
            object.put("ser_port",ser_port_s.getSelectedItem());
        }else{
            object = new JSONObject();
            if (SQLiteHelper.getLocalParameter("serial_port_scale",object)){
                final String cls_id = Utils.getNullStringAsEmpty(object,"cls_id"),name = Utils.getNullStringAsEmpty(object,"name"),
                        ser_port = Utils.getNullStringAsEmpty(object,"ser_port");
                for (int i = 0,size = mProTypes.size();i < size;i ++){
                    object = mProTypes.getJSONObject(i);
                    if (null != object){
                        if (cls_id.equals(Utils.getNullStringAsEmpty(object,"cls_id"))){
                            pro_type_s.setSelection(i);
                            break;
                        }
                    }
                }
                for (int i = 0,size = mSerialPortAdapter.getCount();i < size;i ++){
                    if (ser_port.equals(mSerialPortAdapter.getItem(i))){
                        ser_port_s.setSelection(i);
                        break;
                    }
                }
            }else
                MyDialog.ToastMessage("加载串口秤参数错误：" + object.getString("info"),mContext,null);
        }
        return object;
    }

}
