package com.wyc.cloudapp.adapter;

import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractTransferDetailsAdapter extends AbstractQueryDataAdapter<AbstractTransferDetailsAdapter.MyViewHolder>  {
    protected JSONArray mTransferRetails,mTransferRefunds,mTransferDeposits,mTransferOrderCodes,mTransferCardsc;
    protected final JSONObject mTransferSumInfo;
    protected final boolean mTransferAmtNotVisible;
    protected PasswordEditTextReplacement editTextReplacement;
    protected String mTransferStartTime = "";
    public AbstractTransferDetailsAdapter(MainActivity context){
        super(context);
        mTransferSumInfo = new JSONObject();
        mTransferAmtNotVisible = !verifyShowAmtPermissions();

        if (mTransferAmtNotVisible)
            editTextReplacement = new PasswordEditTextReplacement();
    }

    protected static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public JSONArray getTransferRetails(){
        return mTransferRetails;
    }

    public JSONArray getTransferRefunds(){
        return mTransferRefunds;
    }

    public JSONArray getTransferDeposits(){
        return mTransferDeposits;
    }

    public void setDatas(final String cas_id){
        final StringBuilder err = new StringBuilder();
        final String start_time = mTransferStartTime,ti_code = generateTransferIdOrderCode(),stores_id = mContext.getStoreId();

        Logger.d("start_time：%s",start_time);

        if (getTransferOrderCodes(ti_code,cas_id,stores_id,start_time,err) && getTransferDetailsInfo(cas_id,stores_id,start_time,err)){
            final String pay_method_sql = "SELECT pay_method_id,name pay_m_name from pay_method order by sort";
            mDatas = SQLiteHelper.getListToJson(pay_method_sql,err);
            if (mDatas != null){

                double cash_sum_amt = 0.0;

                cash_sum_amt += disposeTransferRetails(ti_code);
                cash_sum_amt += disposeTransferRefunds(ti_code);
                cash_sum_amt += disposeTransferDeposits(ti_code);
                cash_sum_amt += disposeTransferCardsc(ti_code);

                JSONObject pay_obj;
                for (int i = 0;i < mDatas.size();i++){
                    pay_obj = mDatas.getJSONObject(i);
                    int retail_order_num = pay_obj.getIntValue("retail_order_num"),refund_order_num = pay_obj.getIntValue("refund_order_num"),
                            deposit_order_num = pay_obj.getIntValue("deposit_order_num"),cardsc_order_num = pay_obj.getIntValue("cardsc_order_num");
                    if (retail_order_num == 0 && refund_order_num == 0 && deposit_order_num == 0 && cardsc_order_num == 0){
                        mDatas.remove(i--);
                    }
                }
                //
                mTransferSumInfo.put("cas_id",cas_id);
                mTransferSumInfo.put("order_b_date",start_time);
                mTransferSumInfo.put("order_e_date",System.currentTimeMillis() / 1000);
                mTransferSumInfo.put("sj_money",cash_sum_amt);
                mTransferSumInfo.put("sum_money",cash_sum_amt);
                mTransferSumInfo.put("transfer_time",start_time);
                mTransferSumInfo.put("stores_id",mContext.getStoreId());
                mTransferSumInfo.put("ti_code",ti_code);
            }
        }
        if (err.length() != 0)mContext.runOnUiThread(()->MyDialog.ToastMessage(err.toString(),mContext,null));
    }

    private boolean verifyShowAmtPermissions(){//是否显示金额权限
        return mContext.verifyPermissions("7",null,false);
    }

    private String getTransferStartTime(final String cas_id,final String stores_id,final StringBuilder err){
        final String start_time_where_sql = " transfer_status = 1 and (order_status = 2  or order_status = 4) and stores_id = "+ stores_id +" and cashier_id =" + cas_id,
                start_time_sql = "select min(addtime) addtime from (select min(addtime) addtime from member_order_info where  transfer_status = 1 and status = 3 and stores_id = "+ stores_id +" and cashier_id =" + cas_id +
                        " union select min(addtime) addtime from refund_order where "+ start_time_where_sql + " union select min(addtime) addtime from retail_order where "+ start_time_where_sql +") as b";
        return SQLiteHelper.getString(start_time_sql,err);
    }

    protected double disposeTransferRetails(final String ti_code){
        JSONObject object,pay_obj;
        int pay_method_id,num = 0,total_orders = 0;
        double amt = 0.0,total_amt = 0.0,cash_sum_amt = 0.0;
        //零售
        for (int j = 0,jsize = mTransferRetails.size();j < jsize;j++){
            object = mTransferRetails.getJSONObject(j);
            object.put("ti_code",ti_code);
            pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
            amt = object.getDoubleValue("pay_money");
            num = object.getIntValue("order_num");

            total_orders += num;
            total_amt += amt;

            if (PayMethodViewAdapter.getCashMethodId().equals(String.valueOf(pay_method_id))){
                cash_sum_amt += amt;
            }
            for (int i = 0,size = mDatas.size();i < size;i++){
                pay_obj = mDatas.getJSONObject(i);
                if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                    pay_obj.put("retail_order_num",num);
                    pay_obj.put("retail_amt",amt);
                }
            }
        }

        mTransferSumInfo.put("retail_total_orders",total_orders);
        mTransferSumInfo.put("order_money",total_amt);

        return cash_sum_amt;
    }
    protected double disposeTransferRefunds(final String ti_code){
        JSONObject object,pay_obj;
        int pay_method_id,num = 0,total_num = 0;
        double amt = 0.0,total_amt = 0.0,cash_sum_amt = 0.0;
        //退单
        for (int j = 0,jsize = mTransferRefunds.size();j < jsize;j++){
            object = mTransferRefunds.getJSONObject(j);
            object.put("ti_code",ti_code);
            pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
            amt = object.getDoubleValue("pay_money");
            num = object.getIntValue("order_num");

            total_num += num;
            total_amt += amt;

            if (PayMethodViewAdapter.getCashMethodId().equals(String.valueOf(pay_method_id))){//退单现金要减
                cash_sum_amt -= amt;
            }

            for (int i = 0,size = mDatas.size();i < size;i++){
                pay_obj = mDatas.getJSONObject(i);
                if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                    pay_obj.put("refund_order_num",num);
                    pay_obj.put("refund_amt",amt);
                }
            }
        }
        mTransferSumInfo.put("refund_total_orders",total_num);
        mTransferSumInfo.put("refund_money",total_amt);

        return cash_sum_amt;
    }
    protected double disposeTransferDeposits(final String ti_code){
        JSONObject object,pay_obj;
        int pay_method_id,num = 0,total_num = 0;
        double amt = 0.0,total_amt = 0.0,cash_sum_amt = 0.0;
        //充值
        for (int j = 0,jsize = mTransferDeposits.size();j < jsize;j++){
            object = mTransferDeposits.getJSONObject(j);
            object.put("ti_code",ti_code);

            pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
            amt = object.getDoubleValue("pay_money");
            num = object.getIntValue("order_num");

            total_num += num;
            total_amt += amt;

            if (PayMethodViewAdapter.getCashMethodId().equals(String.valueOf(pay_method_id))){
                cash_sum_amt += amt;
            }

            for (int i = 0,size = mDatas.size();i < size;i++){
                pay_obj = mDatas.getJSONObject(i);
                if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                    pay_obj.put("deposit_order_num",num);
                    pay_obj.put("deposit_amt",amt);
                }
            }
        }
        mTransferSumInfo.put("deposits_total_orders",total_num);
        mTransferSumInfo.put("recharge_money",total_amt);

        return cash_sum_amt;
    }
    protected double disposeTransferCardsc(final String ti_code){
        JSONObject object,pay_obj;
        int pay_method_id,num = 0,total_num = 0;
        double amt = 0.0,total_amt = 0.0,cash_sum_amt = 0.0;

        //次卡
        for (int j = 0,jsize = mTransferCardsc.size();j < jsize;j++){
            object = mTransferCardsc.getJSONObject(j);
            object.put("ti_code",ti_code);

            pay_method_id = Utils.getNotKeyAsNumberDefault(object,"pay_method",-1);
            amt = object.getDoubleValue("pay_money");
            num = object.getIntValue("order_num");

            total_num += num;
            total_amt += amt;

            if (PayMethodViewAdapter.getCashMethodId().equals(String.valueOf(pay_method_id))){
                cash_sum_amt += amt;
            }

            for (int i = 0,size = mDatas.size();i < size;i++){
                pay_obj = mDatas.getJSONObject(i);
                if (pay_method_id == Utils.getNotKeyAsNumberDefault(pay_obj,"pay_method_id",-2)){
                    pay_obj.put("cardsc_order_num",num);
                    pay_obj.put("cardsc_amt",amt);
                }
            }
        }

        mTransferSumInfo.put("cardsc_total_orders",total_num);
        mTransferSumInfo.put("cards_money",total_amt);

        return cash_sum_amt;
    }

    protected String generateTransferIdOrderCode(){
        String prefix = "J" + mContext.getPosNum() + "-" + new SimpleDateFormat("yyMMddHHmmss",Locale.CHINA).format(new Date()) + "-",order_code ;
        JSONObject orders= new JSONObject();
        if (SQLiteHelper.execSql(orders,"SELECT count(ti_code) + 1 ti_code from transfer_info where date(transfer_time,'unixepoch' ) = date('now')")){
            order_code =orders.getString("ti_code");
            order_code = prefix + "0000".substring(order_code.length()) + order_code;
        }else{
            order_code = prefix + "0001";
            Logger.d("生成订单号错误：" + orders.getString("info"));
        }
        return order_code;
    }

    protected boolean getTransferOrderCodes(final String ti_code,final String cas_id,final String stores_id,final String start_time,final StringBuilder err){
        boolean code = false;
         final String retail_code_sql = "select cashier_id cas_id,order_code from retail_order where transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +"  and (order_status = 2  or order_status = 4) and "+ start_time +" <= addtime and addtime <= strftime('%s','now') group by order_code,cashier_id",
                 refund_code_sql = "select cashier_id cas_id,ro_code order_code from refund_order where transfer_status = 1 and order_status = 2 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +" and "+ start_time +" <= addtime and addtime <= strftime('%s','now') group by ro_code,cashier_id",
                 deposit_code_sql = "select cashier_id cas_id,order_code from member_order_info where transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +" and status = 3 and "+ start_time +" <= addtime and addtime <= strftime('%s','now') group by order_code,cashier_id";

         Logger.d(retail_code_sql);
         final JSONArray transfer_retail_codes = SQLiteHelper.getListToJson(retail_code_sql,err);
         final JSONArray transfer_refund_codes = SQLiteHelper.getListToJson(refund_code_sql,err);
         final JSONArray transfer_deposit_codes = SQLiteHelper.getListToJson(deposit_code_sql,err);

         if (null != transfer_retail_codes && null != transfer_refund_codes && null != transfer_deposit_codes ){
             if (mTransferOrderCodes == null)mTransferOrderCodes = new JSONArray();

             mTransferSumInfo.put("order_num",transfer_retail_codes.size());
             mTransferSumInfo.put("refund_num",transfer_refund_codes.size());
             mTransferSumInfo.put("recharge_num",transfer_deposit_codes.size());

             mTransferOrderCodes.addAll(transfer_retail_codes);
             mTransferOrderCodes.addAll(transfer_refund_codes);
             mTransferOrderCodes.addAll(transfer_deposit_codes);

             if (mTransferOrderCodes.isEmpty()){
                 mTransferSumInfo.clear();
                 err.append("无交班信息!");
             }else {
                 JSONObject obj;
                 for (int i = 0,size = mTransferOrderCodes.size();i < size;i++){
                     obj = mTransferOrderCodes.getJSONObject(i);
                     obj.put("status",2);
                     obj.put("ti_code",ti_code);
                 }
                 code = true;
             }
         }
         return code;
     }

    protected boolean getTransferDetailsInfo(final String cas_id,final String stores_id,final String start_time,final StringBuilder err){
         final String retail_sql = "SELECT pay_method,c.name pay_name ,count(1) order_num,sum(pay_money) pay_money FROM retail_order_pays a left join pay_method c on a.pay_method = c.pay_method_id inner join \n" +
                 "retail_order b on a.order_code = b.order_code where b.transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +"  and (b.order_status = 2  or b.order_status = 4) and a.pay_status = 2 and "+ start_time +" <= b.addtime and b.addtime <= strftime('%s','now') group by pay_method",
                 refund_sql = "SELECT pay_method,c.name pay_name,count(1) order_num,sum(pay_money) pay_money FROM refund_order_pays a left join pay_method c on a.pay_method = c.pay_method_id inner join refund_order b \n" +
                         "on a.ro_code = b.ro_code where b.transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +"  and b.order_status = 2 and a.pay_status = 2 and " + start_time +" <= b.addtime and b.addtime <= strftime('%s','now') group by pay_method",
                 deposit_sql = "SELECT a.pay_method_id pay_method,c.name pay_name ,count(1) order_num,sum(order_money) pay_money FROM member_order_info a left join pay_method c on a.pay_method_id = c.pay_method_id where transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +"  and a.status = 3 and " + start_time +" <= addtime and " +
                         "addtime <= strftime('%s','now') group by a.pay_method_id";

         final JSONArray transfer_retails = mTransferRetails = SQLiteHelper.getListToJson(retail_sql,err);
         final JSONArray transfer_refunds = mTransferRefunds = SQLiteHelper.getListToJson(refund_sql,err);
         final JSONArray transfer_deposits = mTransferDeposits =  SQLiteHelper.getListToJson(deposit_sql,err);
         mTransferCardsc = new JSONArray();

         return null != transfer_retails && null != transfer_refunds && null != transfer_deposits;
     }

    public int verifyTransfer(final StringBuilder info){
        int code = -1;
        String cas_id = mContext.getCashierId(),stores_id= mContext.getStoreId(),start_time = getTransferStartTime(cas_id,stores_id,info);

        if (start_time != null){
            if (start_time.isEmpty())start_time = String.valueOf(new Date().getTime() / 1000);
            final String sz_counts = SQLiteHelper.getString("SELECT count(order_id) counts FROM retail_order where pay_status = 3 and cashier_id = "+ cas_id +" and" +
                    " stores_id = " + stores_id + " and "+ start_time +" <= addtime and addtime <= strftime('%s','now')",info),
                    sz_h_counts = SQLiteHelper.getString("select count(hang_id) from hangbill where cas_id = '"+ cas_id +"' and stores_id = " + stores_id,info);

            if (sz_counts != null && sz_h_counts != null){
                if (Integer.parseInt(sz_counts) == 0 ){
                    if (Integer.parseInt(sz_h_counts) == 0){
                        code = 0;
                        mTransferStartTime = start_time;
                    }else {
                        code = 2;
                    }
                }else {
                    info.append(start_time);
                    code = 1;
                }
            }
        }
        return code;
     }

     public JSONObject getTransferSumInfo(){
        return mTransferSumInfo;
     }


    public boolean saveTransferDetailInfo(double cashbox_amt,final StringBuilder err){
        boolean code;
        final String stores_id= mContext.getStoreId(),cas_id = mContext.getCashierId();
        final JSONObject data = new JSONObject();
        final List<String> tables = Arrays.asList("transfer_info","transfer_order","transfer_money_info","transfer_once_cardsc","transfer_recharge_money","transfer_refund_money"),
                transfer_info_cls = Arrays.asList("sj_money","cashbox_money","cards_num","cards_money","order_money","order_e_date","order_b_date","recharge_num","recharge_money","refund_num",
                        "refund_money","sum_money","ti_code","transfer_time","order_num","cas_id","stores_id"),

                transfer_order_cls = Arrays.asList("cas_id","status","order_code","ti_code"),

                details_cls = Arrays.asList("order_num","pay_money","pay_method","ti_code");

        long start_time = mTransferSumInfo.getLongValue("order_b_date"),end_time = mTransferSumInfo.getLongValue("order_e_date");
        final String where_sql = "where transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +"  and (order_status = 2  or order_status = 4) and "+ start_time +" <= addtime and addtime <= "+ end_time,

                retail_order_update_sql = "update retail_order set transfer_time = strftime('%s','now'),transfer_status = 2 " + where_sql,refund_order_update_sql = "update refund_order set transfer_time = strftime('%s','now'),transfer_status = 2 " + where_sql,
                member_order_info_update_sql = "update member_order_info set transfer_status = 2 where transfer_status = 1 and stores_id = "+ stores_id +" and cashier_id = "+ cas_id +"  and status = 3 and " + start_time +" <= addtime and " +
                        "addtime <= "+ end_time;

        final List<String> update_sql = Arrays.asList(retail_order_update_sql,refund_order_update_sql,member_order_info_update_sql);

        final JSONArray transfer_infos = new JSONArray();
        mTransferSumInfo.put("cashbox_money",cashbox_amt);
        transfer_infos.add(mTransferSumInfo);

        data.put("transfer_info",transfer_infos);
        data.put("transfer_order",mTransferOrderCodes);

        data.put("transfer_money_info",mTransferRetails);
        data.put("transfer_once_cardsc",mTransferCardsc);
        data.put("transfer_recharge_money",mTransferDeposits);
        data.put("transfer_refund_money",mTransferRefunds);


        Logger.d_json(data.toJSONString());

        if (!(code = SQLiteHelper.execBatchInsertAndUpdate(data,tables,Arrays.asList(transfer_info_cls,transfer_order_cls,details_cls,details_cls,details_cls,details_cls),update_sql,err,0))){
            err.insert(0,"保存订单信息错误：");
        }
        return code;
    }
    public boolean isTransferAmtNotVisible(){
        return mTransferAmtNotVisible;
    }
    public boolean isEmpty(){
        return mTransferSumInfo.isEmpty();
    }
}
