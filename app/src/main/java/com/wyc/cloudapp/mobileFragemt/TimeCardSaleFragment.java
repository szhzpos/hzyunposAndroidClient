package com.wyc.cloudapp.mobileFragemt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.BasketView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.cashierDesk.CardPayBaseActivity;
import com.wyc.cloudapp.activity.mobile.cashierDesk.SelectTimeCardActivity;
import com.wyc.cloudapp.activity.mobile.cashierDesk.TimeCardPayActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.MobileTimeCardSaleAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardData;
import com.wyc.cloudapp.bean.TimeCardInfo;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

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
public final class TimeCardSaleFragment extends AbstractMobileFragment {
    private VipInfo mVip;
    private MobileTimeCardSaleAdapter mSaleAdapter;
    private EditText _search_content;

    @BindView(R.id.basketView)
    BasketView mBasketView;
    @BindView(R.id.sale_amt_tv)
    TextView sale_amt_tv;
    @BindView(R.id.sale_man_tv)
    TextView sale_man_tv;
    @BindView(R.id.mobile_vip_btn)
    Button _vip_btn;

    @Override
    protected int getRootLayout() {
        return R.layout.time_card_sale_fragment;
    }

    @Override
    protected void viewCreated() {
        initSearchContent();
        initVipBtn();
        initSaleBtn();
        initSaleTimeCardAdapter();
        initCheckoutBtn();
    }

    @Override
    public String getTitle() {
        return CustomApplication.self().getString(R.string.once_card_sale_sz);
    }

    @OnClick(R.id._other_fun_btn)
    void other_func(){
        final Button _clear_btn = findViewById(R.id._clear_btn);
        if (_clear_btn.getVisibility() == View.GONE){
            _clear_btn.setVisibility(View.VISIBLE);
            if (!_clear_btn.hasOnClickListeners())
                _clear_btn.setOnClickListener(v -> {
                    if (!mSaleAdapter.isEmpty() && MyDialog.showMessageToModalDialog(mContext,"是否清空?") == 1){
                        mSaleAdapter.clear();
                    }
                });
        }else _clear_btn.setVisibility(View.GONE);
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

    public TimeCardSaleOrder disposeOrder(){
        return new TimeCardSaleOrder.Builder().vip_openid(mVip.getOpenid())
                .vip_card_no(mVip.getCard_code()).vip_name(mVip.getName()).vip_mobile(mVip.getMobile())
                .amt(Double.parseDouble(sale_amt_tv.getText().toString())).saleman(getSaleManId())
                .cas_id(mContext.getCashierId()).saleInfo(mSaleAdapter.getList()).build();
    }

    private void initSaleTimeCardAdapter(){
        final RecyclerView sale_once_card_list = findViewById(R.id.sale_once_card_list);
        mSaleAdapter = new MobileTimeCardSaleAdapter(mContext);
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
                mBasketView.update(num);
                sale_amt_tv.setText(String.format(Locale.CHINA,"%.2f",amt));
            }
        });
        sale_once_card_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        sale_once_card_list.addItemDecoration(new LinearItemDecoration(mContext.getColor(R.color.gray_subtransparent),3));
        sale_once_card_list.setAdapter(mSaleAdapter);
    }

    private void initSaleBtn(){
        final Button btn = findViewById(R.id.mobile_sale_man_btn);
        if (btn != null)
            btn.setOnClickListener(v -> setSaleman(AbstractVipChargeDialog.showSaleInfo(mContext)));
    }
    private String getSaleManId(){
        return Utils.getViewTagValue(sale_man_tv,"");
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

    public void clearVipInfo(){
        final ConstraintLayout mobile_bottom_btn_layout = findViewById(R.id.mobile_bottom_btn_layout);
        final TextView vip_name_tv = mobile_bottom_btn_layout.findViewById(R.id.vip_name);
        if (null != vip_name_tv){
            vip_name_tv.setText(getText(R.string.space_sz));
        }
        mVip = null;
    }

    public void setVipInfo(final VipInfo vip){
        final TextView vip_name_tv = findViewById(R.id.vip_name);
        assert vip_name_tv != null;
        if ( vip != null ){
            vip_name_tv.setText(vip.getName());
        }else vip_name_tv.setText("");
        mVip = vip;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.mobile_search_content);
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
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                queryTimeCardByName(search.getText().toString());
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    queryTimeCardByName(search.getText().toString());
                }else if(dx < search.getCompoundPaddingLeft()){
                    SelectTimeCardActivity.startWithFragment(this);
                }
            }
            return false;
        });
        _search_content = search;
    }

    private void queryTimeCardByName(String name) {
        final JSONObject object = new JSONObject();
        object.put("appid",mContext.getAppId());
        object.put("channel",1);
        if (Utils.isNotEmpty(name)){
            object.put("title",name);
        }
        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(mContext,getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(mContext.getUrl() + InterfaceURL.ONCE_CARD, HttpRequest.generate_request_parm(object,mContext.getAppSecret()))
                .enqueue(new ObjectCallback<TimeCardData>(TimeCardData.class,true) {
                    @Override
                    protected void onError(String msg) {
                        MyDialog.toastMessage(msg);
                        progressDialog.dismiss();
                    }

                    @Override
                    protected void onSuccessForResult(TimeCardData d, String hint) {
                        List<TimeCardInfo> list = d.getCard();
                        if (null != list){
                            int size = list.size();
                            if (size == 1){
                                add(list.get(0));
                            }else if (size == 0){
                                MyDialog.toastMessage(getString(R.string.not_exist_hint_sz, _search_content.getText().toString()));
                            }else
                                SelectTimeCardActivity.startForResult(mContext, (ArrayList<TimeCardInfo>) list);
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SelectTimeCardActivity.SELECT_ITEM){
                if (null != data){
                    add(SelectTimeCardActivity.getItem(data));
                }
            }else if (requestCode == CardPayBaseActivity.ONCE_CARD_REQUEST_PAY){
                clearContent();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void clearContent(){
        mSaleAdapter.clear();
        setVipInfo(null);
        setSaleman(null);
        _search_content.getText().clear();
    }
    private void setSaleman(JSONObject object){
        sale_man_tv.setTag(Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID));
        sale_man_tv.setText(Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_NAME));
    }
    private void add(TimeCardInfo info){
        final TimeCardSaleInfo saleInfo = new TimeCardSaleInfo.Builder()
                .timeCardId(info.getOnce_card_id())
                .name(info.getTitle())
                .price(info.getPrice())
                .num(1).build();
        mSaleAdapter.addTimeCard(saleInfo);
    }
}
