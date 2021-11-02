package com.wyc.cloudapp.mobileFragemt;

import android.view.View;
import android.widget.Button;

import com.wyc.cloudapp.customizationView.BasketView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.cashierDesk.SelectTimeCardActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileTimeCardSaleAdapter;
import com.wyc.cloudapp.adapter.business.TimeCardSaleAdapterBase;
import com.wyc.cloudapp.bean.TimeCardInfo;
import com.wyc.cloudapp.fragment.TimeCardSaleFragmentBase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: TimeCardSaleFragment
 * @Description: 次卡销售
 * @Author: wyc
 * @CreateDate: 2021-07-07 11:05
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-07 11:05
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class TimeCardSaleFragment extends TimeCardSaleFragmentBase {
    @BindView(R.id.basketView)
    BasketView mBasketView;
    @Override
    protected int getRootLayout() {
        return R.layout.time_card_sale_fragment;
    }

    @Override
    protected TimeCardSaleAdapterBase<? extends AbstractDataAdapter.SuperViewHolder> getSaleAdapter() {
        return new MobileTimeCardSaleAdapter(mContext);
    }
    @Override
    protected void soldDataChange(double num, double amt) {
        mBasketView.update(num);
    }

    @Override
    protected void cardDataChange(List<TimeCardInfo> data) {
        SelectTimeCardActivity.startWithFragment(this, (ArrayList<TimeCardInfo>) data);
    }

    @OnClick(R.id._other_fun_btn)
    void other_func(){
        final Button _clear_btn = findViewById(R.id._clear_btn);
        if (_clear_btn.getVisibility() == View.GONE){
            _clear_btn.setVisibility(View.VISIBLE);
            if (!_clear_btn.hasOnClickListeners())
                _clear_btn.setOnClickListener(v -> clearData());
        }else _clear_btn.setVisibility(View.GONE);
    }
}
