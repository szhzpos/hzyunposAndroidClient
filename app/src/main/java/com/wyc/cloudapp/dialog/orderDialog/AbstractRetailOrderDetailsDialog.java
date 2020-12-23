package com.wyc.cloudapp.dialog.orderDialog;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.AbstractPayInfoAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractRetailOrderDetailsDialog extends AbstractOrderDetailsDialog {
    protected AbstractPayInfoAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mRetailDetailsPayInfoAdapter;
    public AbstractRetailOrderDetailsDialog(@NonNull MainActivity context, final CharSequence title, final JSONObject info) {
        super(context,title,info);
    }


    @Override
    public void dismiss(){
        super.dismiss();
        Printer.dismissPrintIcon(mContext);
    }

    @Override
    public void show(){
        super.show();
        Printer.showPrintIcon(mContext);
    }

    protected void verify_pay(){
        if (null != mPayRecord){
            if (mPayRecord.getIntValue("pay_status") == 2){
                MyDialog.ToastMessage("支付成功!",mContext,getWindow());
                return;
            }
            final CustomProgressDialog mProgressDialog = new CustomProgressDialog(mContext);
            mProgressDialog.setCancel(false).setMessage("正在查询支付结果...").refreshMessage().show();
            CustomApplication.execute(()->{
                try {
                    verify();
                }catch (Exception e){
                    e.printStackTrace();
                    mContext.runOnUiThread(()-> MyDialog.ToastMessage(e.getMessage(),mContext,getWindow()));
                }
                mContext.runOnUiThread(mProgressDialog::dismiss);
            });
        }else {
            MyDialog.ToastMessage("请选择验证记录！",mContext,getWindow());
        }
    }

    private void verify(){
        final JSONObject pay_record = mPayRecord;
        boolean query_status = false;
        final JSONObject object = new JSONObject();
        final HttpRequest httpRequest = new HttpRequest();
        final StringBuilder err = new StringBuilder();
        String unified_pay_query,discount_xnote = "",third_pay_order_id = "";
        double discount_money = 0.0;
        long pay_time = 0;
        int pay_status = 1,order_status = 1;
        final String pay_code = Utils.getNullStringAsEmpty(pay_record,"pay_code");
        if (2 == pay_record.getIntValue("is_check")){
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CANADA);
            try {
                final Date date = simpleDateFormat.parse(Utils.getNullStringAsEmpty(mOrderInfo,"oper_time"));
                if (date != null)
                    pay_time = date.getTime() / 1000; ;
            } catch (ParseException e) {
                e.printStackTrace();
                mContext.runOnUiThread(()-> MyDialog.ToastMessage("格式化时间错误：" + e.getLocalizedMessage(),mContext,getWindow()));
            }
            query_status = true;
            pay_status = 2;
            if (pay_time == 0){
                pay_time = System.currentTimeMillis() / 1000;
            }
        }else{
            unified_pay_query = Utils.getNullStringAsEmpty(pay_record,"unified_pay_query");
            if (unified_pay_query.isEmpty()){
                unified_pay_query = "/api/pay2_query/query";
            }
            object.put("appid",mContext.getAppId());
            if (!pay_code.isEmpty())
                object.put("pay_code",pay_code);
            object.put("order_code_son", pay_record.getString("order_code_son"));

            final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + unified_pay_query,sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    err.append(retJson.getString("info"));
                    break;
                case 1:
                    final JSONObject info_json = JSON.parseObject(retJson.getString("info"));
                    Logger.json(info_json.toString());
                    switch (info_json.getString("status")){
                        case "n":
                            pay_status = 1;
                            err.append(info_json.getString("info"));
                            break;
                        case "y":
                            int res_code = info_json.getIntValue("res_code");
                            if (res_code == 1 || res_code == 2){//支付成功
                                query_status = true;
                                Logger.d_json(info_json.toString());
                                if (info_json.containsKey("xnote")){
                                    discount_xnote = info_json.getString("xnote");
                                }
                                third_pay_order_id = info_json.getString("pay_code");
                                discount_money = info_json.getDoubleValue("discount");
                                pay_time = info_json.getLong("pay_time");
                                pay_status = info_json.getIntValue("pay_status");
                            }
                            if (res_code == 2){//支付失败
                                pay_status = 1;
                                err.append(info_json.getString("info"));
                            }
                            break;
                    }
                    break;
            }
        }

        final String sz_order_code = Utils.getNullStringAsEmpty(mOrderInfo,"order_code");;
        if (!query_status){
            final ContentValues values = new ContentValues();

            values.put("pay_status",pay_status);
            values.put("spare_param1",err.toString());
            final String whereClause = "order_code = ?";
            final String[] whereArgs = new String[]{sz_order_code};
            int rows = SQLiteHelper.execUpdateSql("retail_order",values,whereClause,whereArgs,err);
            if (rows < 0){
                Logger.e("更新订单状态错误：%s",err);
            }else if (rows == 0){
                Logger.i("未更新任何数据Table：%s,values:%s,whereClause:%s,whereArgs:%s","retail_order",values,whereClause, Arrays.toString(whereArgs));
            }
        }else{
            final List<String> tables = new ArrayList<>();
            final List<ContentValues> valueList = new ArrayList<>();
            final List<String> whereClauseList = new ArrayList<>();
            final List<String[]> whereArgsList = new ArrayList<>();

            final String pay_method_id = pay_record.getString("pay_method");
            final String order_code_son = pay_record.getString("order_code_son");

            tables.add("retail_order_pays");

            final ContentValues values_pays = new ContentValues();
            values_pays.put("pay_status",pay_status);
            values_pays.put("pay_serial_no",third_pay_order_id);
            values_pays.put("pay_time",pay_time);
            values_pays.put("discount_money",discount_money);
            values_pays.put("xnote",discount_xnote);
            values_pays.put("return_code",third_pay_order_id);
            valueList.add(values_pays);

            whereClauseList.add("order_code = ? and pay_code = ? and pay_method = ?");
            whereArgsList.add(new String[]{sz_order_code,order_code_son,pay_method_id});

            //更新当前付款记录
            pay_record.put("pay_status",pay_status);
            pay_record.put("pay_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(pay_time * 1000));
            if (pay_status == 2){
                pay_record.put("pay_status_name","已支付");
            }

            if (mRetailDetailsPayInfoAdapter.isPaySuccess()){//所有付款记录成功付款并且订单不是已退货状态再更新订单为已付款
                if (Utils.getNotKeyAsNumberDefault(mOrderInfo,"order_status",1) == 4)
                    order_status = 4;
                else
                    order_status = 2;
                mOrderInfo.put("pay_status_name","已支付");
            }

            tables.add("retail_order");

            final ContentValues values_order = new ContentValues();
            values_order.put("order_status",order_status);
            values_order.put("pay_status",pay_status);
            values_order.put("pay_time",pay_time);
            valueList.add(values_order);

            whereClauseList.add("order_code = ?");
            whereArgsList.add(new String[]{sz_order_code});

            int[] rows = SQLiteHelper.execBatchUpdateSql(tables,valueList,whereClauseList,whereArgsList,err);

            if (rows == null){
                Logger.e("更新订单状态错误：%s",err);
            }else {
                int index = SQLiteHelper.verifyUpdateResult(rows);
                mContext.runOnUiThread(()->{
                    mRetailDetailsPayInfoAdapter.notifyDataSetChanged();
                    if (index == -1){
                        if (err.length() == 0)err.append("支付成功！");
                    } else{
                        final String sz_err = String.format(Locale.CHINA,"数据表%s未更新，value:%s,whereClause:%s,whereArgs:%s",tables.get(index),valueList.get(index),
                                whereClauseList.get(index),Arrays.toString(whereArgsList.get(index)));
                        Logger.e(sz_err);
                        err.append(sz_err);
                    }
                });
                mContext.runOnUiThread(this::showOrderInfo);
            }
        }
        mContext.runOnUiThread(()-> MyDialog.ToastMessage(err.toString(),mContext,getWindow()));
    }
}
