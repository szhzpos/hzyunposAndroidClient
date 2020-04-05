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
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONObject;

import java.util.HashMap;

public class PeripheralSettingFragment extends BaseFragment {
    private static final String mTitle = "外设设置";
    private static final int REQUEST_BLUETOOTH__PERMISSIONS = 0xabc8;
    private Context mContext;
    private Spinner mPrintId;
    private CustomProgressDialog mProgressDialog;
    public PeripheralSettingFragment() {
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public JSONObject laodContent() {
        return null;
    }

    @Override
    public boolean saveContent() {
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
        mPrintId = view.findViewById(R.id.print_id);

        RadioGroup radioGroup = view.findViewById(R.id.print_way);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.bluetooth_p:
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION))){
                                MyDialog.ToastMessage("App不能搜索蓝牙打印机，请设置允许App定位权限",mContext,null);
                            }else {
                                PeripheralSettingFragment.this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH__PERMISSIONS );
                            }
                        }else{
                            startDiscovery();//开始扫描
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
        });

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
        stopDiscovery();
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull  int[]  grantResults) {
        if (requestCode == REQUEST_BLUETOOTH__PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDiscovery();//开始扫描
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
                                Logger.d("name:%s,addr:%s",bluetoothDevice_find.getName(),bluetoothDevice_find.getAddress());
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        if (mProgressDialog != null){
                            mProgressDialog.setMessage("正在搜索蓝牙打印机...").refreshMessage().show();
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

    private void startDiscovery(){
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
    private void stopDiscovery(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
        }
    }
}
