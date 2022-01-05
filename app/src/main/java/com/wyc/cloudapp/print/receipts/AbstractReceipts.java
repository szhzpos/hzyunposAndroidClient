package com.wyc.cloudapp.print.receipts;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.parameter.SalePrintParameter;
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
public abstract class AbstractReceipts implements IReceipts<SalePrintParameter> {
    private final String mOrderCode;
    private final boolean isOpen;/*开钱箱*/
    private final SalePrintParameter mPrintFormatInfo;

    protected AbstractReceipts(final SalePrintParameter printFormatInfo, final String orderCode, boolean open){
        mPrintFormatInfo = printFormatInfo;
        mOrderCode = orderCode;
        isOpen = open;
    }

    @Override
    public final SalePrintParameter getPrintParameter() {
        return mPrintFormatInfo;
    }
    protected static SalePrintParameter formatInfo(final String id){
        final JSONObject print_format_info = new JSONObject();
        if (SQLiteHelper.getLocalParameter(id,print_format_info)){
            return print_format_info.toJavaObject(SalePrintParameter.class);
        }else MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")));
        return null;
    }

    @Override
    public final List<PrintItem> getPrintItem() {
        List<PrintItem> printItem = null;
        final SalePrintParameter mPrintFormatInfo = getPrintParameter();
        if (mPrintFormatInfo!= null && Utils.isNotEmpty(mOrderCode)){
            Logger.d("mPrintFormatInfo.getFormatId:%d,getFormatId:%d",mPrintFormatInfo.getFormatId(),getFormatId());
            if (mPrintFormatInfo.getFormatId() == getFormatId()){
                int size = mPrintFormatInfo.getFormatSize();
                Logger.d("getFormatSize:%d,f_58:%d,f_76:%d,f_80:%d",size,R.id.f_58,R.id.f_76,R.id.f_80);
                if (size == R.id.f_58){
                    printItem = c_format_58(mPrintFormatInfo,mOrderCode);
                }else if (size == R.id.f_76){
                    printItem = c_format_76(mPrintFormatInfo,mOrderCode);
                }else if (size == R.id.f_80){
                    printItem = c_format_80(mPrintFormatInfo,mOrderCode);
                }else MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.IllegalOrderSize));
            }else {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.f_not_sz));
            }
        }else MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.orderIdNotEmptyHint));
        return printItem;
    }
    @Override
    public final boolean isOpenCashBox(){
        return isOpen;
    }

    protected abstract List<PrintItem> c_format_58(@NonNull final SalePrintParameter formatInfo, @NonNull final String orderCode);
    protected abstract List<PrintItem> c_format_76(@NonNull final SalePrintParameter formatInfo, @NonNull final String orderCode);
    protected abstract List<PrintItem> c_format_80(@NonNull final SalePrintParameter formatInfo, @NonNull final String orderCode);
    protected abstract int getFormatId();


    @Override
    protected void finalize() throws Throwable {
        Logger.d("%s has finished",getClass().getSimpleName());
    }
}
