package com.wyc.cloudapp.dialog.vip;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.GridItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.pay.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.print.bean.PrinterStatus;
import com.wyc.cloudapp.print.receipts.VipRechargeReceipts;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
@Deprecated
public final class VipChargeDialogImp extends AbstractPayDialog {
    private JSONObject mVip;
    private Myhandler mHandler;
    private PayMethodViewAdapter mPayMethodViewAdapter;
    VipChargeDialogImp(@NonNull SaleActivity context, final JSONObject vip) {
        super(context,context.getString(R.string.vip_charge_sz));
        mVip = vip;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mHandler = new Myhandler(this);

        setWatcherToPayAmt();

        initPayMethod();

        mOk.setText(R.string.charge_sz);
        mOk.setOnClickListener(view -> vip_charge());//父类默认会调用mYesOnclickListener接口，如果覆盖了记得单独调用mYesOnclickListener

        setHint(mContext.getString(R.string.c_amt_hint_sz));

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

    @Override
    protected void initPayMethod(){
        mPayMethodViewAdapter = new PayMethodViewAdapter(mContext,null);
        mPayMethodViewAdapter.setDatas("3");
        mPayMethodViewAdapter.setOnItemClickListener((object) -> {
            mPayMethod = object;
            if (mPayMethod.getIntValue("is_check") != 2){ //显示付款码输入框
                mPayCode.setVisibility(View.VISIBLE);
                mPayCode.requestFocus();
                mPayCode.setHint(mPayMethod.getString("xtype"));
            }else{
                mPayCode.callOnClick();
                mPayCode.getText().clear();
                mPayCode.setVisibility(View.GONE);
                mPayAmtEt.requestFocus();
            }
        });
        final RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new GridItemDecoration());
        recyclerView.setAdapter(mPayMethodViewAdapter);
    }

