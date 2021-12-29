package com.wyc.cloudapp.print.receipts;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.bean.PrintFormatInfo;
import com.wyc.cloudapp.print.bean.SaleOrderPrintInfo;

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
public abstract class AbstractReceipts implements IReceipts {
    private final PrintFormatInfo mPrintFormatInfo;
    private List<PrintItem> mPrintItem;
    public AbstractReceipts(final String orderCode){
        mPrintFormatInfo = PrintFormatInfo.getFormatInfo();
        if (mPrintFormatInfo!= null){
            if (mPrintFormatInfo.getFormatId() == getFormatId()){
                final SaleOrderPrintInfo saleOrderPrintInfo = SaleOrderPrintInfo.getInstance(orderCode);
                if (saleOrderPrintInfo != null){
                    switch (getPrintFormat().getFormatSize()){
                        case R.id.f_58:
                            mPrintItem = c_format_58(mPrintFormatInfo,saleOrderPrintInfo);
                            break;
                        case R.id.f_76:
                            mPrintItem = c_format_76(mPrintFormatInfo,saleOrderPrintInfo);
                            break;
                        case R.id.f_80:
                            mPrintItem = c_format_80(mPrintFormatInfo,saleOrderPrintInfo);
                            break;
                    }
                }
            }else {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.f_not_sz));
            }
        }
    }
    @Override
    public final PrintFormatInfo getPrintFormat() {
        return mPrintFormatInfo;
    }

    @Override
    public final List<PrintItem> getPrintItem() {
        return mPrintItem;
    }

    protected abstract List<PrintItem> c_format_58(final PrintFormatInfo format_info, final SaleOrderPrintInfo order_info);
    protected abstract List<PrintItem> c_format_76(final PrintFormatInfo format_info, final SaleOrderPrintInfo order_info);
    protected abstract List<PrintItem> c_format_80(final PrintFormatInfo format_info, final SaleOrderPrintInfo order_info);
    protected abstract int getFormatId();

    @Override
    protected void finalize() throws Throwable {
        Logger.d("%s has finished",getClass().getSimpleName());
    }
}
