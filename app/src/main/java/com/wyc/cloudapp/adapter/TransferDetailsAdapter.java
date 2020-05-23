package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class TransferDetailsAdapter extends AbstractQueryDataAdapter<TransferDetailsAdapter.MyViewHolder>  {

    private JSONArray mTransferRetails,mTransferRefunds,mTransferDeposits,mTransferOrderCodes;
    private double mCashSumAmt = 0.0;
    private JSONObject mTransferSumInfo;
    public TransferDetailsAdapter(MainActivity context){
        mContext = context;
        mTransferSumInfo = new JSONObject();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView pay_m_name_tv,retail_order_num_tv,retail_amt_tv,refund_order_num_tv,refund_amt_tv,deposit_order_num_tv,deposit_amt_tv;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            pay_m_name_tv = itemView.findViewById(R.id.pay_m_name);
            retail_order_num_tv = itemView.findViewById(R.id.retail_order_num);
            retail_amt_tv = itemView.findViewById(R.id.retail_amt);
            refund_order_num_tv = itemView.findViewById(R.id.refund_order_num);
            refund_amt_tv = itemView.findViewById(R.id.refund_amt);
            deposit_order_num_tv = itemView.findViewById(R.id.deposit_order_num);
            deposit_amt_tv = itemView.findViewById(R.id.deposit_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext,R.layout.transfer_details_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,40));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mDatas){
            final JSONObject pay_info = mDatas.getJSONObject(position);
            if (pay_info != null){
                holder.pay_m_name_tv.setText(pay_info.getString("pay_m_name"));
                holder.retail_order_num_tv.setText(String.valueOf(pay_info.getIntValue("retail_order_num")));
                holder.retail_amt_tv.setText(String.format(Locale.CHINA,"%.2f",pay_info.getDoubleValue("retail_amt")));
                holder.refund_order_num_tv.setText(String.valueOf(pay_info.getIntValue("refund_order_num")));
                holder.refund_amt_tv.setText(String.format(Locale.CHINA,"%.2f",pay_info.getDoubleValue("refund_amt")));
                holder.deposit_order_num_tv.setText(String.valueOf(pay_info.getIntValue("deposit_order_num")));
                holder.deposit_amt_tv.setText(String.format(Locale.CHINA,"%.2f",pay_info.getDoubleValue("deposit_amt")));

                holder.mCurrentLayoutItemView.setOnClickListener(mItemClickListener);
            }
        }
    }

    private View.OnClickListener mItemClickListener = this::setCurrentItemView;

    @Override
    public void setDatas(final String cas_id){
        final StringBuilder err = new StringBuilder();
        final String pay_method_sql = "SELECT pay_method_id,name pay_m_name from pay_method order by sort",min_time_where_sql = " transfer_status = 1 and cashier_id =" + cas_id,
                min_time_sql = "select min(addtime) addtime from (select min(addtime) addtime from member_order_info where " + min_time_where_sql +
                " union select min(addtime) addtime from refund_order where "+ min_time_where_sql + " union select min(addtime) addtime from retail_order where "+ min_time_where_sql +") as b";
        final String min_time = SQLiteHelper.getString(min_time_sql,err);
        mDatas = SQLiteHelper.getListToJson(pay_method_sql,err);
        if (null != min_time && null != mDatas){
            if (getTransferOrderCodes(cas_id,min_time,err) && getTransferDetailsInfo(cas_id,min_time,err)){
                JSONObject object,pay_obj;
                int pay_method_id,total_num = 0,num = 0;
                double amt = 0.0,total_amt = 0.0;

                //零售
                for (int j = 0,jsize = mTransferRetails.size();j < jsize;j++){
                    object = mTransferRetails.getJSONObject(j);
                    pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
                    amt = object.getDoubleValue("pay_money");
                    num = object.getIntValue("order_num");

                    total_amt += amt;
                    total_num += num;

                    if (PayMethodViewAdapter.CASH_METHOD_ID.equals(String.valueOf(pay_method_id))){
                        mCashSumAmt += amt;
                    }
                    for (int i = 0,size = mDatas.size();i < size;i++){
                        pay_obj = mDatas.getJSONObject(i);
                        if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                            pay_obj.put("retail_order_num",num);
                            pay_obj.put("retail_amt",amt);
                        }
                    }
                }
                mTransferSumInfo.put("order_num",total_num);
                mTransferSumInfo.put("order_money",total_amt);
                total_num = 0;
                total_amt = 0.0;

                //退单
                for (int j = 0,jsize = mTransferRefunds.size();j < jsize;j++){
                    object = mTransferRefunds.getJSONObject(j);
                    pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
                    amt = object.getDoubleValue("pay_money");
                    num = object.getIntValue("order_num");

                    total_amt += amt;
                    total_num += num;

                    if (PayMethodViewAdapter.CASH_METHOD_ID.equals(String.valueOf(pay_method_id))){//退单现金要减
                        mCashSumAmt -= amt;
                    }

                    for (int i = 0,size = mDatas.size();i < size;i++){
                        pay_obj = mDatas.getJSONObject(i);
                        if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                            pay_obj.put("refund_order_num",num);
                            pay_obj.put("refund_amt",amt);
                        }
                    }
                }
                mTransferSumInfo.put("refund_num",total_num);
                mTransferSumInfo.put("refund_money",total_amt);
                total_num = 0;
                total_amt = 0.0;

                //充值
                for (int j = 0,jsize = mTransferDeposits.size();j < jsize;j++){
                    object = mTransferDeposits.getJSONObject(j);
                    pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
                    amt = object.getDoubleValue("pay_money");
                    num = object.getIntValue("order_num");

                    total_amt += amt;
                    total_num += num;

                    if (PayMethodViewAdapter.CASH_METHOD_ID.equals(String.valueOf(pay_method_id))){
                        mCashSumAmt += amt;
                    }

                    for (int i = 0,size = mDatas.size();i < size;i++){
                        pay_obj = mDatas.getJSONObject(i);
                        if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                            pay_obj.put("deposit_order_num",num);
                            pay_obj.put("deposit_amt",amt);
                        }
                    }
                }
                mTransferSumInfo.put("recharge_num",total_num);
                mTransferSumInfo.put("recharge_money",total_amt);

                for (int i = 0;i < mDatas.size();i++){
                    pay_obj = mDatas.getJSONObject(i);
                    int retail_order_num = pay_obj.getIntValue("retail_order_num"),refund_order_num = pay_obj.getIntValue("refund_order_num"),deposit_order_num = pay_obj.getIntValue("deposit_order_num");
                    if (retail_order_num == 0 && refund_order_num == 0 && deposit_order_num == 0){
                        mDatas.remove(i--);
                    }
                }

                //
                mTransferSumInfo.put("start_time",min_time);
                mTransferSumInfo.put("end_time",System.currentTimeMillis() / 1000);
                mTransferSumInfo.put("payable_amt",mCashSumAmt);


                //次卡
                mTransferSumInfo.put("cards_num",0);
                mTransferSumInfo.put("cards_money",0.0);


                Logger.d_json(mTransferSumInfo.toJSONString());

                notifyDataSetChanged();
            }else {
                mContext.runOnUiThread(()->MyDialog.ToastMessage("加载交班信息错误：" + err,mContext,null));
            }
        }else {
            mContext.runOnUiThread(()->MyDialog.ToastMessage("加载交班信息错误：" + err,mContext,null));
        }
     }


     private boolean getTransferOrderCodes(final String cas_id,final String start_time,final StringBuilder err){
         final String retail_code_sql = "select order_code from retail_order where transfer_status = 1 and cashier_id = "+ cas_id +"  and order_status = 2 and "+ start_time +" < addtime and addtime < strftime('%s','now') group by order_code",
                 refund_code_sql = "select ro_code from refund_order where transfer_status = 1 and order_status = 2 and cashier_id = "+ cas_id +" and "+ start_time +" < addtime and addtime < strftime('%s','now') group by ro_code",
                 deposit_code_sql = "select order_code from member_order_info where transfer_status = 1 and cashier_id = "+ cas_id +" and status = 3 and "+ start_time +" < addtime and addtime < strftime('%s','now') group by order_code";

         final JSONArray transfer_retail_codes = SQLiteHelper.getListToValue(retail_code_sql,err);
         final JSONArray transfer_refund_codes = SQLiteHelper.getListToValue(refund_code_sql,err);
         final JSONArray transfer_deposit_codes = SQLiteHelper.getListToValue(deposit_code_sql,err);

         if (null != transfer_retail_codes && null != transfer_refund_codes && null != transfer_deposit_codes ){
             if (mTransferOrderCodes == null)mTransferOrderCodes = new JSONArray();

             mTransferOrderCodes.addAll(transfer_retail_codes);
             mTransferOrderCodes.addAll(transfer_refund_codes);
             mTransferOrderCodes.addAll(transfer_deposit_codes);

             Logger.d_json(mTransferOrderCodes.toJSONString());
             return true;
         }
         return false;
     }

     private boolean getTransferDetailsInfo(final String cas_id,final String start_time,final StringBuilder err){
         final String retail_sql = "SELECT pay_method ,count(1) order_num,sum(pay_money) pay_money FROM retail_order_pays a inner join \n" +
                 "retail_order b on a.order_code = b.order_code where b.transfer_status = 1 and cashier_id = "+ cas_id +"  and b.order_status = 2 and a.pay_status = 2 and "+ start_time +" < b.addtime and b.addtime < strftime('%s','now') group by pay_method",
                 refund_sql = "SELECT pay_method ,count(1) order_num,sum(pay_money) pay_money FROM refund_order_pays a inner join refund_order b \n" +
                         "on a.ro_code = b.ro_code where b.transfer_status = 1 and cashier_id = "+ cas_id +"  and b.order_status = 2 and a.pay_status = 2 and " + start_time +" < b.addtime and b.addtime < strftime('%s','now') group by pay_method",
                 deposit_sql = "SELECT pay_method_id pay_method,count(1) order_num,sum(order_money) pay_money FROM member_order_info where transfer_status = 1 and cashier_id = "+ cas_id +"  and status = 3 and " + start_time +" < addtime and " +
                         "addtime < strftime('%s','now') group by pay_method_id";

         final JSONArray transfer_retails = mTransferRetails = SQLiteHelper.getListToJson(retail_sql,err);
         final JSONArray transfer_refunds = mTransferRefunds = SQLiteHelper.getListToJson(refund_sql,err);
         final JSONArray transfer_deposits = mTransferDeposits =  SQLiteHelper.getListToJson(deposit_sql,err);

         if (null != transfer_retails && null != transfer_refunds && null != transfer_deposits){

             Logger.d_json(transfer_retails.toJSONString());
             Logger.d_json(transfer_refunds.toJSONString());
             Logger.d_json(transfer_deposits.toJSONString());

             return true;
         }
         return false;
     }

     public JSONObject getTransferSumInfo(){
        return mTransferSumInfo;
     }

     public double getCashSumAmt(){
        return mCashSumAmt;
     }
}
