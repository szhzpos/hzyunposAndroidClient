package com.wyc.cloudapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.method.ReplacementTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.cashierDesk.CardPayBaseActivity;
import com.wyc.cloudapp.activity.mobile.cashierDesk.SelectTimeCardActivity;
import com.wyc.cloudapp.activity.mobile.cashierDesk.TimeCardPayActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.TimeCardSaleAdapterBase;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardInfo;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
import com.wyc.cloudapp.data.viewModel.TimeCardViewModel;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.mobileFragemt.AbstractMobileFragment;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      TimeCardSaleFragmentBase
 * @Description:    次卡销售基类
 * @Author:         wyc
 * @CreateDate:     2021-10-19 14:15
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-19 14:15
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract public class TimeCardSaleFragmentBase extends AbstractMobileFragment {
    private VipInfo mVip;
    private TimeCardSaleAdapterBase<? extends AbstractDataAdapter.SuperViewHolder> mSaleAdapter;
    private EditText _search_content;

    @BindView(R.id.sale_man_tv)
    TextView sale_man_tv;

    @BindView(R.id._vip_btn)
    Button _vip_btn;

    @BindView(R.id.sale_amt_tv)
    TextView sale_amt_tv;

    protected abstract TimeCardSaleAdapterBase<? extends AbstractDataAdapter.SuperViewHolder> getSaleAdapter();
    protected abstract void cardDataChange(List<TimeCardInfo> data);

    @CallSuper
    @Override
    protected void viewCreated() {
        initSaleTimeCardAdapter();
        initSearchContent();
        initSaleBtn();
        initVipBtn();
        initCheckoutBtn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode , Intent data ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SelectTimeCardActivity.SELECT_ITEM) {
                if (null != data) {
                    add(SelectTimeCardActivity.getItem(data));
                }
            } else if (requestCode == CardPayBaseActivity.ONCE_CARD_REQUEST_PAY) {
                clearContent();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String getTitle() {
        return CustomApplication.self().getString(R.string.once_card_sale_sz);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_search_content != null){
            _search_content.postDelayed(()->_search_content.requestFocus(),150);
        }
    }

    protected void clearData(){
        if (!mSaleAdapter.isEmpty() && MyDialog.showMessageToModalDialog(mContext,"是否清空?") == 1){
            mSaleAdapter.clear();
        }
    }

    private void initSaleTimeCardAdapter(){
        final RecyclerView sale_once_card_list = findViewById(R.id.sale_once_card_list);
        mSaleAdapter = getSaleAdapter();
        mSaleAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                int num = 0;
                double amt = 0.0;
                List<TimeCardSaleInfo> saleInfoList = mSaleAdapter.getList();
                if (null != saleInfoList && saleInfoList.size() != 0){
                    for (TimeCardSaleInfo info : saleInfoList){
                        num += info.getNum();
                        amt += info.getAmt();
                    }
                }
                soldDataChange(num,amt);
            }
        });
        sale_once_card_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        sale_once_card_list.addItemDecoration(new LinearItemDecoration(mContext.getColor(R.color.gray_subtransparent),3));
        sale_once_card_list.setAdapter(mSaleAdapter);
    }

    protected void soldDataChange(double num, double amt){
        sale_amt_tv.setText(String.format(Locale.CHINA,"%.2f",amt));
    }

    private void initCheckoutBtn(){
        final Button onceCard_checkout_btn = findViewById(R.id.onceCard_checkout_btn);
        onceCard_checkout_btn.setOnClickListener(v -> {
            if (mVip != null){
                TimeCardPayActivity.start(this,disposeOrder());
            }else {
                _vip_btn.callOnClick();
            }
        });
    }

    protected void clearContent() {
        mSaleAdapter.clear();
        setVipInfo(null);
        setSaleman(null);
        _search_content.getText().clear();
    }

    @CallSuper
    protected void setSaleman(JSONObject obj) {
        sale_man_tv.setTag(Utils.getNullStringAsEmpty(obj, TreeListBaseAdapter.COL_ID));
        sale_man_tv.setText(Utils.getNullStringAsEmpty(obj, TreeListBaseAdapter.COL_NAME));
    }

    private void initVipBtn(){
        _vip_btn.setOnClickListener(v -> {
            final VipInfoDialog vipInfoDialog = new VipInfoDialog(mContext);
            if (mVip != null){
                if (1 == MyDialog.showMessageToModalDialog(mContext,"已存在会员信息,是否清除？")){
                    clearVipInfo();
                }
            }else
                vipInfoDialog.setYesOnclickListener(dialog -> {
                    setVipInfo(dialog.getVipBean());dialog.dismiss(); }).show();
        });
    }
    @CallSuper
    public void setVipInfo(VipInfo vip) {
        final TextView tv = findViewById(R.id.vip_name);
        if (tv != null){
            if ( vip != null ){
                tv.setText(vip.getName());
            }else {
                tv.setText("");
            }
        }
        mVip = vip;
    }

    @CallSuper
    public void clearVipInfo(){
        final TextView vip_name_tv = findViewById(R.id.vip_name);
        if (null != vip_name_tv){
            vip_name_tv.setText(getText(R.string.space_sz));
        }
        mVip = null;
    }

    private void initSaleBtn(){
        final Button btn = findViewById(R.id.sale_man_btn);
        if (btn != null)
            btn.setOnClickListener(v -> setSaleman(AbstractVipChargeDialog.showSaleInfo(mContext)));
    }
    private String getSaleManId(){
        return Utils.getViewTagValue(sale_man_tv,"");
    }

    public TimeCardSaleOrder disposeOrder(){
        return new TimeCardSaleOrder.Builder().vip_openid(mVip.getOpenid())
                .vip_card_no(mVip.getCard_code()).vip_name(mVip.getName()).vip_mobile(mVip.getMobile())
                .amt(Double.parseDouble(sale_amt_tv.getText().toString())).saleman(getSaleManId())
                .cas_id(mContext.getCashierId()).saleInfo(mSaleAdapter.getList()).build();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id._search_content);
        assert search != null;
        search.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                return new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            }

            @Override
            protected char[] getReplacement() {
                return new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            }
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    queryTimeCardByName(search.getText().toString());
                }else if(dx < search.getCompoundPaddingLeft()){
                    SelectTimeCardActivity.startWithFragment(this,null);
                }
            }
            return false;
        });
        _search_content = search;
    }

    @Override
    public boolean hookEnterKey() {
        if (_search_content != null && _search_content.hasFocus()){
            queryTimeCardByName(_search_content.getText().toString());
            return true;
        }
        return false;
    }
    protected void queryTimeCardByName(String name) {
        final MutableLiveData<List<TimeCardInfo>> liveData = new ViewModelProvider(this).get(TimeCardViewModel.class).refresh(mContext,name);
        if (!liveData.hasActiveObservers()){
            liveData.observe(this, list -> {
                if (null != list){
                    int size = list.size();
                    if (size == 1){
                        add(list.get(0));
                    }else if (size == 0){
                        MyDialog.toastMessage(getString(R.string.not_exist_hint_sz, _search_content.getText().toString()));
                    }else
                        cardDataChange(list);
                }
            });
        }
    }

    protected void add(TimeCardInfo info ) {
        final TimeCardSaleInfo saleInfo = new TimeCardSaleInfo.Builder().timeCardId(info.getOnce_card_id())
                .name(info.getTitle())
                .price(info.getPrice())
                .num(1).build();
        mSaleAdapter.addTimeCard(saleInfo);
    }
    protected void addNum(int n){
        mSaleAdapter.modifyCurrentNum(n);
    }
}