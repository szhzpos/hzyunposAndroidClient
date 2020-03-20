package com.wyc.cloudapp.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Looper;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;

/**
 * Created by Administrator on 2018-05-04.
 */

public class NetChangeMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
       if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction()) || "confirm_connection".equals(intent.getAction())){
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
}
