package com.wyc.cloudapp.print.printer;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print
 * @ClassName: USBPrinter
 * @Description: USB打印机
 * @Author: wyc
 * @CreateDate: 2021-12-29 11:54
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-29 11:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class USBPrinter  extends AbstractPrinter {
    @Override
    public void printObj(@NonNull IReceipts<? extends IParameter> receipts) {
        usb_print_byte(receipts.getPrintParameter(),receipts.getPrintItem(),receipts.isOpenCashBox());
    }
    private void usb_print_byte(final IParameter format_info, List<PrintItem> items, boolean open){
        Logger.d("mPrintFormatInfo:%s,ItemContent:%s",format_info,items);
        boolean hasContent = format_info != null && items != null && !items.isEmpty();
        if (hasContent || open){
            MyDialog.toastMessage(CustomApplication.self().getString(R.string.begin_print));
            final JSONObject object = new JSONObject();
            if (Printer.getPrinterSetting(object)){
                int type_id = object.getIntValue("id");
                String tmp = Utils.getNullStringAsEmpty(object,"v");
                String[] vals = tmp.split("\t");
                if (type_id == R.id.usb_p && vals.length > 1){
                    final String vid = vals[0].substring(vals[0].indexOf(":") + 1);
                    final String pid = vals[1].substring(vals[1].indexOf(":") + 1);
                    synchronized (USBPrinter.class){
                        UsbDevice device = null;
                        UsbInterface usbInterface = null;
                        UsbEndpoint usbOutEndpoint = null,usbInEndpoint = null,tmpEndpoint;
                        UsbDeviceConnection connection = null;
                        UsbManager manager = (UsbManager)CustomApplication.self().getSystemService(Context.USB_SERVICE);
                        if (manager != null){
                            String msg = "";

                            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                            for(String sz:deviceList.keySet()){
                                device = deviceList.get(sz);
                                if (device != null){
                                    Logger.d("name:%s,--vid:%s,--pid:%s",device.getDeviceName(),vid,pid);
                                    Logger.d("name:%s,vid:%d,pid:%d",device.getDeviceName(),device.getVendorId(),device.getProductId());
                                    if (String.valueOf(device.getVendorId()).equals(vid) && String.valueOf(device.getProductId()).equals(pid)){
                                        break;
                                    }
                                }
                            }
                            if (null != device){
                                usbInterface = device.getInterface(0);
                                for(int i = 0,size = usbInterface.getEndpointCount();i < size; i++){
                                    tmpEndpoint = usbInterface.getEndpoint(i);
                                    if (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_OUT){
                                        usbOutEndpoint = tmpEndpoint;
                                    }else if (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_IN){
                                        usbInEndpoint = tmpEndpoint;
                                    }
                                }
                                if (usbOutEndpoint != null){
                                    connection = manager.openDevice(device);
                                    if (null != connection){
                                        if (connection.claimInterface(usbInterface, true)){
                                            try {
                                                connection.bulkTransfer(usbOutEndpoint,Printer.RESET,Printer.RESET.length, 100);

                                                if (hasContent){
                                                    int footerSpace = format_info.getFooterSpace();
                                                    int count = format_info.getPrintCount();
                                                    while (count -- > 0){
                                                        print(connection,usbOutEndpoint,items,footerSpace);
                                                    }
                                                    printSuccess();
                                                    if (open)connection.bulkTransfer(usbOutEndpoint,Printer.OPEN_CASHBOX,Printer.OPEN_CASHBOX.length, 100);
                                                }else {
                                                    connection.bulkTransfer(usbOutEndpoint,Printer.OPEN_CASHBOX,Printer.OPEN_CASHBOX.length, 100);
                                                }
                                                MyDialog.toastMessage(CustomApplication.self().getString(R.string.end_print));
                                            } catch (UnsupportedEncodingException e) {
                                                msg = e.getMessage();
                                            } finally {
                                                connection.releaseInterface(usbInterface);
                                                connection.close();
                                            }
                                        }else{
                                            msg = "独占访问打印机错误！";
                                        }
                                    }else{
                                        msg = "打开打印机连接错误！";
                                    }
                                }else{
                                    msg = "未找到USB输出端口！";
                                }
                            }else{
                                msg = "未找到打印机设备！";
                            }
                            showError(msg);
                        }
                    }
                }else showError(CustomApplication.getStringByResId(R.string.printer_error, Arrays.toString(vals)));
            }else {
                showError(CustomApplication.getStringByResId(R.string.printer_error,object.getString("info")));
            }
        }
    }
    private void print(UsbDeviceConnection connection,UsbEndpoint usbOutEndpoint,List<PrintItem> items,int footerSpace) throws UnsupportedEncodingException {
        for (PrintItem item : items){

            if (item.isBold()){
                connection.bulkTransfer(usbOutEndpoint,Printer.BOLD,Printer.BOLD.length, 100);
            }else connection.bulkTransfer(usbOutEndpoint,Printer.BOLD_CANCEL,Printer.BOLD_CANCEL.length, 100);

            if (item.isDoubleHigh() && item.isDoubleWidth()){
                connection.bulkTransfer(usbOutEndpoint,Printer.DOUBLE_HEIGHT_WIDTH,Printer.DOUBLE_HEIGHT_WIDTH.length, 100);
            }else if (item.isDoubleHigh()) {
                connection.bulkTransfer(usbOutEndpoint,Printer.DOUBLE_HEIGHT,Printer.DOUBLE_HEIGHT.length, 100);
            }else if (item.isDoubleWidth()){
                connection.bulkTransfer(usbOutEndpoint,Printer.DOUBLE_WIDTH,Printer.DOUBLE_WIDTH.length, 100);
            }else connection.bulkTransfer(usbOutEndpoint,Printer.NORMAL,Printer.NORMAL.length, 100);

            if (item.isNewline()){
                switch (item.getLineSpacing()){
                    case SPACING_2:
                    case SPACING_DEFAULT:
                        connection.bulkTransfer(usbOutEndpoint,Printer.LINE_SPACING_DEFAULT,Printer.LINE_SPACING_DEFAULT.length, 100);
                        break;
                    case SPACING_10:
                        connection.bulkTransfer(usbOutEndpoint,Printer.LINE_SPACING_48,Printer.LINE_SPACING_48.length, 100);
                        break;
                }

                connection.bulkTransfer(usbOutEndpoint,Printer.NEW_LINE,Printer.NEW_LINE.length, 100);
            }

            switch (item.getAlign()){
                case LEFT:
                    connection.bulkTransfer(usbOutEndpoint,Printer.ALIGN_LEFT,Printer.ALIGN_LEFT.length, 100);
                    break;
                case CENTRE:
                    connection.bulkTransfer(usbOutEndpoint,Printer.ALIGN_CENTER,Printer.ALIGN_CENTER.length, 100);
                    break;
                case RIGHT:
                    connection.bulkTransfer(usbOutEndpoint,Printer.ALIGN_RIGHT,Printer.ALIGN_RIGHT.length, 100);
                    break;
            }

            final byte[] content = item.getContent().getBytes(Printer.CHARACTER_SET);
            connection.bulkTransfer(usbOutEndpoint,content,content.length, 100);
        }
        for (int i = 0; i < footerSpace; i++) {
            connection.bulkTransfer(usbOutEndpoint,Printer.NEW_LINE,Printer.NEW_LINE.length, 100);
        }
        connection.bulkTransfer(usbOutEndpoint,Printer.CUT,Printer.CUT.length, 100);
    }

    @Override
    public void openCashBox() {
        usb_print_byte(null,null,true);
    }

    @Override
    public void clear() {

    }
}
