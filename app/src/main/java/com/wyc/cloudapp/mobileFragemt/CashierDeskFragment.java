package com.wyc.cloudapp.mobileFragemt;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

public class CashierDeskFragment extends AbstractMobileFragment {
    public CashierDeskFragment(MainActivity activity) {
        super(activity);
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.mobile_cashier_desk_fragment_layout;
    }
    @Override
    protected int getMainLayoutId() {
        return R.id.main_linearLayout;
    }
}