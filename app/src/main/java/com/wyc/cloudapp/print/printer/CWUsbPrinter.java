package com.wyc.cloudapp.print.printer;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;

import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.printer
 * @ClassName: CWUsbPrinter
 * @Description: 常旺收银机usb打印机
 * @Author: wyc
 * @CreateDate: 2022/3/11 11:47
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/3/11 11:47
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class CWUsbPrinter extends USBPrinter {
    @Override
    protected void print(UsbDeviceConnection connection, UsbEndpoint usbOutEndpoint, List<PrintItem> items, int footerSpace) throws UnsupportedEncodingException {
        for (PrintItem item : items){

            if (item.isBold()){
                connection.bulkTransfer(usbOutEndpoint, Printer.BOLD,Printer.BOLD.length, 100);
            }else connection.bulkTransfer(usbOutEndpoint,Printer.BOLD_CANCEL,Printer.BOLD_CANCEL.length, 100);

            if (item.isDoubleHigh() && item.isDoubleWidth()){
                connection.bulkTransfer(usbOutEndpoint,Printer.DOUBLE_HEIGHT_WIDTH,Printer.DOUBLE_HEIGHT_WIDTH.length, 100);
            }else if (item.isDoubleHigh()) {
                connection.bulkTransfer(usbOutEndpoint,Printer.DOUBLE_HEIGHT,Printer.DOUBLE_HEIGHT.length, 100);
            }else if (item.isDoubleWidth()){
                connection.bulkTransfer(usbOutEndpoint,Printer.DOUBLE_WIDTH,Printer.DOUBLE_WIDTH.length, 100);
            }else connection.bulkTransfer(usbOutEndpoint,Printer.NORMAL,Printer.NORMAL.length, 100);

            if (item.isNewline()){
                connection.bulkTransfer(usbOutEndpoint,Printer.NEW_LINE,Printer.NEW_LINE.length, 100);
                switch (item.getLineSpacing()){
                    case SPACING_2:
                    case SPACING_DEFAULT:
                        connection.bulkTransfer(usbOutEndpoint,Printer.LINE_SPACING_DEFAULT,Printer.LINE_SPACING_DEFAULT.length, 100);
                        break;
                    case SPACING_10:
                        connection.bulkTransfer(usbOutEndpoint,Printer.LINE_SPACING_10,Printer.LINE_SPACING_10.length, 100);
                        break;
                }
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
}
