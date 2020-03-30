package com.wyc.cloudapp.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.PayMethodItemDecoration;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.interface_abstract.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Date;

public class VipChargeDialog extends AbstractPayDialog {
    private JSONObject mVip;
    private Myhandler mHandler;
    public VipChargeDialog(@NonNull Context context,final JSONObject vip) {
        super(context);
        mVip = vip;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mHandler = new Myhandler(this);

        //初始化支付方式
        initPayMethod();

        //初始化按钮事件
        mOk.setOnClickListener(view -> vip_charge());//父类默认会调用mYesOnclickListener接口，如果覆盖了记得单独调用mYesOnclickListener

        setTitle(mContext.getString(R.string.vip_charge_sz));
        setHint(mContext.getString(R.string.c_amt_hint_sz));

        //保留两位小数
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
                if (editable.length()> 0){
                    int index = editable.toString().indexOf('.');
                    if (index > -1 && editable.length() >= (index += 3)){
                        Logger.d("index:%d",index);
                        editable.delete(index,editable.length());
                    }
                }
            }
        });
    }

    @Override
    protected void initPayMethod(){
        final PayMethodViewAdapter payMethodViewAdapter = new PayMethodViewAdapter(mContext,94);
        payMethodViewAdapter.setDatas("3");
        payMethodViewAdapter.setOnItemClickListener((v, pos) -> {
            mPayMethod = payMethodViewAdapter.getItem(pos);
            if (mPayMethod != null) {
                Logger.d_json(mPayMethod.toString());
                if (mPayMethod.optInt("is_check") != 2){ //显示付款码输入框
                    mPayCode.setVisibility(View.VISIBLE);
                    mPayCode.requestFocus();
                    mPayCode.setHint(mPayMethod.optString("xtype",""));
                }else{
                    mPayCode.callOnClick();
                    mPayCode.getText().clear();
                    mPayCode.setVisibility(View.GONE);
                    mPayAmtEt.requestFocus();
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(payMethodViewAdapter);
    }

    private void vip_charge(){
        if (verify()){
                mProgressDialog.setCancel(false).setMessage("正在生成充值订单...").show();
                CustomApplication.execute(()->{
                    JSONObject cashier_info = new JSONObject(),store_info = new JSONObject(),data_ = new JSONObject(),retJson,info_json;
                    if (SQLiteHelper.getLocalParameter("cashierInfo",cashier_info)){
                        if (SQLiteHelper.getLocalParameter("connParam",store_info)){
                            try {

                                HttpRequest httpRequest = new HttpRequest();

                                String url = store_info.getString("server_url"),appId = store_info.getString("appId"),
                                        appScret = store_info.getString("appScret"),stores_id,sz_param,order_code;

                                store_info = new JSONObject(store_info.getString("storeInfo"));
                                stores_id = store_info.optString("stores_id");

                                data_.put("appid",appId);
                                data_.put("stores_id",stores_id);
                                data_.put("member_id",mVip.getString("member_id"));
                                data_.put("cashier_id",cashier_info.getString("cas_id"));
                                data_.put("order_money", mPayAmtEt.getText().toString());

                                sz_param = HttpRequest.generate_request_parm(data_,appScret);

                                Logger.i("生成充值订单参数:url:%s%s,param:%s",url ,"/api/member/mk_money_order" ,sz_param);
                                retJson = httpRequest.sendPost(url + "/api/member/mk_money_order",sz_param,true);
                                Logger.i("生成充值订单返回:%s",retJson.toString());

                                switch (retJson.optInt("flag")) {
                                    case 0:
                                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                        break;
                                    case 1:
                                        info_json = new JSONObject(retJson.optString("info"));
                                        switch (info_json.optString("status")){
                                            case "n":
                                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                break;
                                            case "y":
                                                Logger.d_json(info_json.toString());

                                                order_code = info_json.getString("order_code");

                                                //发起支付请求
                                                if (mPayMethod.getInt("is_check") != 2){
                                                    String unified_pay_order = mPayMethod.getString("unified_pay_order"),
                                                            unified_pay_query = mPayMethod.getString("unified_pay_query");

                                                    if ("null".equals(unified_pay_order) || "".equals(unified_pay_order)){
                                                        unified_pay_order = "/api/pay2/index";
                                                    }
                                                    if ("null".equals(unified_pay_query) || "".equals(unified_pay_query)){
                                                        unified_pay_query = "/api/pay2_query/query";
                                                    }

                                                    mProgressDialog.setMessage("正在发起支付请求...").refreshMessage();
                                                    data_ = new JSONObject();
                                                    data_.put("appid",appId);
                                                    data_.put("stores_id",stores_id);
                                                    data_.put("order_code",order_code);
                                                    data_.put("pos_num",cashier_info.getString("pos_num"));
                                                    data_.put("is_wuren",2);
                                                    data_.put("order_code_son",generate_pay_son_order_id());
                                                    data_.put("pay_money", mPayAmtEt.getText().toString());
                                                    data_.put("pay_method",mPayMethod.optString("pay_method_id"));
                                                    data_.put("pay_code_str",mPayCode.getText().toString());

                                                    sz_param = HttpRequest.generate_request_parm(data_,appScret);

                                                    Logger.i("会员充值请求支付参数:url:%s%s,param:%s",url ,unified_pay_order,sz_param);
                                                    retJson = httpRequest.sendPost(url + unified_pay_order,sz_param,true);
                                                    Logger.i("会员充值支付请求返回:%s",retJson.toString());

                                                    switch (retJson.optInt("flag")){
                                                        case 0:
                                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                                            return;
                                                        case 1:
                                                            info_json = new JSONObject(retJson.optString("info"));
                                                            switch (info_json.optString("status")){
                                                                case "n":
                                                                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                                    return;
                                                                case "y":
                                                                    int res_code = info_json.getInt("res_code");
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
                                                                                sz_param = HttpRequest.generate_request_parm(data_,appScret);

                                                                                Logger.i("会员充值支付查询参数:url:%s%s,param:%s",url,unified_pay_order,sz_param);
                                                                                retJson = httpRequest.sendPost(url + unified_pay_query,sz_param,true);
                                                                                Logger.i("会员充值支付查询返回:%s",retJson.toString());

                                                                                switch (retJson.getInt("flag")){
                                                                                    case 0:
                                                                                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                                                                        return;
                                                                                    case 1:
                                                                                        info_json = new JSONObject(retJson.optString("info"));
                                                                                        Logger.json(info_json.toString());
                                                                                        switch (info_json.getString("status")){
                                                                                            case "n":
                                                                                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                                                                return;
                                                                                            case "y":
                                                                                                res_code = info_json.getInt("res_code");
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
                                                data_ = new JSONObject();
                                                data_.put("appid",appId);
                                                data_.put("order_code",order_code);
                                                data_.put("case_pay_money", mPayAmtEt.getText().toString());
                                                data_.put("pay_method",mPayMethod.optString("pay_method_id"));

                                                url = url + "/api/member/cl_money_order";
                                                sz_param = HttpRequest.generate_request_parm(data_,appScret);
                                                retJson = httpRequest.sendPost(url,sz_param,true);

                                                switch (retJson.optInt("flag")) {
                                                    case 0:
                                                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                                                        break;
                                                    case 1:
                                                        info_json = new JSONObject(retJson.optString("info"));
                                                        switch (info_json.optString("status")){
                                                            case "n":
                                                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                                                break;
                                                            case "y":
                                                                JSONArray array = new JSONArray(info_json.getString("member"));
                                                                info_json = array.getJSONObject(0);
                                                                mHandler.obtainMessage(MessageID.VIP_C_SUCCESS_ID,info_json).sendToTarget();
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
                                MyDialog.ToastMessage("参数解析错误：" + e.getMessage(),mContext,mDialogWindow);
                            }
                        }else{
                            MyDialog.ToastMessage("查询仓库信息错误：" + store_info.optString("info"),mContext,mDialogWindow);
                        }
                    }else{
                        MyDialog.ToastMessage("查询收银员信息错误：" + cashier_info.optString("info"),mContext,mDialogWindow);
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
       return MyDialog.ToastMessage("会员信息不能为空！",mContext,mDialogWindow,mVip != null)
        && MyDialog.SnackbarMessage(mDialogWindow,mContext.getString(R.string.select_pay_way_hint_sz),getCurrentFocus(),mPayMethod != null) && super.verify();
    }



    @SuppressLint("NewApi")
    private String generate_pay_son_order_id(){
        return "MPAY" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + Utils.getNonce_str(8);
    }
    private static class Myhandler extends Handler {
        private WeakReference<VipChargeDialog> weakHandler;
        private Myhandler(VipChargeDialog dialog){
            this.weakHandler = new WeakReference<>(dialog);
        }
        public void handleMessage(@NonNull Message msg){
            VipChargeDialog dialog = weakHandler.get();
            if (null == dialog)return;
            if (dialog.mProgressDialog != null && dialog.mProgressDialog.isShowing())dialog.mProgressDialog.dismiss();
            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(msg.obj.toString(),dialog.mContext,dialog.mDialogWindow);
                    break;
                case MessageID.VIP_C_SUCCESS_ID:
                    if (msg.obj instanceof JSONObject){
                        Logger.d_json( msg.obj.toString());
                        dialog.mVip = (JSONObject) msg.obj;
                        MyDialog.ToastMessage("充值成功！",dialog.mContext,dialog.mDialogWindow);
                        if (dialog.mYesOnclickListener != null){
                            dialog.mYesOnclickListener.onYesClick(dialog);
                        }
                    }
                    break;
            }
        }
    }

}
