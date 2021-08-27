package com.wyc.cloudapp.utils;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wyc.cloudapp.dialog.MyDialog;

import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.utils
 * @ClassName: BluetoothUtils
 * @Description: 蓝牙工具
 * @Author: wyc
 * @CreateDate: 2021-08-18 13:54
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-08-18 13:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BluetoothUtils {
    public static final int REQUEST_BLUETOOTH__PERMISSIONS = 0xabc8;
    public static final int REQUEST_BLUETOOTH_ENABLE = 0X8888;
    public static void startBlueToothDiscovery(final Fragment fragment){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null )
            if (bluetoothAdapter.isEnabled()) {
                if (!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                }
            }else {
                MyDialog.displayAskMessage(fragment.getContext(), "蓝牙已关闭，是否开启蓝牙功能？", myDialog -> {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    fragment.startActivityForResult(enableBtIntent,REQUEST_BLUETOOTH_ENABLE);//请求开启蓝牙
                    myDialog.dismiss();
                }, Dialog::dismiss);
            }
        else
            MyDialog.ToastMessage("设备不支持蓝牙功能！", null);
    }
    public static void stopBlueToothDiscovery(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
        }
    }
    public static void bondBlueTooth(final String addr){
        if (Utils.isNotEmpty(addr)){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null){
                try {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(addr);
                    if (device.getBondState() == BluetoothDevice.BOND_NONE){
                        device.createBond();
                    }
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                    MyDialog.toastMessage(String.format(Locale.CHINA,"The address of bluetooth:%s,exception:%s",addr,e.getMessage()));
                }
            }
        }
    }

    public static void attachReceiver(@NonNull final Context context, @NonNull BroadcastReceiver receiver){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(receiver,intentFilter);
    }
    public static void detachReceiver(@NonNull final Context context, @NonNull BroadcastReceiver receiver){
        context.unregisterReceiver(receiver);
    }
}
