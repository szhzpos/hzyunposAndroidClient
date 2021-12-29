package com.wyc.cloudapp.print.printer;

import androidx.annotation.NonNull;

import com.mt.retail.platform.printer.MtPrintResult;
import com.mt.retail.printapi.IMtPrintView;
import com.mt.retail.printapi.MtPrintApi;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.bean.PrintFormatInfo;
import com.wyc.cloudapp.print.receipts.IReceipts;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print
 * @ClassName: ToledoPrinter
 * @Description: 托利多 Plus U2 一体收银打印机
 * @Author: wyc
 * @CreateDate: 2021-12-24 14:35
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-24 14:35
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ToledoPrinter extends AbstractPrinter implements IMtPrintView {
    // -2001 	打印机未连接 	                内部异常，请联系MT
    // -2002 	打印机正忙 	                如打印机实际不在打印数据，且几秒后还不能恢复正常，请联系MT
    // -2003 	打印机缺纸 	                检查是否缺纸，如不缺则再做标定打印机缺纸传感器操作；如还不能恢复正常，请联系MT
    // -2004 	打印机过热 	                等待一会儿再打印，如不能解决请联系MT
    // -2005 	打印的标签未取走 	            取走打印口的标签或关闭取纸传感器；
    //	                                    如不能解决请联系MT
    // -2006 	非法打印头 	                确认打印机是否梅特勒-托利多正品备件
    // -2007 	打印头或打印舱门未关闭 	        检查打印头是否压紧打印纸；
    //	                                    检测打印机纸舱门是否关闭；
    // -2008 	切刀出错 	                更换打印机或联系MT
    // -2009 	切刀需要清洁 	                清洁打印机切刀刀片
    // -2010 	打印机需要标定打印纸 	        放入打印纸，执行标定打印机指令
    // -2011 	打印机电源异常 	            打印机供电电压过高或过低，联系MT
    // -2012 	预留打印机状态 	            暂无具体含意
    // -2013 	未授权标签打印 	             打印标签功能未激活，请联系MT

    public static final int RETURN_CODE_NOT_CONNECTED = -2001;
    public static final int RETURN_CODE_PRINTER_BUSY = -2002;
    public static final int RETURN_CODE_OUT_OF_PAPER = -2003;
    public static final int RETURN_CODE_PRINT_HEAD_HOT = -2004;
    public static final int RETURN_CODE_LABEL_NOT_TAKENUP = -2005;
    public static final int RETURN_CODE_UNKNOWN_PRTHEAD = -2006;
    public static final int RETURN_CODE_NOT_CLOSE_HEAD = -2007;
    public static final int RETURN_CODE_CUTTER_ERROR = -2008;
    public static final int RETURN_CODE_CUTTER_NEED_CLEAN = -2009;
    public static final int RETURN_CODE_PAPER_NEED_CALIBRATION = -2010;
    public static final int RETURN_CODE_POWER_NOT_GOOD = -2011;
    public static final int RETURN_CODE_RESERVED = -2012;
    public static final int RETURN_CODE_LABEL_DISABLED = -2013;

    // 0 	    命令执行成功或打印机空闲状态 	本返回码表示成功或可用，非错误。
    // -1 	    到MT称重或打印服务连接未建立 	是否重复建立通讯连接；
    //                                      是否误删除了MT的服务APP；
    //                                      其它，请联系MT
    // -2 	    到MT的远程通信异常出错 	        内部错误，请联系MT
    // -3 	    没有这个设备或接口 	        检查打印机类型是否正确；
    //                                      设备供电是否异常；
    //                                      请联系MT
    // -4 	    无效的参数 	                一般是参数范围出错；
    // -5 	    接口或设备未初始化完成 	        调用逻辑错误或内部错误，请联系MT
    // -6 	    收到设备表示失败或出错的应答 	内部错误，请联系MT
    // -7 	    通信失败 	                内部通信错误，请联系MT
    // -8 	    不支持的功能 	                当前的硬件类型不支持该功能
    // -9 	    命令的请求和应答不匹配 	        内部错误，重启秤后如不能恢复，请联系MT
    // -10 	    发送消息出错 	                内部错误，请联系MT
    // -11 	    权限不够 	                内部错误，请联系MT
    // -12 	    内部数据格式错误 	            内部错误，请联系MT
    // -13 	    未知错误 	                可能是JAVA层面函数异常；
    //                                      内部错误，请联系MT
    // -14 	    端口未准备好 	                读写操作时通信端口没打开，请联系MT

    public static final int RETURN_CODE_OK                    =  0;
    public static final int RETURN_CODE_SERVICE_UNCONNECTED  = -1;
    public static final int RETURN_CODE_REMOTE_COMM_ERROR   = -2;
    public static final int RETURN_CODE_DEVICE_NOT_EXIST = -3;
    public static final int RETURN_CODE_INVALID_INPUT_PARAMETER = -4;
    public static final int RETURN_CODE_DEVICE_UNINITIALIZE = -5;
    public static final int RETURN_CODE_DEVICE_RESPONSE_ERROR = -6;
    public static final int RETURN_CODE_INTERNAL_COMM_ERROR = -7;
    public static final int RETURN_CODE_DEVICE_NOT_SUPPORTED = -8;
    public static final int RETURN_CODE_COMMAND_RESPONSE_UNMATCHED = -9;
    public static final int RETURN_CODE_SEND_MESSAGE_ERROR = -10;
    public static final int RETURN_CODE_LIMIT_OF_ACCESS = -11;
    public static final int RETURN_CODE_DATA_FORMAT_ERROR = -12;
    public static final int RETURN_CODE_UNKNOWN_ERROR = -13;
    public static final int RETURN_CODE_PORT_NOT_READY = -14;

    private MtPrintApi mtPrintApi;

    private PrintFormatInfo mPrintFormatInfo;
    private List<PrintItem> ItemContent;

    public ToledoPrinter(){
        mtPrintApi = MtPrintApi.getInstance();
    }

    @Override
    public void onMtPrintServiceConnected() {

    }

    @Override
    public void onMtPrintServiceDisconnected() {
        MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.printer_offline));
    }

    /**
     * 打印机热插拔事件通知，其在打印机热插拔发生时发送通知，用于APP显示打印机中断或连接，其略早于onPrinterListChanged()回调，
     * @param printer 发生事件的打印机；
     * @param iEvent：0: 打印机printer被拔出；1：打印机printer被插入
     */
    @Override
    public void onPlugEvent(String printer, int iEvent) {
        switch (iEvent){
            case 0:
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.printer_out,printer));
                break;
            case 1:
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.printer_inserted,printer));
                break;
        }
    }

    @Override
    public void onPrinterListChanged(ArrayList<String> printerList) {
        if (mPrintFormatInfo != null && ItemContent != null && !ItemContent.isEmpty()){
            final MtPrintResult result = new MtPrintResult();
            mtPrintApi.setLeftMargin(40,result);
            int align = 0;
            for (PrintItem item : ItemContent){
                int fontSize = 24;
                if (item.getLineSpacing() == PrintItem.LineSpacing.SPACING_10){
                    mtPrintApi.setDefaultFontSize(14,result);
                    mtPrintApi.printBlankLine(1,result);
                }
                if (item.isDoubleHigh()){
                    fontSize = 32;
                }
                if (item.isBold()){
                    fontSize = 28;
                }
                switch (item.getAlign()){
                    case LEFT:
                        align = 0;
                        break;
                    case CENTRE:
                        align = 1;
                        break;
                    case RIGHT:
                        align = 2;
                        break;
                }
                mtPrintApi.printTextWithFontAndAlignment(item.getContent(),fontSize,align,result);
                LockSupport.parkNanos(1000 * 1000 * 20L);
            }
            mtPrintApi.setDefaultFontSize(18,result);
            mtPrintApi.printBlankLine(mPrintFormatInfo.getFooterSpace(),result);

            MyDialog.toastMessage(getErrorStr(result.getReturnCode()));
        }
    }

    @Override
    public void onFeedPaperFinished(int retCode) {
        MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.feedPaperFinished,getErrorStr(retCode)));
    }

    @Override
    public void onPrintFinished(int retCode) {
        MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.print_finished,getErrorStr(retCode)));
        clear();
    }

    public static String getErrorStr(int err) {
        String msg = "";
        switch(err)
        {
            case RETURN_CODE_OK:
                msg = CustomApplication.getStringByResId(R.string.success);
                break;
            case RETURN_CODE_SERVICE_UNCONNECTED :
                msg = ("Service not connected");
                break;
            case RETURN_CODE_REMOTE_COMM_ERROR :
                msg = ("Remote communication error");
                break;
            case RETURN_CODE_DEVICE_NOT_EXIST :
                msg = ("Device not exist");
                break;
            case RETURN_CODE_INVALID_INPUT_PARAMETER :
                msg = ("Invalid input parameter");
                break;
            case RETURN_CODE_DEVICE_UNINITIALIZE :
                msg = ("Device not initialized");
                break;
            case RETURN_CODE_DEVICE_RESPONSE_ERROR :
                msg = ("Device response error");
                break;
            case RETURN_CODE_INTERNAL_COMM_ERROR :
                msg = ("Internal communication error");
                break;
            case RETURN_CODE_DEVICE_NOT_SUPPORTED :
                msg = ("Device not supported");
                break;
            case RETURN_CODE_COMMAND_RESPONSE_UNMATCHED :
                msg = ("Command and response not matched");
                break;
            case RETURN_CODE_SEND_MESSAGE_ERROR :
                msg = ("Send message error");
                break;
            case RETURN_CODE_LIMIT_OF_ACCESS :
                msg = ("Device access limited");
                break;
            case RETURN_CODE_DATA_FORMAT_ERROR :
                msg = ("Data format error");
                break;
            case RETURN_CODE_UNKNOWN_ERROR :
                msg = ("Unkown error");
                break;
            case RETURN_CODE_NOT_CONNECTED :
                msg = ("Printer not connected");
                break;
            case RETURN_CODE_PRINTER_BUSY :
                msg = ("Printer is busy");
                break;
            case RETURN_CODE_OUT_OF_PAPER :
                msg = ("Printer is out of paper");
                break;
            case RETURN_CODE_PRINT_HEAD_HOT :
                msg = ("Printer head is too hotter");
                break;
            case RETURN_CODE_LABEL_NOT_TAKENUP :
                msg = ("Label not taken up");
                break;
            case RETURN_CODE_UNKNOWN_PRTHEAD :
                msg = ("Printer head is unknown");
                break;
            case RETURN_CODE_NOT_CLOSE_HEAD :
                msg = ("Printer head not closed");
                break;
            case RETURN_CODE_CUTTER_ERROR :
                msg = ("Printer cutter error");
                break;
            case RETURN_CODE_CUTTER_NEED_CLEAN :
                msg = ("Printer cutter need clean");
                break;
            case RETURN_CODE_PAPER_NEED_CALIBRATION :
                msg = ("Printer paper need calibration");
                break;
            case RETURN_CODE_POWER_NOT_GOOD :
                msg = ("Printer power not good");
                break;
            case RETURN_CODE_RESERVED :
                msg = ("Printer reserved error code");
                break;
            case RETURN_CODE_LABEL_DISABLED :
                msg = ("Printer label disabled error code");
                break;
            default:
                break;
        }
        return msg;
    }

    private void clear(){
        if (mtPrintApi != null){
            mtPrintApi.disconnectService(CustomApplication.self());
            try {
                final Field field = mtPrintApi.getClass().getDeclaredField("mIMtPrintView");
                field.setAccessible(true);
                field.set(mtPrintApi,null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            mtPrintApi = null;
        }
    }

    @Override
    public void printObj(@NonNull IReceipts receipts) {
        mPrintFormatInfo = receipts.getPrintFormat();
        ItemContent = receipts.getPrintItem();
        mtPrintApi.connectToService(CustomApplication.self(),this);
    }
}
