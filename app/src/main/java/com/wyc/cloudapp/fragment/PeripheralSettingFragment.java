package com.wyc.cloudapp.fragment;

import android.Manifest;
import android.app.Dialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeripheralSettingFragment extends BaseFragment {
    private static final String mTitle = "外设设置";
    private static final int REQUEST_BLUETOOTH__PERMISSIONS = 0xabc8;
    private Context mContext;
    private CustomProgressDialog mProgressDialog;
    private View mRootView;
    private ArrayAdapter<String> mPrintIdAdapter;
    public PeripheralSettingFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject laodContent() {
        try {
            get_printer_setting(false);
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.ToastMessage(null,e.getMessage(),mContext,null);
        }
        return null;
    }

    @Override
    public boolean saveContent() {
        JSONObject content = new JSONObject();
        JSONArray array = new JSONArray();
        StringBuilder err = new StringBuilder();
        try {
            content.put("parameter_id", "printer");
            content.put("parameter_content", get_printer_setting(true));
            content.put("parameter_desc", "打印机设置");
            array.put(content);

            if (!SQLiteHelper.execSQLByBatchFromJson(array,"local_parameter",null,err)){
                MyDialog.ToastMessage(null,err.toString(),mContext,null);
            }else{
                MyDialog.ToastMessage(null,"保存成功！",mContext,null);
            }
        }catch (JSONException e){
            e.printStackTrace();
            MyDialog.ToastMessage(null,e.getMessage(),mContext,null);
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.peripheral_setting_content_layout,container);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        initPrintId();
        mRootView.findViewById(R.id.save).setOnClickListener(v->saveContent());
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            context.registerReceiver(usb_bluetooth_receiver,intentFilter);
        }
        mContext = context;
        mProgressDialog = new CustomProgressDialog(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(usb_bluetooth_receiver);
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    @Override
    public void onResume(){
        super.onResume();
        laodContent();
    }
    @Override
    public void onPause(){
        super.onPause();
        stopBlueToothDiscovery();
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
                                             szs = content.split("\r\n");
                                            if (szs.length > 1 && addr.equals(szs[1])){
                                                isExist = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!isExist){
                                        mPrintIdAdapter.add(name.concat("\r\n").concat(addr));
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
                        BluetoothDevice bluetoothDevice_bound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (bluetoothDevice_bound != null)
                            if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0) == 12){

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
                        MyDialog.ToastMessage("App不能搜索蓝牙打印机，请设置允许App定位权限",mContext,null);
                    }else {
                        PeripheralSettingFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH__PERMISSIONS );
                    }
                }else{
                    startBlueToothDiscovery();//开始扫描
                }
                break;
            case R.id.usb_p:
                UsbManager manager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
                if (null != manager){
                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    UsbDevice device = deviceList.get("deviceName");
                    Logger.d(deviceList.toString());
                }
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
                MyDialog.displayAskMessage(null, "蓝牙已关闭，是否开启蓝牙功能？", mContext, myDialog -> {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 0X8888);//请求开启蓝牙
                    myDialog.dismiss();
                },Dialog::dismiss);
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

    private void initPrintId(){
        Spinner printId = mRootView.findViewById(R.id.print_id);
        mPrintIdAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);
        mPrintIdAdapter.setDropDownViewResource(R.layout.drop_down_style);
        mPrintIdAdapter.add("请选择打印机");
        printId.setAdapter(mPrintIdAdapter);
        printId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 stopBlueToothDiscovery();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Logger.d("onNothingSelected");
            }
        });
        printId.setOnTouchListener((v, event) -> {
            v.performClick();
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                startFindDevice();
            }
            return false;
        });
    }

    private JSONObject get_printer_setting(boolean way) throws JSONException {
        int id = -1,status = 0;
        JSONObject object = new JSONObject();
        RadioGroup radioGroup = mRootView.findViewById(R.id.print_way);
        Spinner printId = mRootView.findViewById(R.id.print_id);
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
            object.put("v",printId.getSelectedItem());
        }else{
            if (SQLiteHelper.getLocalParameter("printer",object)){
                radioGroup.check(object.optInt("id"));
                mPrintIdAdapter.clear();
                mPrintIdAdapter.add(object.optString("v"));
            }else
                MyDialog.ToastMessage("加载打印机参数错误：" + object.getString("info"),mContext,null);
        }

        return object;
    }

}
