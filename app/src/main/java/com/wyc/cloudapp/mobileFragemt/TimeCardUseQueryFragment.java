package com.wyc.cloudapp.mobileFragemt;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: TimeCardUseQueryFragment
 * @Description: 次卡使用查询
 * @Author: wyc
 * @CreateDate: 2021-07-12 14:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-12 14:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TimeCardUseQueryFragment extends AbstractMobileFragment {
    @Override
    protected int getRootLayout() {
        return R.layout.time_card_use_query_fragment;
    }

    @Override
    protected void viewCreated() {

    }
    @Override
    public String getTitle(){
        return CustomApplication.self().getString(R.string.once_card_use) + CustomApplication.self().getString(R.string.query_sz);
    }
}
