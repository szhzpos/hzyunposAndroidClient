package com.wyc.cloudapp.print.printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.parameter.IParameter;
import com.wyc.cloudapp.print.receipts.IReceipts;
import com.wyc.cloudapp.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print
 * @ClassName: CommandPrinter
 * @Description: 蓝牙或usb打印机
 * @Author: wyc
 * @CreateDate: 2021-12-28 15:48
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-28 15:48
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BluetoothPrinter extends AbstractPrinter {
    @Override
    public void printObj(@NonNull IReceipts<? extends IParameter> receipts) {
        bluetooth_print(receipts.getPrintParameter(),receipts.getPrintItem(),receipts.isOpenCashBox());
    }
    private void bluetooth_print(final IParameter format_info,List<PrintItem> items,boolean open){
        Logger.d("mPrintFormatInfo:%s,ItemContent:%s",format_info,items);
        boolean hasContent = format_info != null && items != null && !items.isEmpty();
        if (hasContent || open){

            final JSONObject object = new JSONObject();
            if (Printer.getPrinterSetting(object)){
                int status_id = object.getIntValue("id");
                final String[] vals = Utils.getNullStringAsEmpty(object,"v").split("\t");

                if (vals.length < 2)return;

                final String deviceAddr = vals[1];
                if (R.id.bluetooth_p == status_id && Utils.isNotEmpty(deviceAddr)){
                    MyDialog.toastMessage(CustomApplication.self().getString(R.string.begin_print));
                    String errMsg = "";
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter != null){
                        if (bluetoothAdapter.isEnabled()){
                            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddr);
                            synchronized (BluetoothPrinter.class){
                                try (BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                     BufferedOutputStream outputStream = new BufferedOutputStream(bluetoothSocket.getOutputStream())){
                                    bluetoothSocket.connect();

                                    if (hasContent){
                                        int footerSpace = format_info.getFooterSpace();
                                        int count = format_info.getPrintCount();
                                        while (count-- > 0){
                                            print(outputStream,items,footerSpace);
                                        }
                                        if (open)outputStream.write(Printer.OPEN_CASHBOX);
                                        printSuccess();
                                    }else {
                                        outputStream.write(Printer.OPEN_CASHBOX);
                                    }
                                    MyDialog.toastMessage(CustomApplication.self().getString(R.string.end_print));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    errMsg = "打印错误：" + e.getMessage();
                                }
                            }
                        }else{
                            errMsg = "蓝牙已关闭！";
                        }
                    }
                    showError(errMsg);
                }else showError(CustomApplication.getStringByResId(R.string.printer_error,deviceAddr));
            }else {
                showError(CustomApplication.getStringByResId(R.string.printer_error,object.getString("info")));
            }
        }
    }

    private void print(final BufferedOutputStream outputStream ,List<PrintItem> items,int footerSpace) throws IOException {
        for (PrintItem item : items){
            if (item.isBold()){
                outputStream.write(Printer.BOLD);
            }else outputStream.write(Printer.BOLD_CANCEL);

            if (item.isDoubleHigh() && item.isDoubleWidth()){
                outputStream.write(Printer.DOUBLE_HEIGHT_WIDTH);
            }else if (item.isDoubleHigh()) {
                outputStream.write(Printer.DOUBLE_HEIGHT);
            }else if (item.isDoubleWidth()){
                outputStream.write(Printer.DOUBLE_WIDTH);
            }else outputStream.write(Printer.NORMAL);

            if (item.isNewline()){
                outputStream.write(Printer.NEW_LINE);

                switch (item.getLineSpacing()){
                    case SPACING_2:
                        outputStream.write(Printer.LINE_SPACING_2);
                        break;
                    case SPACING_10:
                        outputStream.write(Printer.LINE_SPACING_10);
                        break;
                    case SPACING_DEFAULT:
                        outputStream.write(Printer.LINE_SPACING_DEFAULT);
                        break;
                }
            }

            switch (item.getAlign()){
                case LEFT:
                    outputStream.write(Printer.ALIGN_LEFT);
                    break;
                case CENTRE:
                    outputStream.write(Printer.ALIGN_CENTER);
                    break;
                case RIGHT:
                    outputStream.write(Printer.ALIGN_RIGHT);
                    break;
            }

            outputStream.write(item.getContent().getBytes(Printer.CHARACTER_SET));
            outputStream.flush();
            LockSupport.parkNanos(1000 * 1000 * 50L);
        }
        for (int i = 0; i < footerSpace; i++) {
            outputStream.write(Printer.NEW_LINE);
        }
        outputStream.write(Printer.CUT);
    }

    @Override
    public void openCashBox() {
        bluetooth_print(null,null,true);
    }

    @Override
    public void clear() {

    }
}
