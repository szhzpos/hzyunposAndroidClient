package com.wyc.cloudapp.mobileFragemt;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: OnceCardUseFragment
 * @Description: 次卡使用
 * @Author: wyc
 * @CreateDate: 2021-07-07 11:28
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-07 11:28
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class OnceCardUseFragment extends AbstractMobileFragment {
    @Override
    protected int getRootLayout() {
        return R.layout.once_card_use_fragment;
    }

    @Override
    protected void viewCreated() {

    }
    @Override
    public String getTitle() {
        return CustomApplication.self().getString(R.string.once_card_use);
    }
}
