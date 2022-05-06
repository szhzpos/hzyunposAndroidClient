package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.customerView.bean.CVSetting;
import com.wyc.cloudapp.customerView.CVUtils;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.serialScales.AbstractWeightedScaleImp;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj;
import com.wyc.cloudapp.print.cashDrawer.ConnPrinter;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortFinder;

public class PeripheralSetting extends AbstractParameterFragment {
    private static final String mTitle = "外设设置";

    private ArrayAdapter<String> mSerialPortAdapter;
    private JSONArray mProTypes;

    public static final String connPrinter = ConnPrinter.class.getSimpleName();
    public static final String NONE = "NONE";

    public PeripheralSetting() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject loadContent() {

        get_or_show_serialScale_setting(false);
        get_or_show_cashbox_setting(false);
        get_or_show_cv_setting(false);
        return null;
    }

    @Override
    public boolean saveContent() {
        final String p_id_key  = "parameter_id",p_c_key = "parameter_content",p_desc_key = "parameter_desc";
        final JSONArray array = new JSONArray();
        final StringBuilder err = new StringBuilder();

        JSONObject content = new JSONObject();

        content.put(p_id_key, "serial_port_scale");
        content.put(p_c_key, get_or_show_serialScale_setting(true));
        content.put(p_desc_key, "串口秤设置");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key, "cashbox");
        content.put(p_c_key, get_or_show_cashbox_setting(true));
        content.put(p_desc_key, "钱箱设置");
        array.add(content);

        content = new JSONObject();
        content.put(p_id_key, CVSetting.KEY);
        content.put(p_c_key, get_or_show_cv_setting(true));
        content.put(p_desc_key, "顾显设置");
        array.add(content);

