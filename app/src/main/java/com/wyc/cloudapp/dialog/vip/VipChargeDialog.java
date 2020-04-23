package com.wyc.cloudapp.dialog.vip;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.PayMethodItemDecoration;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.pay.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Locale;

public class VipChargeDialog extends AbstractPayDialog {
    private JSONObject mVip;
    private Myhandler mHandler;
    private boolean mOpenCashbox = false;
    private PayMethodViewAdapter mPayMethodViewAdapter;
    private boolean mPrintStatus = true;
    VipChargeDialog(@NonNull Context context, final JSONObject vip,boolean s) {
        super(context);
        mVip = vip;
        mPrintStatus = s;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mHandler = new Myhandler(this);

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

        //初始化支付方式
        initPayMethod();

        //初始化按钮事件
        mOk.setOnClickListener(view -> vip_charge());//父类默认会调用mYesOnclickListener接口，如果覆盖了记得单独调用mYesOnclickListener

        setTitle(mContext.getString(R.string.vip_charge_sz));
        setHint(mContext.getString(R.string.c_amt_hint_sz));

    }

    @Override
    protected void initPayMethod(){
        mPayMethodViewAdapter = new PayMethodViewAdapter(mContext,94);
        mPayMethodViewAdapter.setDatas("3");
        mPayMethodViewAdapter.setOnItemClickListener((v, pos) -> {
            mPayMethod = mPayMethodViewAdapter.getItem(pos);
            if (mPayMethod != null) {
                Logger.d_json(mPayMethod.toString());

                //开钱箱
                mOpenCashbox = PayMethodViewAdapter.CASH_METHOD_ID.equals(mPayMethod.getString("pay_method_id"));

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
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(mPayMethodViewAdapter);
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

                                store_info = JSON.parseObject((store_info.getString("storeInfo")));
                                stores_id = store_info.getString("stores_id");

                                data_.put("appid",appId);
                                data_.put("stores_id",stores_id);
                                data_.put("member_id",mVip.getString("member_id"));
                                data_.put("cashier_id",cashier_info.getString("cas_id"));
                                data_.put("order_money", mPayAmtEt.getText().toString());

                                sz_param = HttpRequest.generate_request_parm(data_,appScret);

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

                                                order_code = info_json.getString("order_code");

                                                //发起支付请求
                                                if (mPayMethod.getIntValue("is_check") != 2){
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
                                                    data_.put("pay_method",mPayMethod.getString("pay_method_id"));
                                                    data_.put("pay_code_str",mPayCode.getText().toString());

                                                    sz_param = HttpRequest.generate_request_parm(data_,appScret);

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
                                                                                sz_param = HttpRequest.generate_request_parm(data_,appScret);

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
                                                data_ = new JSONObject();
                                                data_.put("appid",appId);
                                                data_.put("order_code",order_code);
                                                data_.put("case_pay_money", mPayAmtEt.getText().toString());
                                                data_.put("pay_method",mPayMethod.getString("pay_method_id"));

                                                url = url + "/api/member/cl_money_order";
                                                sz_param = HttpRequest.generate_request_parm(data_,appScret);
                                                retJson = httpRequest.sendPost(url,sz_param,true);

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
                                                                Logger.d("充值成功返回：%s",info_json);
                                                                JSONArray members = JSON.parseArray(info_json.getString("member")),money_orders = JSON.parseArray(info_json.getString("money_order"));
                                                                JSONObject member = members.getJSONObject(0);

                                                                if (mPrintStatus && mContext instanceof Activity)
                                                                    Printer.print((Activity) mContext,get_print_content(member,money_orders.getJSONObject(0),cashier_info,store_info,info_json.getJSONArray("welfare")));

                                                                mHandler.obtainMessage(MessageID.VIP_C_SUCCESS_ID,member).sendToTarget();
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
                            MyDialog.ToastMessage("查询仓库信息错误：" + store_info.getString("info"),mContext,mDialogWindow);
                        }
                    }else{
                        MyDialog.ToastMessage("查询收银员信息错误：" + cashier_info.getString("info"),mContext,mDialogWindow);
                    }
                });
            }
    }
    private String c_format_58(JSONObject format_info,JSONObject member,JSONObject order,JSONObject casher_info,JSONObject stores_info,JSONArray welfare){
        StringBuilder info = new StringBuilder();
        String store_name = "",footer_c,new_line,order_code = "";
        int print_count = 1,footer_space = 5;

        order_code = order.getString("order_code");

        store_name = format_info.getString("s_n");

        footer_c = format_info.getString("f_c");
        print_count = format_info.getIntValue("p_c");
        footer_space = format_info.getIntValue("f_s");
        new_line = "\r\n";//Printer.commandToStr(Printer.NEW_LINE);

        if (mOpenCashbox)//开钱箱
            info.append(Printer.commandToStr(Printer.OPEN_CASHBOX));

        while (print_count-- > 0) {//打印份数
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name.length() == 0 ? stores_info.getString("stores_name") : store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append("门店：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"stores_name",""))).append(new_line);
            info.append("单号：".concat(order_code)).append(new_line);
            info.append("操作员：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"cas_name",""))).append(new_line);
            info.append("卡号：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"card_code",""))).append(new_line);
            info.append("会员姓名：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"name",""))).append(new_line);
            info.append("支付方式：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"pay_method_name",""))).append(new_line);
            info.append("充值金额：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"order_money",""))).append(new_line);
            info.append("赠送金额：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"give_money",""))).append(new_line);
            info.append("会员余额：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"money_sum",""))).append(new_line);
            info.append("会员积分：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"points_sum",""))).append(new_line);
            info.append("会员电话：".concat(Utils.getNullOrEmptyStringAsDefault(stores_info,"mobile",""))).append(new_line);
            info.append("时    间：".concat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(member.getIntValue("addtime") * 1000))).append(new_line);
            if (welfare != null && welfare.size() != 0){
                for (int i = 0,size = welfare.size();i < size;i++){
                    if (i == 0)info.append("优惠信息").append(new_line);
                    info.append("  ").append(welfare.getString(i)).append(new_line);
                }
            }
            info.append(new_line).append("门店热线：").append(Utils.getNullOrEmptyStringAsDefault(stores_info,"telphone","")).append(new_line);
            info.append("门店地址：").append(Utils.getNullOrEmptyStringAsDefault(stores_info,"region","")).append(new_line);

            info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c);
            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);

            if (print_count > 0){
                info.append(new_line).append(new_line).append(new_line);
            }

        }

        Logger.d(info);

        return info.toString();
    }

    private String get_print_content(JSONObject merber,JSONObject order,JSONObject casher_info,JSONObject stores_info,JSONArray welfare){
        JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("v_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.vip_c_format){
                switch (print_format_info.getIntValue("f_z")){
                    case R.id.f_58:
                        content = c_format_58(print_format_info,merber,order,casher_info,stores_info,welfare);
                        break;
                    case R.id.f_76:
                        break;
                    case R.id.f_80:
                        break;
                }
            }
        }else
            MyDialog.ToastMessage("加载打印格式错误：" + print_format_info.getString("info"),getContext(),getWindow());

        return content;
    }

    @Override
    public JSONObject getContent() {//返回的是同一个引用
        return mVip;
    }

    @Override
    public boolean verify(){
       if (mPayMethod == null)mPayMethodViewAdapter.getCurrentPayMethod();
       return MyDialog.ToastMessage(null,"会员信息不能为空！",mContext,mDialogWindow,mVip != null) && super.verify();
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
