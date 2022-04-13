package com.wyc.cloudapp.design;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.io.BluetoothPort;
import com.gprinter.io.EthernetPort;
import com.gprinter.io.PortManager;
import com.gprinter.io.SerialPort;
import com.gprinter.io.UsbPort;
import com.gprinter.utils.CallbackListener;
import com.gprinter.utils.Command;
import com.gprinter.utils.ConnMethod;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.BluetoothUtils;

import java.io.IOException;
import java.util.Vector;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GPPrinter implements CallbackListener {
    private static GPPrinter printer=null;
    private static PortManager portManager=null;
    public GPPrinter(){
    }
    /**
     * 单例
     * @return
     */
    public static GPPrinter getInstance(){
       if (printer==null){
           printer=new GPPrinter();
       }
       return printer;
    }

    /**
     * 获取连接状态
     * @return
     */
    public static boolean getConnectState(){
        return portManager.getConnectStatus();
    }

    /**
     * 连接
     * @param devices
     */
    public static void connect(final PrinterDevices devices){
        CustomApplication.execute(()->{
            if (portManager!=null) {//先close上次连接
                portManager.closePort();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
            if (devices!=null) {
                switch (devices.getConnMethod()) {
                    case BLUETOOTH://蓝牙
                        portManager = new BluetoothPort(devices);
                        portManager.openPort();
                        break;
                    case USB://USB
                        portManager = new UsbPort(devices);
                        portManager.openPort();
                        break;
                    case WIFI://WIFI
                        portManager = new EthernetPort(devices);
                        portManager.openPort();
                        break;
                    case SERIALPORT://串口
                        portManager=new SerialPort(devices);
                        portManager.openPort();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * @param mac 蓝牙设备地址
     * 打开蓝牙设备
     * */
    public static void openBlueTooth(final String mac){
        Logger.d("connecting printer. mac is %s",mac);
        final PrinterDevices blueTooth = new PrinterDevices.Build()
                .setContext(CustomApplication.self())
                .setConnMethod(ConnMethod.BLUETOOTH)
                .setMacAddress(mac)
                .setCommand(Command.TSC)
                .setCallbackListener(GPPrinter.getInstance()).build();
        connect(blueTooth);
    }

    /**
     * @param mac 蓝牙设备地址
     * @param callbackListener 打印机状态回调
     * 打开蓝牙设备
     * */
    public static void openBlueTooth(final String mac,CallbackListener callbackListener){
        Logger.d("connecting printer. mac is %s",mac);
        final PrinterDevices blueTooth = new PrinterDevices.Build()
                .setContext(CustomApplication.self())
                .setConnMethod(ConnMethod.BLUETOOTH)
                .setMacAddress(mac)
                .setCommand(Command.TSC)
                .setCallbackListener(callbackListener).build();
        connect(blueTooth);
    }


    /**
     * 发送数据到打印机 字节数据
     * @param vector
     * @return true发送成功 false 发送失败
     * 打印机连接异常或断开发送时会抛异常，可以捕获异常进行处理
     */
    public static void sendDataToPrinter(byte [] vector) {
        if (portManager==null){
            MyDialog.toastMessage(R.string.printer_not_init);
        }
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean result = portManager.writeDataImmediately(vector);
            if (!result){
                printer.showPrinterStatusDescription(portManager.getPrinterStatus(portManager.getCommand()));
            }else
                emitter.onNext(true);
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(t -> MyDialog.toastMessage(R.string.send_success),
                throwable -> MyDialog.toastMessage(throwable.getMessage()));
    }

    /**
     * 获取打印机状态
     * @param printerCommand 打印机命令 ESC为小票，TSC为标签 ，CPCL为面单
     * @return 返回值常见文档说明
     * @throws IOException
     */
    public static int getPrinterState(Command printerCommand, long delayMillis)throws IOException {
        if (portManager == null)return -1;
        return portManager.getPrinterStatus(printerCommand);
    }

    /**
     * 获取打印机电量
     * @return
     * @throws IOException
     */
    public static int getPower() throws IOException {
        if (portManager == null)return -1;
        return portManager.getPower();
    }

    /**
     * 设置使用指令
     * @param printerCommand
     */
    public static void setPrinterCommand(Command printerCommand){
        if (portManager==null){
            return;
        }
        portManager.setCommand(printerCommand);
    }
    /**
     * 发送数据到打印机 指令集合内容
     * @param vector

     * 打印机连接异常或断开发送时会抛异常，可以捕获异常进行处理
     */
    public static void sendDataToPrinter(Vector<Byte> vector) throws IOException {
        if (portManager==null){
            MyDialog.toastMessage(R.string.printer_not_init);
            return;
        }
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            boolean result = portManager.writeDataImmediately(vector);
            if (!result){
                printer.showPrinterStatusDescription(portManager.getPrinterStatus(portManager.getCommand()));
            }else
                emitter.onNext(true);
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(t -> MyDialog.toastMessage(R.string.send_success),
                throwable -> MyDialog.toastMessage(throwable.getMessage()));
    }

    private void showPrinterStatusDescription(int status){
        if (status==-1){
             MyDialog.toastMessage(R.string.status_fail);
        }else if (status==1){
            MyDialog.toastMessage(R.string.status_feed);
        }else if (status==0){
            MyDialog.toastMessage(R.string.status_normal);
        }else if (status==-2){
            MyDialog.toastMessage(R.string.status_out_of_paper);
        }else if (status==-3){
            MyDialog.toastMessage(R.string.status_open);
        }else if (status==-4){
            MyDialog.toastMessage(R.string.status_overheated);
        }
    }

    /**
     * 关闭连接
     * @return
     */
    public static void close(){
        if (portManager!=null){
             portManager.closePort();
             portManager=null;
        }
        if (printer != null){
            printer = null;
        }
    }

    @Override
    public void onConnecting() {
        MyDialog.toastMessage(R.string.printer_connecting);
    }

    @Override
    public void onCheckCommand() {
        Logger.d("onCheckCommand");
    }

    @Override
    public void onSuccess(PrinterDevices printerDevices) {
        MyDialog.toastMessage(R.string.conn_success);
    }

    @Override
    public void onReceive(byte[] bytes) {
        Logger.d("onReceive");
    }

    @Override
    public void onFailure() {
        MyDialog.toastMessage(R.string.conn_fail);
    }

    @Override
    public void onDisconnect() {
        MyDialog.toastMessage(R.string.printer_disconnect);
    }
}