    private void setWatcherToPayAmt(){
        mPayAmtEt.postDelayed(()-> mPayAmtEt.requestFocus(),300);
        mPayAmtEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()> 0){//保留两位小数
                    int index = editable.toString().indexOf('.');
                    if (index > -1 && editable.length() >= (index += 3)){
                        Logger.d("index:%d",index);
                        editable.delete(index,editable.length());
                    }
                }
            }
        });
    }
    private void vip_charge(){
        if (verify()){
            mProgressDialog.setCancel(false).setMessage("正在生成充值订单...").show();
            CustomApplication.execute(()->{
                final HttpRequest httpRequest = new HttpRequest();
                final JSONObject member_order_info = new JSONObject();
                final StringBuilder err = new StringBuilder();

                JSONObject data_ = new JSONObject(),retJson,info_json;
                final String url = mContext.getUrl(),appId = mContext.getAppId(),appSecret = mContext.getAppSecret(),stores_id = mContext.getStoreId(),sz_moeny =  mPayAmtEt.getText().toString(),
                        member_id = mVip.getString("member_id"),third_order_id = generate_pay_son_order_id();


                member_order_info.put("stores_id",stores_id);
                member_order_info.put("member_id",member_id);
                member_order_info.put("status",1);
                member_order_info.put("addtime",System.currentTimeMillis() / 1000);

                member_order_info.put("card_code",mVip.getString("card_code"));
                member_order_info.put("mobile",mVip.getString("mobile"));
                member_order_info.put("name",mVip.getString("name"));

                member_order_info.put("third_order_id",third_order_id);
                member_order_info.put("cashier_id",mContext.getCashierId());
                member_order_info.put("order_money",sz_moeny);

                //保存单据
                if (!SQLiteHelper.saveFormJson(member_order_info,"member_order_info",null,0,err)){
                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,err.toString()).sendToTarget();
                    return;
                }

                    try {

                        data_.put("appid",appId);
                        data_.put("stores_id",stores_id);
                        data_.put("member_id",member_id);
                        data_.put("cashier_id",mContext.getCashierId());
                        data_.put("order_money",sz_moeny);

                        String sz_param = HttpRequest.generate_request_parma(data_,appSecret);

                        Logger.i("生成充值订单参数:url:%s%s,param:%s",url ,"/api/member/mk_money_order" ,sz_param);
                        retJson = httpRequest.sendPost(url + "/api/member/mk_money_order",sz_param,true);
                        Logger.i("生成充值订单返回:%s",retJson.toString());

                        switch (retJson.getIntValue("flag")) {
                            case 0:
                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                break;
                            case 1:
                                info_json = JSON.parseObject(retJson.getString("info"));
                                switch (info_json.getString("status")){
                                    case "n":
                                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                        break;
                                    case "y":
                                        Logger.d_json(info_json.toString());

                                        int is_check = mPayMethod.getIntValue("is_check");

                                        final String order_code = info_json.getString("order_code"),pay_method_id = mPayMethod.getString("pay_method_id"),
                                                whereClause = "member_id = ? and third_order_id = ?";

                                        final String[] whereArgs = new String[]{member_id,third_order_id};
                                        final ContentValues values = new ContentValues();

                                        //保存支付单号
                                        values.put("order_code",order_code);
                                        if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,err.toString()).sendToTarget();
                                            return;
                                        }

                                        //发起支付请求
                                        if (is_check != 2){
                                            String unified_pay_order = mPayMethod.getString("unified_pay_order"),
                                                    unified_pay_query = mPayMethod.getString("unified_pay_query");

                                            if ("null".equals(unified_pay_order) || "".equals(unified_pay_order)){
                                                unified_pay_order = InterfaceURL.UNIFIED_PAY;
                                            }
                                            if ("null".equals(unified_pay_query) || "".equals(unified_pay_query)){
                                                unified_pay_query = InterfaceURL.UNIFIED_PAY_QUERY;
                                            }

                                            mProgressDialog.setMessage("正在发起支付请求...").refreshMessage();
                                            data_ = new JSONObject();
                                            data_.put("appid",appId);
                                            data_.put("stores_id",stores_id);
                                            data_.put("order_code",order_code);
                                            data_.put("pos_num",mContext.getPosNum());
                                            data_.put("is_wuren",2);
                                            data_.put("order_code_son",third_order_id);
                                            data_.put("pay_money",sz_moeny);
                                            data_.put("pay_method",pay_method_id);
                                            data_.put("pay_code_str",mPayCode.getText().toString());

                                            sz_param = HttpRequest.generate_request_parma(data_,appSecret);

                                            Logger.i("会员充值请求支付参数:url:%s%s,param:%s",url ,unified_pay_order,sz_param);
                                            retJson = httpRequest.sendPost(url + unified_pay_order,sz_param,true);
                                            Logger.i("会员充值支付请求返回:%s",retJson.toString());

                                            switch (retJson.getIntValue("flag")){
                                                case 0:
                                                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                                    return;
                                                case 1:
                                                    info_json = JSON.parseObject(retJson.getString("info"));
                                                    switch (info_json.getString("status")){
                                                        case "n":
                                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                            return;
                                                        case "y":
                                                            int res_code = info_json.getIntValue("res_code");
                                                            switch (res_code){
                                                                case 1://支付成功
                                                                    break;
                                                                case 2:
                                                                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                                    return;
                                                                case 3:
                                                                case 4:
                                                                    while (res_code == 3 ||  res_code == 4){
                                                                        mProgressDialog.setMessage("正在查询支付结果...").refreshMessage();
                                                                        data_ = new JSONObject();

                                                                        data_.put("appid",appId);
                                                                        data_.put("pay_code",info_json.getString("pay_code"));
                                                                        data_.put("order_code_son",info_json.getString("order_code_son"));

                                                                        if (res_code == 4){
                                                                            data_.put("pay_password","");
                                                                        }
                                                                        sz_param = HttpRequest.generate_request_parma(data_,appSecret);

                                                                        Logger.i("会员充值支付查询参数:url:%s%s,param:%s",url,unified_pay_order,sz_param);
                                                                        retJson = httpRequest.sendPost(url + unified_pay_query,sz_param,true);
                                                                        Logger.i("会员充值支付查询返回:%s",retJson.toString());

                                                                        switch (retJson.getIntValue("flag")){
                                                                            case 0:
                                                                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                                                                return;
                                                                            case 1:
                                                                                info_json = JSON.parseObject(retJson.getString("info"));
                                                                                Logger.json(info_json.toString());
                                                                                switch (info_json.getString("status")){
                                                                                    case "n":
                                                                                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                                                        return;
                                                                                    case "y":
                                                                                        res_code = info_json.getIntValue("res_code");
                                                                                        if (res_code == 2){//支付失败
                                                                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                                                            return;
                                                                                        }
                                                                                        break;
                                                                                }
                                                                                break;
                                                                        }
                                                                    }
                                                                    break;
                                                            }
                                                            break;
                                                    }
                                                    break;
                                            }

                                        }

                                        //处理充值订单
                                        mProgressDialog.setMessage("正在处理充值订单...").refreshMessage();

                                        //保存支付方式,更新状态为已支付完成
                                        values.clear();
                                        values.put("pay_method_id",pay_method_id);
                                        values.put("status",2);
                                        if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,err.toString()).sendToTarget();
                                            return;
                                        }

                                        data_ = new JSONObject();
                                        data_.put("appid",appId);
                                        data_.put("order_code",order_code);
                                        if (is_check == 2)
                                            data_.put("case_pay_money",sz_moeny);
                                        data_.put("pay_method",pay_method_id);

                                        Logger.d_json(data_.toJSONString());

                                        sz_param = HttpRequest.generate_request_parma(data_,appSecret);
                                        retJson = httpRequest.sendPost(url + "/api/member/cl_money_order",sz_param,true);

                                        switch (retJson.getIntValue("flag")) {
                                            case 0:
                                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                                break;
                                            case 1:
                                                info_json = JSON.parseObject(retJson.getString("info"));
                                                switch (info_json.getString("status")){
                                                    case "n":
                                                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                        break;
                                                    case "y":
                                                        Logger.d_json(info_json.toJSONString());
                                                        final JSONArray members = JSON.parseArray(info_json.getString("member")),money_orders = JSON.parseArray(info_json.getString("money_order"));
                                                        final JSONObject member = members.getJSONObject(0),pay_info = money_orders.getJSONObject(0);

                                                        if (pay_info != null && member != null){

                                                            values.clear();
                                                            values.put("status",3);//已完成
                                                            values.put("xnote",info_json.toJSONString());
                                                            values.put("give_money",pay_info.getDoubleValue("give_money"));
                                                            if (SQLiteHelper.execUpdateSql("member_order_info",values,whereClause,whereArgs,err) < 0){
                                                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,err.toString()).sendToTarget();
                                                            }else {
                                                                mHandler.obtainMessage(MessageID.VIP_C_SUCCESS_ID,member).sendToTarget();
                                                                VipRechargeReceipts.print(order_code);
                                                            }
                                                        }else {
                                                            Logger.e("服务器返回member：%s,money_order：%s",members,money_orders);
                                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"服务器返回信息为空！").sendToTarget();
                                                        }
                                                        break;
                                                }
                                                break;
                                        }
                                        break;
                                }
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyDialog.ToastMessage("参数解析错误：" + e.getMessage(), mDialogWindow);
                    }
                });
            }
    }




    @Override
    public JSONObject getContent() {//返回的是同一个引用
        return mVip;
    }

    @Override
    public boolean verify(){
       if (mPayMethod == null)mPayMethod = mPayMethodViewAdapter.getDefaultPayMethod();
       return MyDialog.ToastMessage(null,"会员信息不能为空！", mDialogWindow,mVip != null) && super.verify();
    }

    private String generate_pay_son_order_id(){
        return "MPAY" + new SimpleDateFormat("yyyyMMdd",Locale.CHINA).format(new Date()) + Utils.getNonce_str(8);
    }
    private static class Myhandler extends Handler {
        private final WeakReference<VipChargeDialogImp> weakHandler;
        private Myhandler(VipChargeDialogImp dialog){
            this.weakHandler = new WeakReference<>(dialog);
        }
        public void handleMessage(@NonNull Message msg){
            VipChargeDialogImp dialog = weakHandler.get();
            if (null == dialog)return;
            if (dialog.mProgressDialog != null && dialog.mProgressDialog.isShowing())dialog.mProgressDialog.dismiss();
            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(msg.obj.toString(), dialog.mDialogWindow);
                    break;
                case MessageID.VIP_C_SUCCESS_ID:
                    if (msg.obj instanceof JSONObject){
                        Logger.d_json( msg.obj.toString());
                        dialog.mVip = (JSONObject) msg.obj;
                        MyDialog.ToastMessage("充值成功！", dialog.mDialogWindow);
                        if (dialog.mYesOnclickListener != null){
                            dialog.mYesOnclickListener.onYesClick(dialog);
                        }
                    }
                    break;
            }
        }
    }

}
