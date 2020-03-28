package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class VipInfoDialog extends Dialog {
    private EditText mSearchContent;
    private String mAppId,mAppScret,mUrl;
    private CustomProgressDialog mProgressDialog;
    private Context mContext;
    private Myhandler mHandler;
    private JSONObject mVip;
    private TextView mVip_name,mVip_sex,mVip_p_num,mVip_card_id,mVip_balance,mVip_integral;
    private Button mSearchBtn,mModfiyBtn,mChargeBtn,mAddBtn;
    private onYesOnclickListener mYesOnclickListener;//确定按钮被点击了的监听器
    public VipInfoDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.vip_info_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(this);

        //初始化成员变量
        mVip_name = findViewById(R.id.vip_name);
        mVip_sex = findViewById(R.id.vip_sex);
        mVip_p_num = findViewById(R.id.vip_p_num);
        mVip_card_id = findViewById(R.id.vip_card_id);
        mVip_balance = findViewById(R.id.vip_balance);
        mVip_integral = findViewById(R.id.vip_integral);

        //初始化按钮
        mSearchBtn = findViewById(R.id._ok);
        mModfiyBtn = findViewById(R.id.vip_modify);
        mChargeBtn = findViewById(R.id.vip_charge);
        mAddBtn = findViewById(R.id.vip_add);

        //初始化连接参数
        if (!initConnParam()){
            return;
        }

        //初始化搜索条件输入框
        initSearchCondition();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener( v ->VipInfoDialog.this.dismiss());
        findViewById(R.id._back).setOnClickListener(v -> {
            View view =  getCurrentFocus();
            if (view != null) {
                if (view.getId() == R.id.search_content) {
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
        mSearchBtn.setOnClickListener(view -> {
            if (mVip == null)
                serchVip(mSearchContent.getText().toString(),0);
            else{
                if (mYesOnclickListener != null)mYesOnclickListener.onYesClick(VipInfoDialog.this);
            }
        });
        mAddBtn.setOnClickListener(view -> {
            AddVipInfoDialog dialog = new AddVipInfoDialog(mContext,null,mUrl,mAppId,mAppScret);
            dialog.setOnShowListener(dialog12 -> mSearchContent.clearFocus());
            dialog.setOnDismissListener(dialog1 -> mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300));
            dialog.setYesOnclickListener(dialog13 -> {
                JSONObject jsonObject = dialog13.getVipInfo();
                if (jsonObject != null){
                    mSearchContent.setText(jsonObject.optString("mobile"));
                    mSearchBtn.callOnClick();
                }
                dialog13.dismiss();
            }).show();
        });
        mModfiyBtn.setOnClickListener(view -> {
            if (mVip != null){
                AddVipInfoDialog dialog = new AddVipInfoDialog(mContext,mVip,mUrl,mAppId,mAppScret);
                dialog.setOnShowListener(dialog12 -> mSearchContent.clearFocus());
                dialog.setOnDismissListener(dialog1 -> mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300));
                dialog.setYesOnclickListener(dialog14 -> {
                    showVipInfo(dialog14.getVipInfo());
                    dialog14.dismiss();
                }).show();
            }else{
                serchVip(mSearchContent.getText().toString(),mModfiyBtn.getId());
            }
        });
        mChargeBtn.setOnClickListener(view -> {
            if (mVip != null){
                VipChargeDialog vipChargeDialog = new VipChargeDialog(mContext,mVip);
                vipChargeDialog.setYesOnclickListener(dialog -> {
                    showVipInfo(dialog.getContent());
                    dialog.dismiss();
                }).show();
            }else{
                serchVip(mSearchContent.getText().toString(),mChargeBtn.getId());
            }
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id.cancel || id == R.id._ok)){
                tmp_v.setOnClickListener(button_click);
            }
        }

    }

    @Override
    public void onAttachedToWindow(){

    }

    @Override
    public void onDetachedFromWindow(){

    }

    private void initSearchCondition(){
        mSearchContent = findViewById(R.id.search_content);
        mSearchContent.setSelectAllOnFocus(true);
        mSearchContent.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
        mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300);
        mSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                clearVipInfo();
            }
        });
        mSearchContent.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getX() > (mSearchContent.getWidth() - mSearchContent.getCompoundPaddingRight())){
                        mSearchBtn.callOnClick();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        });
    }
    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.search_content) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };

    public void serchVip(final String ph_num,int btn_id){
        if(ph_num != null && ph_num.length() != 0){
            mProgressDialog.setMessage("正在查询会员...").show();
            CustomApplication.execute(()->{
                String url = mUrl + "/api/member/get_member_info",sz_param;
                JSONObject object = new JSONObject(),ret_json;
                HttpRequest httpRequest = new HttpRequest();
                try {
                    object.put("appid",mAppId);
                    object.put("mobile",ph_num);
                    sz_param = HttpRequest.generate_request_parm(object,mAppScret);
                    ret_json = httpRequest.sendPost(url,sz_param,true);
                    switch (ret_json.getInt("flag")){
                        case 0:
                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员错误：" + ret_json.optString("info")).sendToTarget();
                            break;
                        case 1:
                            ret_json = new JSONObject(ret_json.getString("info"));
                            switch (ret_json.getString("status")){
                                case "n":
                                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员错误：" + ret_json.optString("info")).sendToTarget();
                                    break;
                                case "y":
                                    mHandler.obtainMessage(MessageID.QUERY_VIP_INFO_ID,btn_id,0,new JSONArray(ret_json.getString("list"))).sendToTarget();
                                    break;
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员级别错误：" + e.getMessage()).sendToTarget();
                }

            });
        }else{
            MyDialog.ToastMessage(mSearchContent.getHint().toString(),mContext,getWindow());
        }
    }

    private boolean initConnParam(){
        JSONObject conn = new JSONObject();
        boolean code = true;
        if (SQLiteHelper.getLocalParameter("connParam",conn)){
            mUrl = conn.optString("server_url");
            mAppId = conn.optString("appId");
            mAppScret = conn.optString("appScret");
        }else{
            code = false;
            MyDialog.ToastMessage("查询连接参数错误：" + conn.optString("info"),mContext,getWindow());
        }
        return code;
    }

    private void showVipInfo(JSONObject object){
        mVip = object;
        mSearchBtn.setText(mContext.getString(R.string.OK));
        mVip_name.setText(object.optString("name"));
        mVip_sex.setText(object.optString("sex"));
        mVip_p_num.setText(object.optString("mobile"));
        mVip_card_id.setText(object.optString("card_code"));
        mVip_balance.setText(object.optString("money_sum","0.00"));
        mVip_integral.setText(object.optString("points_sum","0.0"));
    }

    private void clearVipInfo(){
        if (mVip != null){
            mSearchBtn.setText(mContext.getString(R.string.search_sz));
            mVip = null;
            mVip_name.setText(mContext.getText(R.string.space_sz));
            mVip_sex.setText(mContext.getText(R.string.space_sz));
            mVip_p_num.setText(mContext.getText(R.string.space_sz));
            mVip_card_id.setText(mContext.getText(R.string.space_sz));
            mVip_balance.setText(mContext.getText(R.string.space_sz));
            mVip_integral.setText(mContext.getText(R.string.space_sz));
        }
    }

    public JSONObject getVip(){
        return mVip;
    }

    public VipInfoDialog setYesOnclickListener(onYesOnclickListener listener) {
        if (listener != null){
            mYesOnclickListener = listener;
        }
        return this;
    }

    public interface onYesOnclickListener {
        void onYesClick(VipInfoDialog dialog);
    }

    private static class Myhandler extends Handler {
        private WeakReference<VipInfoDialog> weakHandler;
        private Myhandler(VipInfoDialog dialog){
            this.weakHandler = new WeakReference<>(dialog);
        }
        public void handleMessage(@NonNull Message msg){
            VipInfoDialog dialog = weakHandler.get();
            if (null == dialog)return;
            if (dialog.mProgressDialog != null && dialog.mProgressDialog.isShowing())dialog.mProgressDialog.dismiss();
            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(msg.obj.toString(),dialog.mContext,dialog.getWindow());
                    break;
                case MessageID.QUERY_VIP_INFO_ID:
                    if (msg.obj instanceof JSONArray){
                        JSONArray array = (JSONArray)msg.obj;
                        if (array.length() == 1){
                            dialog.showVipInfo(array.optJSONObject(0));

                            //触发修改或充值按钮点击事件；做这两个操作之前客户可能没查询会员，必须先查询再操作。
                            Button btn = dialog.findViewById(msg.arg1);
                            if (btn != null)btn.callOnClick();
                        }else{//提示选择对话框

                        }
                    }
                    break;
            }
        }
    }

}
