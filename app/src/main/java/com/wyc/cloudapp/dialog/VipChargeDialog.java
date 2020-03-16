package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class VipChargeDialog extends Dialog {
    private EditText mC_amt;
    private Context mContext;
    private String mPayMethodId;
    private CustomProgressDialog mProgressDialog;
    private JSONObject mVip;
    private Myhandler mHandler;
    private onYesOnclickListener mYesOnclickListener;
    public VipChargeDialog(@NonNull Context context,final JSONObject vip) {
        super(context);
        mContext = context;
        mVip = vip;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.vip_charge_dialog_content);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(this);

        //初始化金额text
        init_c_amount();

        //初始化支付方式
        intiPayMethod();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(view->VipChargeDialog.this.dismiss());
        findViewById(R.id._back).setOnClickListener(v -> {
            View view =  getCurrentFocus();
            if (view != null) {
                if (view.getId() == R.id.c_amt) {
                    EditText tmp_edit = ((EditText)view);
                    int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                    if (index != end && end  == tmp_edit.getText().length()){
                        tmp_edit.setText(mContext.getString(R.string.space_sz));
                    }else{
                        if (index == 0)return;
                        tmp_edit.getText().delete(index - 1, index);
                    }
                }
            }
        });
        findViewById(R.id._ok).setOnClickListener(v -> {
            vip_charge();
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id._cancel || id == R.id._ok)){
                tmp_v.setOnClickListener(button_click);
            }
        }
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.c_amt) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };

    private void init_c_amount(){
        mC_amt = findViewById(R.id.c_amt);
        mC_amt.setSelectAllOnFocus(true);
        mC_amt.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
        mC_amt.postDelayed(()->{mC_amt.requestFocus();},300);
    }

    private void intiPayMethod(){
        StringBuilder err = new StringBuilder();
        JSONArray array = SQLiteHelper.getList("select pay_method_id,name from pay_method where status = '1' order by sort",0,0,false,err);
        if (array != null){
            Spinner m_vip_level = findViewById(R.id.pay_method_spinner);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);
            if (array.length() != 0){
                arrayAdapter.add(mContext.getString(R.string.pay_m_hint_sz));
                mPayMethodId = array.optJSONObject(0).optString("pay_method_id");
                for(int i = 0,length = array.length();i < length;i++){
                    JSONObject object = array.optJSONObject(i);
                    arrayAdapter.add(object.optString("name"));
                }
                m_vip_level.setAdapter(arrayAdapter);
                m_vip_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0){
                            JSONObject jsonObject = array.optJSONObject(position);
                            if (jsonObject != null)
                                mPayMethodId = jsonObject.optString("pay_method_id");

                            Logger.d("mPayMethodId:%s",mPayMethodId);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }else {
            MyDialog.ToastMessage("初始化支付方式错误：" + err, mContext);
        }
    }

    private void vip_charge(){
        if (mVip != null){
            mProgressDialog.setMessage("正在充值...").show();
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
                            data_.put("order_money",mC_amt.getText().toString());

                            sz_param = HttpRequest.generate_request_parm(data_,appScret);

                            retJson = httpRequest.sendPost(url + "/api/member/mk_money_order",sz_param,true);

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

                                            //处理充值订单
                                            data_ = new JSONObject();
                                            data_.put("appid",appId);
                                            data_.put("order_code",order_code);
                                            data_.put("case_pay_money",mC_amt.getText().toString());
                                            data_.put("pay_method",mPayMethodId);

                                            url = url + "/api/member/cl_money_order";
                                            sz_param = HttpRequest.generate_request_parm(data_,appScret);
                                            retJson = httpRequest.sendPost(url,sz_param,true);

                                            Logger.json(retJson.toString());
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
                            MyDialog.ToastMessage("参数解析错误：" + e.getMessage(),mContext);
                        }
                    }else{
                        MyDialog.ToastMessage("查询仓库信息错误：" + store_info.optString("info"),mContext);
                    }
                }else{
                    MyDialog.ToastMessage("查询收银员信息错误：" + cashier_info.optString("info"),mContext);
                }
            });
        }else{
            MyDialog.ToastMessage("会员信息不能为空！",mContext);
        }
    }

    public JSONObject getVipInfo(){
        return mVip;
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
                        MyDialog.ToastMessage(msg.obj.toString(),dialog.mContext);
                    break;
                case MessageID.VIP_C_SUCCESS_ID:
                    if (msg.obj instanceof JSONObject){
                        Logger.d_json( msg.obj.toString());

                        dialog.mVip = (JSONObject) msg.obj;

                        if (dialog.mYesOnclickListener != null){
                            dialog.mYesOnclickListener.onYesClick(dialog);
                        }
                    }
                    break;
            }
        }
    }

    public VipChargeDialog setYesOnclickListener(onYesOnclickListener listener) {
        if (listener != null){
            mYesOnclickListener = listener;
        }
        return this;
    }
    public interface onYesOnclickListener {
        void onYesClick(VipChargeDialog dialog);
    }
}
