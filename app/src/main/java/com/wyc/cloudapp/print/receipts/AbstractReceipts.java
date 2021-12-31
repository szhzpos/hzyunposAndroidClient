package com.wyc.cloudapp.print.receipts;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.bean.PrintFormatInfo;
import com.wyc.cloudapp.utils.Utils;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: AbstractReceipts
 * @Description: 单据抽象父类
 * @Author: wyc
 * @CreateDate: 2021-12-29 13:10
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-29 13:10
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractReceipts implements IReceipts<PrintFormatInfo> {
    private final String mOrderCode;
    private final boolean isOpen;/*开钱箱*/
    private final PrintFormatInfo mPrintFormatInfo;

    protected AbstractReceipts(final PrintFormatInfo printFormatInfo,final String orderCode,boolean open){
        mPrintFormatInfo = printFormatInfo;
        mOrderCode = orderCode;
        isOpen = open;
    }

    @Override
    public final PrintFormatInfo getPrintFormat() {
        return mPrintFormatInfo;
    }
    protected static PrintFormatInfo formatInfo(final String id){
        final JSONObject print_format_info = new JSONObject();
        if (SQLiteHelper.getLocalParameter(id,print_format_info)){
            return print_format_info.toJavaObject(PrintFormatInfo.class);
        }else MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")));
        return null;
    }

    @Override
    public final List<PrintItem> getPrintItem() {
        List<PrintItem> printItem = null;
        final PrintFormatInfo mPrintFormatInfo = getPrintFormat();
        if (mPrintFormatInfo!= null && Utils.isNotEmpty(mOrderCode)){
            if (mPrintFormatInfo.getFormatId() == getFormatId()){
                switch (mPrintFormatInfo.getFormatSize()){
                    case R.id.f_58:
                        printItem = c_format_58(mPrintFormatInfo,mOrderCode);
                        break;
                    case R.id.f_76:
                        printItem = c_format_76(mPrintFormatInfo,mOrderCode);
                        break;
                    case R.id.f_80:
                        printItem = c_format_80(mPrintFormatInfo,mOrderCode);
                        break;
                }
            }else {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.f_not_sz));
            }
        }else MyDialog.toastMessage("打印单号不能为空！");
        return printItem;
    }
    @Override
    public final boolean isOpenCashBox(){
        return isOpen;
    }

    protected abstract List<PrintItem> c_format_58(@NonNull final PrintFormatInfo formatInfo,@NonNull final String orderCode);
    protected abstract List<PrintItem> c_format_76(@NonNull final PrintFormatInfo formatInfo,@NonNull final String orderCode);
    protected abstract List<PrintItem> c_format_80(@NonNull final PrintFormatInfo formatInfo,@NonNull final String orderCode);
    protected abstract int getFormatId();


    @Override
    protected void finalize() throws Throwable {
        Logger.d("%s has finished",getClass().getSimpleName());
    }
}
