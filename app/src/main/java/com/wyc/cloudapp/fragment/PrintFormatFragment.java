package com.wyc.cloudapp.fragment;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class PrintFormatFragment extends AbstractBaseFragment {
    public static final String ACTION_USB_PERMISSION = "com.wyc.cloudapp.USB_PERMISSION";
    private static final int REQUEST_BLUETOOTH__PERMISSIONS = 0xabc8;
    private static final int REQUEST_BLUETOOTH_ENABLE = 0X8888;
    private static final String mTitle = "打印设置";
    private ArrayAdapter<String> mPrintIdAdapter;
    private CustomProgressDialog mProgressDialog;
    public PrintFormatFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject loadContent() {
        get_or_show_printer_setting(false);
        get_or_show_print_format_content_2(false);

        return null;
    }

    @Override
    public boolean saveContent() {
        final JSONArray array = get_or_show_print_format_content_2(true);
        final JSONObject content = new JSONObject();
        content.put("parameter_id","printer");
        content.put("parameter_content",get_or_show_printer_setting(true));
        content.put("parameter_desc","打印机设置");
        array.add(content);

        final StringBuilder err = new StringBuilder();
        if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err,1)){
            MyDialog.ToastMessage(null,err.toString(),mContext,null);
        }else{
            MyDialog.ToastMessage(null,"保存成功！",mContext,null);
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.print_format_content_layout,container);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        final Button save_btn = mRootView.findViewById(R.id.save);
        initPrinterId();
        //保存参数
        save_btn.setOnClickListener(v->saveContent());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mProgressDialog = new CustomProgressDialog(context);
        {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intentFilter.addAction(ACTION_USB_PERMISSION);
            context.registerReceiver(usb_bluetooth_receiver,intentFilter);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        loadContent();
    }

    @Override
    public void onPause(){
        super.onPause();
        stopBlueToothDiscovery();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(usb_bluetooth_receiver);
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull  int[]  grantResults) {
        if (requestCode == REQUEST_BLUETOOTH__PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBlueToothDiscovery();//开始扫描
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){//蓝牙开启回调
        if (resultCode == RESULT_OK && requestCode == REQUEST_BLUETOOTH_ENABLE){
            startBlueToothDiscovery();//开始扫描
        }
    }

    private void initPrinterId(){
        Spinner printerId = mRootView.findViewById(R.id.printer_id);
        mPrintIdAdapter = new ArrayAdapter<>(mContext,R.layout.drop_down_style);
        mPrintIdAdapter.setDropDownViewResource(R.layout.drop_down_style);
        mPrintIdAdapter.add("请选择打印机");
        printerId.setAdapter(mPrintIdAdapter);
        printerId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String tmp = mPrintIdAdapter.getItem(position);
                if (null != tmp){
                    String[] vals = tmp.split("\t");
                    if (vals.length > 1){
                        RadioGroup radioGroup = mRootView.findViewById(R.id.print_way);
                        switch (radioGroup.getCheckedRadioButtonId()){
                            case R.id.bluetooth_p:
                                bondBlueTooth(vals[1]);
                                stopBlueToothDiscovery();
                                break;
                            case R.id.usb_p:
                                startUSBDiscoveryAndAuth(vals[0].substring(vals[0].indexOf(":") + 1),vals[1].substring(vals[1].indexOf(":") + 1));
                                break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Logger.d("onNothingSelected");
            }
        });
        printerId.setOnTouchListener((v, event) -> {
            v.performClick();
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                startFindDevice();
            }
            return false;
        });
    }

    private JSONObject get_or_show_print_format_content(boolean way){
        final JSONObject object = new JSONObject(),content = new JSONObject();
        if (mRootView != null){
            final RadioGroup frg = mRootView.findViewById(R.id.format_rg),fzrg = mRootView.findViewById(R.id.format_size_rg);
            final EditText stores_name = mRootView.findViewById(R.id.stores_name),footer_c = mRootView.findViewById(R.id.footer_c),
                    p_count = mRootView.findViewById(R.id.c_count),footer_space = mRootView.findViewById(R.id.footer_space);
            String parameter_id = "",parameter_desc = "";
            int id = frg.getCheckedRadioButtonId();
            switch (id){
                case R.id.checkout_format:
                    parameter_id = "c_f_info";
                    parameter_desc = "结账小票打印格式信息";
                    break;
                case R.id.vip_c_format:
                    parameter_id = "v_f_info";
                    parameter_desc = "充值小票打印格式信息";
                    break;
                case R.id.refund_format:
                    parameter_id = "r_f_info";
                    parameter_desc = "退货小票打印格式信息";
                    break;
                case R.id.transfer_format:
                    parameter_id = "t_f_info";
                    parameter_desc = "交班小票打印格式信息";
                    break;
            }
             if (way){
                 object.put("f",id);
                 object.put("f_z",fzrg.getCheckedRadioButtonId());
                 object.put("s_n",stores_name.getText().toString());
                 object.put("f_c",footer_c.getText().toString());
                 object.put("p_c",p_count.getText().toString());
                 object.put("f_s",footer_space.getText().toString());

                 content.put("parameter_id", parameter_id);
                 content.put("parameter_content",object);
                 content.put("parameter_desc",parameter_desc);
             }else{
                 if (SQLiteHelper.getLocalParameter(parameter_id,object)){
                     fzrg.check(object.getIntValue("f_z"));
                     stores_name.setText(Utils.getNullStringAsEmpty(object,"s_n"));
                     footer_c.setText(Utils.getNullStringAsEmpty(object,"f_c"));
                     p_count.setText(Utils.getNullOrEmptyStringAsDefault(object,"p_c","1"));
                     footer_space.setText(Utils.getNullOrEmptyStringAsDefault(object,"f_s","5"));
                 }else{
                     MyDialog.ToastMessage("加载打印格式参数错误：" + object.getString("info"),mContext,null);
                 }
             }
        }
        return content;
    }

    private JSONArray get_or_show_print_format_content_2(boolean way){//每种格式共用公共参数
        final JSONArray array = new JSONArray();
        if (mRootView != null){
            JSONObject object = new JSONObject(),content = new JSONObject();
            final RadioGroup fzrg = mRootView.findViewById(R.id.format_size_rg);
            final EditText stores_name_et = mRootView.findViewById(R.id.stores_name),footer_c_et = mRootView.findViewById(R.id.footer_c),
                    p_count_et = mRootView.findViewById(R.id.c_count),footer_space_et = mRootView.findViewById(R.id.footer_space),
                    v_c_count_et = mRootView.findViewById(R.id.v_c_count),r_c_count_et = mRootView.findViewById(R.id.r_c_count),t_c_count_et = mRootView.findViewById(R.id.t_c_count);

            if (way){
                int size_id = fzrg.getCheckedRadioButtonId();
                final String stores_name = stores_name_et.getText().toString(),footer_c = footer_c_et.getText().toString(),p_count = p_count_et.getText().toString(),
                        footer_space = footer_space_et.getText().toString();

                //结账单
                object.put("f",R.id.checkout_format);
                object.put("f_z",size_id);
                object.put("s_n",stores_name);
                object.put("f_c",footer_c);
                object.put("p_c",p_count);
                object.put("f_s",footer_space);

                content.put("parameter_id","c_f_info");
                content.put("parameter_content",object);
                content.put("parameter_desc","结账小票打印格式信息");
                array.add(content);

                //充值单
                object = new JSONObject();
                object.put("f",R.id.vip_c_format);
                object.put("f_z",size_id);
                object.put("s_n",stores_name);
                object.put("f_c",footer_c);
                object.put("p_c",v_c_count_et.getText().toString());
                object.put("f_s",footer_space);
                content = new JSONObject();
                content.put("parameter_id", "v_f_info");
                content.put("parameter_content",object);
                content.put("parameter_desc","充值小票打印格式信息");
                array.add(content);

                //退货单
                object = new JSONObject();
                object.put("f",R.id.refund_format);
                object.put("f_z",size_id);
                object.put("s_n",stores_name);
                object.put("f_c",footer_c);
                object.put("p_c",r_c_count_et.getText().toString());
                object.put("f_s",footer_space);
                content = new JSONObject();
                content.put("parameter_id", "r_f_info");
                content.put("parameter_content",object);
                content.put("parameter_desc","退货小票打印格式信息");
                array.add(content);

                //交班单
                object = new JSONObject();
                object.put("f",R.id.transfer_format);
                object.put("f_z",size_id);
                object.put("s_n",stores_name);
                object.put("f_c",footer_c);
                object.put("p_c",t_c_count_et.getText().toString());
                object.put("f_s",footer_space);
                content = new JSONObject();
                content.put("parameter_id", "t_f_info");
                content.put("parameter_content",object);
                content.put("parameter_desc","交班小票打印格式信息");
                array.add(content);
            }else{
                boolean code = SQLiteHelper.getLocalParameter("c_f_info",object);
                if (code){
                    fzrg.check(object.getIntValue("f_z"));
                    stores_name_et.setText(Utils.getNullStringAsEmpty(object,"s_n"));
                    footer_c_et.setText(Utils.getNullStringAsEmpty(object,"f_c"));
                    p_count_et.setText(Utils.getNullOrEmptyStringAsDefault(object,"p_c","1"));
                    footer_space_et.setText(Utils.getNullOrEmptyStringAsDefault(object,"f_s","5"));

                    code = SQLiteHelper.getLocalParameter("v_f_info",object);
                    if (code)v_c_count_et.setText(Utils.getNullOrEmptyStringAsDefault(object,"p_c","1"));

                    code = SQLiteHelper.getLocalParameter("r_f_info",object);
                    if (code)r_c_count_et.setText(Utils.getNullOrEmptyStringAsDefault(object,"p_c","1"));

                    code = SQLiteHelper.getLocalParameter("t_f_info",object);
                    if (code)t_c_count_et.setText(Utils.getNullOrEmptyStringAsDefault(object,"p_c","1"));

                }else{
                    MyDialog.ToastMessage("加载打印格式参数错误：" + object.getString("info"),mContext,null);
                }
            }
        }
        return array;
    }
    private JSONObject get_or_show_printer_setting(boolean way) {
        int id = -1,status = 0;
        JSONObject object = new JSONObject();
        RadioGroup radioGroup = mRootView.findViewById(R.id.print_way);
        Spinner printerId = mRootView.findViewById(R.id.printer_id);
        if (way){
            switch (radioGroup.getCheckedRadioButtonId()){
                case R.id.bluetooth_p:
                    id = R.id.bluetooth_p;
                    status = 1;
                    break;
                case R.id.usb_p:
                    id = R.id.usb_p;
                    status = 1;
                    break;
            }
            object.put("id",id);
            object.put("s",status);
            object.put("v",printerId.getSelectedItem());
        }else{
            if (SQLiteHelper.getLocalParameter("printer",object)){
                String printer_info = Utils.getNullStringAsEmpty(object,"v");
                int status_id = object.getIntValue("id");
                String[] vals = printer_info.split("\t");
                if (vals.length > 1){
                    switch (status_id){
                        case R.id.bluetooth_p:
                            bondBlueTooth(vals[1]);
                            break;
                        case R.id.usb_p:
                            startUSBDiscoveryAndAuth(vals[0],vals[1]);
                            break;
                    }
                }
                radioGroup.check(status_id);
                mPrintIdAdapter.clear();
                mPrintIdAdapter.add(printer_info);
            }else
                MyDialog.ToastMessage("加载打印机参数错误：" + object.getString("info"),mContext,null);
        }

        return object;
    }

    private BroadcastReceiver usb_bluetooth_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String szAction = intent.getAction();
            if (szAction != null){
                switch (szAction){
                    case BluetoothDevice.ACTION_FOUND:
                        int device_style = 0;
                        Logger.d("ACTION_FOUND");
                        BluetoothDevice bluetoothDevice_find = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (bluetoothDevice_find != null){
                            device_style = bluetoothDevice_find.getBluetoothClass().getMajorDeviceClass();
                            if (device_style == BluetoothClass.Device.Major.IMAGING || device_style == BluetoothClass.Device.Major.MISC) {
                                String name = bluetoothDevice_find.getName(),addr = bluetoothDevice_find.getAddress(),content;
                                String[] szs;
                                boolean isExist = false;
                                if (mPrintIdAdapter != null){
                                    for(int i = 1,size = mPrintIdAdapter.getCount();i < size;i++){
                                        content = mPrintIdAdapter.getItem(i);
                                        if (content != null){
                                            szs = content.split("\t");
                                            if (szs.length > 1 && addr.equals(szs[1])){
                                                isExist = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!isExist){
                                        if (bluetoothDevice_find.getBondState() == BluetoothDevice.BOND_NONE){
                                            name = name.concat("<未配对>");
                                        }else{
                                            name = name.concat("<已配对>");
                                        }
                                        mPrintIdAdapter.add(name.concat("\t").concat(addr));
                                        mPrintIdAdapter.notifyDataSetChanged();
                                    }
                                }
                                Logger.d("name:%s,addr:%s",bluetoothDevice_find.getName(),bluetoothDevice_find.getAddress());
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        if (mProgressDialog != null){
                            mProgressDialog.setOnCancelListener(dialog -> stopBlueToothDiscovery());
                            mProgressDialog.setMessage("正在搜索蓝牙打印机...").refreshMessage();
                            if (!mProgressDialog.isShowing())mProgressDialog.show();
                        }
                        if (mPrintIdAdapter != null && mPrintIdAdapter.getCount() != 0){
                            mPrintIdAdapter.clear();
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        Logger.d("ACTION_BOND_STATE_CHANGED");
                        boolean bond_status = false;
                        BluetoothDevice bluetoothDevice_bound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (bluetoothDevice_bound != null){
                            if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0) == 12){
                                bond_status = true;
                            }
                            String addr = bluetoothDevice_bound.getAddress(),tmp,sz;
                            for(int i = 0,size = mPrintIdAdapter.getCount();i < size;i++){
                                tmp = mPrintIdAdapter.getItem(i);
                                if (tmp != null && tmp.contains(addr)){
                                    mPrintIdAdapter.remove(tmp);
                                    sz = tmp.substring(tmp.indexOf('<') + 1,tmp.lastIndexOf('>'));
                                    if (bond_status)
                                        tmp = tmp.replace(sz,"已配对");
                                    else
                                        tmp = tmp.replace(sz,"未配对");

                                    mPrintIdAdapter.add(tmp);
                                    mPrintIdAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                        break;
                    case ACTION_USB_PERMISSION:
                        synchronized (this) {
                            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if(device != null){
                                    mPrintIdAdapter.remove("vid:" + device.getVendorId() + "\tpid:" + device.getProductId());
                                    MyDialog.ToastMessage("拒绝权限将无法使用USB打印机",mContext,null);
                                }
                            }
                        }
                        break;
                }
            }
        }
    };

    private void startFindDevice(){
        RadioGroup radioGroup = mRootView.findViewById(R.id.print_way);
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.bluetooth_p:
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION))){
                        PrintFormatFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH__PERMISSIONS );
                        //MyDialog.ToastMessage("App不能搜索蓝牙打印机，请设置允许App定位权限",mContext,null);
                    }else {
                        PrintFormatFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH__PERMISSIONS );
                    }
                }else{
                    startBlueToothDiscovery();//开始扫描
                }
                break;
            case R.id.usb_p:
                startUSBDiscoveryAndAuth(null,null);
                break;
        }
    }
    private void startBlueToothDiscovery(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null )
            if (bluetoothAdapter.isEnabled()) {
                if (!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                }
            }else {
                MyDialog.displayAskMessage(mContext, "蓝牙已关闭，是否开启蓝牙功能？", myDialog -> {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent,REQUEST_BLUETOOTH_ENABLE);//请求开启蓝牙
                    myDialog.dismiss();
                }, Dialog::dismiss);
            }
        else
            MyDialog.ToastMessage("设备不支持蓝牙功能！",mContext,null);
    }
    private void stopBlueToothDiscovery(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
        }
    }
    private void bondBlueTooth(final String addr){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(addr);
            if (device.getBondState() == BluetoothDevice.BOND_NONE){
                device.createBond();
            }
        }
    }
    private void startUSBDiscoveryAndAuth(final String vid, final String pid){
        UsbManager manager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
        if (null != manager){
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            UsbDevice device = null;
            boolean isExist = false;
            for(String sz:deviceList.keySet()){
                device = deviceList.get(sz);
                if (null != device){
                    String value;
                    if (vid == null && null == pid) {
                        value = "vid:" + device.getVendorId() + "\tpid:" + device.getProductId();
                        for (int i = 0,size = mPrintIdAdapter.getCount();i < size;i++){
                            if (value.equals(mPrintIdAdapter.getItem(i))){
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist){
                            mPrintIdAdapter.add(value);
                        }
                        isExist = false;
                    }else{
                        if (String.valueOf(device.getVendorId()).equals(vid) && pid.equals(String.valueOf(device.getProductId()))){
                            isExist = true;
                            break;
                        }
                    }
                }
            }
            if ((isExist || mPrintIdAdapter.getCount() == 1) && device != null && !manager.hasPermission(device)){
                PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                manager.requestPermission(device, permissionIntent);
            }
        }
    }
}
