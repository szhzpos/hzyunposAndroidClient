package com.wyc.cloudapp.activity.mobile;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.UnifiedPayResult;
import com.wyc.cloudapp.data.room.entity.PayMethod;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
* 次卡订单详情
* */
public class TimeCardOrderDetailActivity extends AbstractMobileActivity{

    @BindView(R.id.order_id_tv)
    TextView order_id_tv;
    @BindView(R.id.m_order_time_tv)
    TextView order_time_tv;
    @BindView(R.id.m_vip_no_tv)
    TextView vip_no_tv;
    @BindView(R.id.m_vip_name_tv)
    TextView vip_name_tv;
    @BindView(R.id.m_vip_mobile_tv)
    TextView vip_mobile_tv;
    @BindView(R.id.m_sale_man_tv)
    TextView sale_man_tv;
    @BindView(R.id._amt_tv)
    TextView _amt_tv;

    private TimeCardSaleOrder mOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getString(R.string.time_card_detail));
        ButterKnife.bind(this);

        initOrder();
        initTimeCardList();
        initTimeCardPayList();
    }

    @OnClick(R.id.m_pay_verify_btn)
    public void verify(){
        List<TimeCardPayDetail> payDetailList = mOrder.getPayInfo();
        if (!payDetailList.isEmpty()){
            TimeCardPayDetail payDetail = payDetailList.get(0);
            if (mOrder.isSuccess() && payDetail.getStatus() == 1){
                MyDialog.toastMessage(getString(R.string.success));
                return;
            }
            PayMethod payMethod = PayMethod.getMethodById(payDetail.getPay_method_id());
            if (null != payMethod){
                if (payMethod.isCheckApi()){
                    UnifiedPayResult result = payMethod.queryPayStatus(this,payDetail.getOrder_no(),getLocalClassName());
                    if (result.isSuccess()){
                        payDetail.success();
                        mOrder.uploadPayInfo(s->{
                            EventBus.getDefault().post(mOrder.getStatus());
                            MyDialog.toastMessage(s);
                        }, MyDialog::toastMessage);
                    }else{
                        payDetail.failure();
                        MyDialog.toastMessage(result.getInfo());
                    }
                    TimeCardPayDetail.update(payDetailList);
                }else {
                    mOrder.uploadPayInfo(MyDialog::toastMessage, MyDialog::toastMessage);
                }
            }else MyDialog.toastMessage(getString(R.string.not_exist_hint_sz,"PayMethodId:" + payDetail.getPay_method_id()));
        }
    }

    @OnClick(R.id.m_print_btn)
    public void print(){
        TimeCardPayActivity.print(this,mOrder);
    }

    private void initOrder(){
        mOrder = getIntent().getParcelableExtra("o");
        if (null == mOrder){
            throw new IllegalArgumentException("mOrder must not be empty...");
        }
        mOrder.setPayInfo(TimeCardPayDetail.getPayDetailByOrderNo(mOrder.getOrder_no()));
        mOrder.setSaleInfo(TimeCardSaleInfo.getSaleInfoById(mOrder.getOrder_no()));

        order_id_tv.setText(mOrder.getOnline_order_no());
        order_time_tv.setText(mOrder.getFormatTime());
        vip_no_tv.setText(mOrder.getVip_card_no());
        vip_name_tv.setText(mOrder.getVip_name());
        vip_mobile_tv.setText(mOrder.getVip_mobile());
        sale_man_tv.setText(mOrder.getSalemanName());
        _amt_tv.setText(String.format(Locale.CHINA,"%.2f",mOrder.getAmt()));
    }

    private void initTimeCardList(){
        RecyclerView _order_details_list = findViewById(R.id._order_details_list);
        TimeCardAdapter adapter = new TimeCardAdapter();
        _order_details_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        _order_details_list.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        _order_details_list.setAdapter(adapter);
        adapter.setDataForList(mOrder.getSaleInfo());
    }

    private void initTimeCardPayList(){
        RecyclerView _pay_details_list = findViewById(R.id.m_pay_details_list);
        TimeCardPayAdapter adapter = new TimeCardPayAdapter();
        _pay_details_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        _pay_details_list.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        _pay_details_list.setAdapter(adapter);
        adapter.setDataForList(mOrder.getPayInfo());
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_time_card_order_detail;
    }

    public static void start(final Context context,@NonNull TimeCardSaleOrder order){
        context.startActivity(new Intent(context,TimeCardOrderDetailActivity.class).putExtra("o",order));
    }

    private static class TimeCardAdapter extends AbstractDataAdapterForList<TimeCardSaleInfo,TimeCardAdapter.MyViewHolder>{

        static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
            TextView row_id_tv,name,xnum_tv,price_tv,sale_amt_tv;
            MyViewHolder(View itemView) {
                super(itemView);
                row_id_tv = findViewById(R.id.row_id);
                name = findViewById(R.id.goods_title);
                xnum_tv = findViewById(R.id.xnum);
                price_tv = findViewById(R.id.price);
                sale_amt_tv = findViewById(R.id.sale_amt);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View itemView = View.inflate(parent.getContext(), R.layout.mobile_retail_details_goods_info_content_layout, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            TimeCardSaleInfo saleInfo = getItem(position);
            if (saleInfo != null){
                holder.row_id_tv.setText(String.format(Locale.CHINA,"%d、",position + 1));
                holder.name.setText(saleInfo.getName());
                holder.xnum_tv.setText(String.valueOf(saleInfo.getNum()));
                holder.price_tv.setText(String.valueOf(saleInfo.getPrice()));
                holder.sale_amt_tv.setText(String.format(Locale.CHINA,"%.2f",saleInfo.getAmt()));
            }
        }
    }

    private static class TimeCardPayAdapter extends AbstractDataAdapterForList<TimeCardPayDetail,TimeCardPayAdapter.MyViewHolder> {

        static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
            TextView row_id_tv,pay_method_name_tv,pay_amt_tv,pay_status_tv,pay_time_tv,pay_code_tv;
            MyViewHolder(View itemView) {
                super(itemView);
                row_id_tv = findViewById(R.id.row_id);
                pay_method_name_tv = itemView.findViewById(R.id.pay_method_name);
                pay_amt_tv = findViewById(R.id.pay_amt);
                pay_status_tv = findViewById(R.id.pay_status);
                pay_time_tv = findViewById(R.id.pay_time);
                pay_code_tv = findViewById(R.id.pay_code);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            View itemView = View.inflate(context, R.layout.mobile_retail_details_pay_info_content_layout, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) context.getResources().getDimension(R.dimen.table_row_height)));
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final TimeCardPayDetail payDetail = getItem(position);
            if (null != payDetail){
                holder.row_id_tv.setText(String.valueOf(position + 1));

                PayMethod payMethod = PayMethod.getMethodById(payDetail.getPay_method_id());
                if (payMethod != null){
                    holder.pay_method_name_tv.setText(payMethod.getName());
                }
                holder.pay_amt_tv.setText(String.format(Locale.CHINA,"%.2f",payDetail.getAmt()));
                holder.pay_status_tv.setText(String.valueOf(payDetail.getStatus()));
                holder.pay_time_tv.setText(payDetail.getPay_time());
                holder.pay_code_tv.setText(payDetail.getOrder_no());
            }
        }
    }
}