package com.wyc.cloudapp.mobileFragemt;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.QueryCondition;
import com.wyc.cloudapp.bean.VipTimeCardUseOrder;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ArrayCallback;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

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
public class TimeCardUseQueryFragment extends AbstractTimeCardQueryFragment {

    @Override
    protected AbstractDataAdapterForList<?,? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
        return new UseOrderAdapter(mContext);
    }

    @Override
    protected void query(@NonNull QueryCondition condition) {
        final JSONObject param = new JSONObject();

        param.put("appid",mContext.getAppId());
        final String content = condition.getCondition();
        if (!content.isEmpty()){
            if (condition.isOrder()){
                param.put("order_code",content);
            }else {
                param.put("member_card",content);
            }
        }
        param.put("start_time",condition.getStart());
        param.put("end_time",condition.getEnd());

        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(mContext,getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(mContext.getUrl() + "/api/once_cards/uses", HttpRequest.generate_request_parma(param,mContext.getAppSecret())).
                enqueue(new ArrayCallback<VipTimeCardUseOrder>(VipTimeCardUseOrder.class) {
                    @Override
                    protected void onError(String msg) {
                        progressDialog.dismiss();
                        MyDialog.toastMessage(msg);
                    }

                    @Override
                    protected void onSuccessForResult(List<VipTimeCardUseOrder> d, String hint) {
                        ((UseOrderAdapter)mAdapter).setDataForList(d);
                        progressDialog.dismiss();
                    }
                });
    }


    @Override
    public String getTitle(){
        return CustomApplication.self().getString(R.string.once_card_use) + CustomApplication.self().getString(R.string.query_sz);
    }

    static class UseOrderAdapter extends AbstractDataAdapterForList<VipTimeCardUseOrder,UseOrderAdapter.MyViewHolder> implements View.OnClickListener{
        private final MainActivity mContext;
        public UseOrderAdapter(MainActivity c){
            mContext = c;
        }
        @Override
        public void onClick(View v) {
            final Object o = v.getTag();
            if (o instanceof VipTimeCardUseOrder){
                ((VipTimeCardUseOrder)o).print(mContext);
            }
        }

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
            @BindView(R.id.order_code)
            TextView order_code;
            @BindView(R.id.store_name)
            TextView store_name;
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id._order_time)
            TextView _order_time;
            @BindView(R.id.use_time)
            TextView use_time;
            @BindView(R.id._vip_label)
            TextView _vip_label;
            @BindView(R.id.print)
            TextView print;

            public MyViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = View.inflate(mContext, R.layout.time_card_use_query_adapter, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final VipTimeCardUseOrder order = getItem(position);
            if (null != order){
                holder.order_code.setText(order.getOrderCode());
                holder.store_name.setText(order.getStoresName());
                holder.name.setText(order.getTitle());
                holder._order_time.setText(order.getAddtime());
                holder.use_time.setText(String.format(Locale.CHINA,"%d次",order.getUseNum()));
                holder._vip_label.setText(String.format(Locale.CHINA,"会员：%s(%s)",order.getMemberName(),order.getMemberMobile()));

                holder.print.setTag(order);
                holder.print.setOnClickListener(this);
            }
        }
    }
}
