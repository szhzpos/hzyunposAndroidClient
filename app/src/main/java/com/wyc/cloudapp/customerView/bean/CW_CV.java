package com.wyc.cloudapp.customerView.bean;

import com.pavolibrary.commands.DspAPI;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customerView.ICustomerView;

import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.customerView
 * @ClassName: CW_CS
 * @Description: 常旺收银机客显
 * @Author: wyc
 * @CreateDate: 2022/4/19 10:34
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/4/19 10:34
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class CW_CV implements ICustomerView {
    private final DspAPI mDsp;
    public CW_CV(final String port,Integer rate){
        mDsp = new DspAPI(CustomApplication.self());
        mDsp.connect(port,rate,0);
    }

    @Override
    public void writPrice(double price) {
        if (mDsp.isConnect()){
            mDsp.DSP_Dispay(String.format(Locale.CHINA,"%.2f",price),"ASCII");
        }
    }

    @Override
    public void writRealPay(double amt) {
        if (mDsp.isConnect()){
            mDsp.DSP_Dispay(String.format(Locale.CHINA,"%.2f",amt),"ASCII");
        }
    }

    @Override
    public void writChange(double amt) {
        if (mDsp.isConnect()){
            mDsp.DSP_Dispay(String.format(Locale.CHINA,"%.2f",amt),"ASCII");
        }
    }

    @Override
    public void clear() {
        if (mDsp != null){
            mDsp.disconnect();
        }
    }
}
