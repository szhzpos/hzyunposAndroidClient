package com.wyc.cloudapp.print;

import androidx.annotation.NonNull;

import com.mt.retail.platform.printer.MtPrintResult;
import com.mt.retail.printapi.IMtPrintView;
import com.mt.retail.printapi.MtPrintApi;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

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
    private MtPrintApi mtPrintApi;
    private String mContent = "";
    public ToledoPrinter(){
        mtPrintApi = MtPrintApi.getInstance();
        mtPrintApi.connectToService(CustomApplication.self(),this);
    }

    @Override
    public void print(@NonNull String c) {
        mContent = c;
    }

    @Override
    public void onMtPrintServiceConnected() {
         Logger.d("连接打印机");
        final MtPrintResult result = new MtPrintResult();
        if (!mtPrintApi.printText(mContent,result)){
            MyDialog.toastMessage(result.getMsg());
        }
        Logger.d("msg:%s,code:%d",result.getMsg(),result.getReturnCode());
    }

    @Override
    public void onMtPrintServiceDisconnected() {
        Logger.d("断开打印机");
    }

    @Override
    public void onPlugEvent(String printer, int iEvent) {
        Logger.d("printer：%s,iEvent:%d",printer,iEvent);
    }

    @Override
    public void onPrinterListChanged(ArrayList<String> printerList) {
        Logger.d("printerList：%s", Arrays.toString(printerList.toArray()));
    }

    @Override
    public void onFeedPaperFinished(int retCode) {
        Logger.d("走纸完成");
    }

    @Override
    public void onPrintFinished(int retCode) {
        clear();
    }
    private void clear(){
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
