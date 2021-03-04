package com.wyc.cloudapp.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.fragment.PrintFormatFragment;

/**
 * Created by Administrator on 2018-05-04.
 */

public class GlobalBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null){
            switch (action){
                case "android.net.conn.CONNECTIVITY_CHANGE":
                case "confirm_connection":
                    network_status(context);
                    break;
                case "android.intent.action.BOOT_COMPLETED":
                    LoginActivity.start(context);
                    break;
                case "android.hardware.usb.action.USB_DEVICE_ATTACHED":
                    synchronized (this) {
                        UsbDevice device =  intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        UsbManager manager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
                        if (null != manager && !manager.hasPermission(device)){
                            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(PrintFormatFragment.ACTION_USB_PERMISSION), 0);
                            manager.requestPermission(device, permissionIntent);
                        }
                    }
                    break;
            }
        }
    }


    private void network_status(final Context context){
        final CustomApplication application = (CustomApplication)context.getApplicationContext();
        final ConnectivityManager  connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);
        final Notification mNotification = builder
                .setContentTitle("")
                .setSmallIcon(R.drawable.dialog_icon)
                .build();
        if (connectivityManager != null && mNotificationManager != null){
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks.length != 0) {
                for (Network tmp : networks) {
                    final NetworkInfo networkInfo = connectivityManager.getNetworkInfo(tmp);
                    if (networkInfo != null) {
                        switch (networkInfo.getState()) {
                            case CONNECTED:
                                break;
                            default:
                                builder.setContentTitle("网络状态");
                                builder.setContentText("无线网已断开,进入离线模式！");
                                mNotificationManager.notify(1, mNotification);
                                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                                    application.setNetState_mobile(0);
                                else
                                    application.setNetState(0);
                                break;

                        }

                    }
                }
            }else {
                builder.setContentTitle("网络状态");
                builder.setContentText("无线网已断开,进入离线模式！");
                mNotificationManager.notify(1, mNotification);
                application.setNetState_mobile(0);
                application.setNetState(0);
            }
        }
    }
}