        if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err,1)){
            MyDialog.ToastMessage(null,err.toString(), null);
        }else{
            MyDialog.ToastMessage(null,mContext.getString(R.string.save_hint), null);
        }

        return false;
    }

    @Override
    protected void viewCreated() {
        findViewById(R.id.save).setOnClickListener(v->saveContent());
        //初始化
        initSerialScale();
        initCashDrawer();
        initCustomerView();
        //加载参数
        loadContent();
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

    private void initCustomerView(){
        final TextView cv = findViewById(R.id.ks),port = findViewById(R.id.ks_port);
        if (cv != null && port != null){
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TreeListDialogForObj treeListDialog = new TreeListDialogForObj(mContext,"顾显");
                    treeListDialog.setData(generate(),null,true);
                    if (treeListDialog.exec() == 1){
                        final TreeListItem object = treeListDialog.getSingleContent();
                        cv.setText(object.getItem_name());
                        cv.setTag(object.getItem_id());
                    }
                }
                private List<TreeListItem> generate(){
                    List<TreeListItem> data = CVUtils.support();
                    final TreeListItem item = new TreeListItem();
                    item.setItem_id(NONE);
                    item.setItem_name("无");
                    data.add(0,item);
                    return data;
                }
            });

            port.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TreeListDialogForObj treeListDialog = new TreeListDialogForObj(mContext,getString(R.string.port_sz));
                    treeListDialog.setData(generate(),null,true);
                    if (treeListDialog.exec() == 1){
                        final TreeListItem object = treeListDialog.getSingleContent();
                        port.setText(object.getItem_name());
                        port.setTag(object.getItem_id());
                    }
                }
                private List<TreeListItem> generate(){
                    List<TreeListItem> data = new ArrayList<>();
                    TreeListItem item = new TreeListItem();
                    item.setItem_id(NONE);
                    item.setItem_name("无");
                    data.add(item);

                    final String[] ports = new SerialPortFinder().getAllDevicesPath();
                    for (String p : ports){
                        item = new TreeListItem();
                        item.setItem_id(p);
                        item.setItem_name(p);
                        data.add(item);
                    }
                    return data;
                }
            });
        }
    }

    private void initCashDrawer(){
        final TextView c_box = findViewById(R.id.c_box);
        if (c_box != null){
            c_box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TreeListDialogForObj treeListDialog = new TreeListDialogForObj(mContext,"收银机");
                    treeListDialog.setData(generate(),null,true);
                    if (treeListDialog.exec() == 1){
                        final TreeListItem object = treeListDialog.getSingleContent();
                        c_box.setText(object.getItem_name());
                        c_box.setTag(object.getItem_id());
                    }
                }
                private List<TreeListItem> generate(){
                    List<TreeListItem> data = new ArrayList<>();

                    TreeListItem item = new TreeListItem();
                    item.setItem_id(NONE);
                    item.setItem_name("无");
                    data.add(item);

                    item = new TreeListItem();
                    item.setItem_id(connPrinter);
                    item.setItem_name(getString(R.string.conn_printer));
                    data.add(item);

                    return data;
                }
            });
        }
    }

    private void initSerialScale(){
        final Spinner pro_type = findViewById(R.id.pro_type),ser_port = findViewById(R.id.ser_port);
        mSerialPortAdapter = new ArrayAdapter<>(mContext,R.layout.drop_down_style);
        final ArrayAdapter<String> proTypeAdaper = new ArrayAdapter<>(mContext,R.layout.drop_down_style);
        mSerialPortAdapter.setDropDownViewResource(R.layout.drop_down_style);
        proTypeAdaper.setDropDownViewResource(R.layout.drop_down_style);
        ser_port.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final CheckBox auto_weigh_rb = findViewById(R.id.auto_weigh);
                if (NONE.equals(mSerialPortAdapter.getItem(position))){
                    auto_weigh_rb.setChecked(false);
                    auto_weigh_rb.setVisibility(View.GONE);
                }else auto_weigh_rb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //协议类型
        mProTypes = AbstractWeightedScaleImp.generateProductType();
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
        mSerialPortAdapter.add(NONE);
        for(String value : entryValues){
            mSerialPortAdapter.add(value);
        }
        ser_port.setAdapter(mSerialPortAdapter);

    }
    private JSONObject get_or_show_serialScale_setting(boolean way){
        JSONObject object;
        final Spinner pro_type_s = findViewById(R.id.pro_type),ser_port_s = findViewById(R.id.ser_port);
        final CheckBox auto_weigh_rb = findViewById(R.id.auto_weigh);
        final TextView c_box = findViewById(R.id.c_box);
        if (way){
            object = mProTypes.getJSONObject(pro_type_s.getSelectedItemPosition());
            object.put("ser_port",ser_port_s.getSelectedItem());
            object.put("auto_weigh",auto_weigh_rb.isChecked());
            object.put("c_box",Utils.getViewTagValue(c_box,""));
        }else{
            object = new JSONObject();
            if (SQLiteHelper.getLocalParameter("serial_port_scale",object)){
                final String cls_id = Utils.getNullStringAsEmpty(object,"cls_id"),name = Utils.getNullStringAsEmpty(object,"name"),
                        ser_port = Utils.getNullStringAsEmpty(object,"ser_port");

                boolean auto_weigh = object.getBooleanValue("auto_weigh");
                auto_weigh_rb.setChecked(auto_weigh);

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
                MyDialog.ToastMessage("加载串口秤参数错误：" + object.getString("info"), null);
        }
        return object;
    }

    private JSONObject get_or_show_cashbox_setting(boolean way){
        final JSONObject object = new JSONObject();
        final TextView c_box = findViewById(R.id.c_box);
        if (c_box != null){
            if (way){
                object.put("c_box",Utils.getViewTagValue(c_box,connPrinter));
                object.put("c_box_name",c_box.getText().toString());
            }else{
                if (loadCashboxSetting(object)){
                    if (object.isEmpty()){
                        c_box.setTag(connPrinter);
                        c_box.setText(R.string.conn_printer);
                    }else {
                        c_box.setTag(object.getString("c_box"));
                        c_box.setText(object.getString("c_box_name"));
                    }
                }
            }
        }
        return object;
    }
    public static boolean loadCashboxSetting(JSONObject object){
        if (object == null)object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("cashbox",object)){
            return true;
        }
        MyDialog.ToastMessage("加载钱箱参数错误：" + object.getString("info"), null);
        return false;
    }

    /**
     * 顾显参数
     * */
    private JSONObject get_or_show_cv_setting(boolean way){
        JSONObject object = new JSONObject();
        final TextView cv = findViewById(R.id.ks),port = findViewById(R.id.ks_port);
        if (cv != null && port != null){
            if (way){
                final CVSetting setting = new CVSetting();
                setting.setCsl(Utils.getViewTagValue(cv,""));
                setting.setName(cv.getText().toString());
                setting.setBoundRate(2400);
                setting.setPort(port.getText().toString());
                object = JSONObject.parseObject(JSON.toJSONString(setting));
            }else{
                final CVSetting setting = CVSetting.getInstance();
                if (setting != null){
                    cv.setText(setting.getName());
                    cv.setTag(setting.getCsl());
                    port.setText(setting.getPort());
                }
            }
        }
        return object;
    }

}
