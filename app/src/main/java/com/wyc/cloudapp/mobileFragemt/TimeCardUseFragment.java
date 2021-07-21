package com.wyc.cloudapp.mobileFragemt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.SelectTimeCardActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.VipTimeCardData;
import com.wyc.cloudapp.bean.VipTimeCardUseOrder;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ArrayCallback;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.mobileFragemt
 * @ClassName: TimeCardUseFragment
 * @Description: 次卡使用
 * @Author: wyc
 * @CreateDate: 2021-07-07 11:28
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-07 11:28
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class TimeCardUseFragment extends AbstractMobileFragment {
    private TimeCardInfoAdapter mAdapter;

    @BindView(R.id._vip_no_tv)
    TextView _vip_no_tv;
    @BindView(R.id._vip_name_tv)
    TextView _vip_name_tv;
    @BindView(R.id._vip_mobile_tv)
    TextView _vip_mobile_tv;
    @BindView(R.id._vip_grade_tv)
    TextView _vip_grade_tv;

    @Override
    protected int getRootLayout() {
        return R.layout.time_card_use_fragment;
    }

    @Override
    protected void viewCreated() {
        initSearchContent();
        initSaleTimeCardAdapter();
    }

    private void initSaleTimeCardAdapter(){
        final RecyclerView use_once_card_list = findViewById(R.id.use_time_card_list);
        mAdapter = new TimeCardInfoAdapter(mContext);
        assert use_once_card_list != null;
        use_once_card_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        use_once_card_list.addItemDecoration(new LinearItemDecoration(mContext.getColor(R.color.gray_subtransparent),3));
        use_once_card_list.setAdapter(mAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.search_et);
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
                queryVipTimeCard(search);
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    queryVipTimeCard(search);
                }
            }
            return false;
        });
    }

    private void queryVipTimeCard(EditText search){
        final String c = search.getText().toString();
        if (!Utils.isNotEmpty(c)){
            MyDialog.toastMessage(getString(R.string.not_empty_hint_sz,search.getHint()));
            return;
        }
        final JSONObject param = new JSONObject();
        param.put("appid",mContext.getAppId());
        param.put("members",c);
        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(mContext,getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(mContext.getUrl() + InterfaceURL.VIP_TIME_CARD, HttpRequest.generate_request_parm(param,mContext.getAppSecret()))
                .enqueue(new ObjectCallback<VipTimeCardData>(VipTimeCardData.class) {
                    @Override
                    protected void onError(String msg) {
                        progressDialog.dismiss();
                        MyDialog.toastMessage(msg);
                    }

                    @Override
                    protected void onSuccessForResult(VipTimeCardData d, String hint) {
                        if (null != d){
                            List<VipTimeCardData.VipTimeCardInfo> cardInfoList = d.getCard();
                            if (!cardInfoList.isEmpty()){
                                showVip(cardInfoList.get(0));
                            }
                            mAdapter.setDataForList(cardInfoList);
                        }else  mAdapter.setDataForList(null);
                        progressDialog.dismiss();
                        MyDialog.toastMessage(hint);
                    }
                });
    }

    private void showVip(@NonNull VipTimeCardData.VipTimeCardInfo cardInfo){
        _vip_no_tv.setText(cardInfo.getMemberCard());
        _vip_mobile_tv.setText(cardInfo.getMemberMobile());
        _vip_name_tv.setText(cardInfo.getMemberName());
        _vip_grade_tv.setText(cardInfo.getMemberGrade());
    }

    @Override
    public String getTitle() {
        return CustomApplication.self().getString(R.string.once_card_use);
    }

    static class TimeCardInfoAdapter extends AbstractDataAdapterForList<VipTimeCardData.VipTimeCardInfo,TimeCardInfoAdapter.MyViewHolder> implements View.OnClickListener{
        private final MainActivity mContext;
        public TimeCardInfoAdapter(MainActivity activity){
            mContext = activity;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = View.inflate(mContext, R.layout.time_card_use_adapter,null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final VipTimeCardData.VipTimeCardInfo cardInfo = getItem(position);
            if (cardInfo != null){
                holder.row_id.setText(String.format(Locale.CHINA,"%d、",position + 1));
                holder.card_no_tv.setText(String.valueOf(cardInfo.getNumber()));
                holder.card_name_tv.setText(cardInfo.getTitle());
                holder.residue_time_tv.setText(cardInfo.getAvailableLimit() == 1 ? "不限" : String.valueOf(cardInfo.getAvailable()));
                holder.use_limit_tv.setText(cardInfo.getSyLimitTypes());
                holder.usage_time_tv.setText(String.valueOf(cardInfo.getUsenum()));
                holder.valid_date_tv.setText(cardInfo.getValidityTypes());

                holder.sale_num.setText(String.valueOf(cardInfo.getSale_num()));

                holder._minus_btn.setTag(cardInfo);
                if (!holder._minus_btn.hasOnClickListeners()){
                    holder._minus_btn.setOnClickListener(this);
                }
                holder._plus_btn.setTag(cardInfo);
                if (!holder._plus_btn.hasOnClickListeners()){
                    holder._plus_btn.setOnClickListener(this);
                }
                holder.use_btn.setTag(cardInfo);
                if (!holder.use_btn.hasOnClickListeners()){
                    holder.use_btn.setOnClickListener(this);
                }
            }
        }

        @Override
        public void onClick(View v) {
            final Object o = v.getTag();
            if (o instanceof VipTimeCardData.VipTimeCardInfo){
                VipTimeCardData.VipTimeCardInfo cardInfo = (VipTimeCardData.VipTimeCardInfo)o;

                int id = v.getId();
                switch (id){
                    case R.id.use_btn:
                        sale(cardInfo);
                        break;
                    case R.id._plus_btn:
                        updateSaleNum(cardInfo,1);
                        break;
                    case R.id._minus_btn:
                        updateSaleNum(cardInfo, - 1);
                        break;
                }
            }
        }
        private void updateSaleNum(@NonNull VipTimeCardData.VipTimeCardInfo cardInfo,int num){
            if (cardInfo.setSale_num(cardInfo.getSale_num() + num)){
                notifyDataSetChanged();
            }
        }

        private void sale(@NonNull VipTimeCardData.VipTimeCardInfo cardInfo){
            final JSONObject param = new JSONObject();
            param.put("appid",mContext.getAppId());
            param.put("number",cardInfo.getNumber());
            param.put("stores_id",mContext.getStoreId());
            param.put("cas_id",mContext.getCashierId());
            param.put("use_num",cardInfo.getSale_num());

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(mContext,mContext.getString(R.string.dispose_hints));
            HttpUtils.sendAsyncPost(mContext.getUrl() + InterfaceURL.VIP_TIME_CARD_USE,HttpRequest.generate_request_parm(param,mContext.getAppSecret()))
                    .enqueue(new ArrayCallback<VipTimeCardUseOrder>(VipTimeCardUseOrder.class) {

                        @Override
                        protected void onSuccessForResult(@Nullable List<VipTimeCardUseOrder> d, String hint) {
                            MyDialog.toastMessage(hint);
                            if (null != d && !d.isEmpty()){
                                final VipTimeCardUseOrder order = d.get(0);
                                order.print(mContext);

                                {
                                    cardInfo.setSale_num(1);
                                    cardInfo.setUsenum(cardInfo.getUsenum() + order.getUseNum());
                                    if (cardInfo.getAvailableLimit() != 1)
                                        cardInfo.setAvailable(cardInfo.getAvailable() - order.getUseNum());

                                    notifyDataSetChanged();
                                }
                            }
                            progressDialog.dismiss();
                        }
                        @Override
                        protected void onError(String msg) {
                            MyDialog.toastMessage(msg);
                            progressDialog.dismiss();
                        }
                    });
        }

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
            @BindView(R.id.row_id)
            TextView row_id;
            @BindView(R.id.card_no_tv)
            TextView card_no_tv;
            @BindView(R.id.residue_time_tv)
            TextView residue_time_tv;
            @BindView(R.id.use_limit_tv)
            TextView use_limit_tv;
            @BindView(R.id.card_name_tv)
            TextView card_name_tv;
            @BindView(R.id.usage_time_tv)
            TextView usage_time_tv;
            @BindView(R.id.valid_date_tv)
            TextView valid_date_tv;

            @BindView(R.id.use_btn)
            Button use_btn;
            @BindView(R.id._plus_btn)
            Button _plus_btn;
            @BindView(R.id._minus_btn)
            Button _minus_btn;
            @BindView(R.id.sale_num)
            TextView sale_num;

            public MyViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }
    }

}
