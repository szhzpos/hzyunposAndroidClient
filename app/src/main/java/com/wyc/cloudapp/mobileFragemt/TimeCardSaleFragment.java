package com.wyc.cloudapp.mobileFragemt;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.BasketView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.cashierDesk.TimeCardPayActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.MobileTimeCardSaleAdapter;
import com.wyc.cloudapp.adapter.business.TimeCardSaleAdapterBase;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.fragment.TimeCardSaleFragmentBase;
import com.wyc.cloudapp.utils.Utils;

import java.util.List;
import java.util.Locale;

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
    protected void dataChange(double num, double amt) {
        mBasketView.update(num);
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
