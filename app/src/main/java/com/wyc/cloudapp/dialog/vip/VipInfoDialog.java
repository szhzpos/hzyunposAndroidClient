package com.wyc.cloudapp.dialog.vip;

import android.annotation.SuppressLint;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.customizationView.KeyboardView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.lang.ref.WeakReference;
import java.util.Locale;

public final class VipInfoDialog extends AbstractDialogMainActivity {
    private EditText mSearchContent;
    private CustomProgressDialog mProgressDialog;
    private Myhandler mHandler;
    @Deprecated
    private JSONObject mVip;
    private VipInfo mVObj;
    private TextView mVip_name,mVip_sex,mVip_p_num,mVip_card_id,mVip_balance,mVip_integral,mVipGrade,mVipDiscount;
    private Button mSearchBtn;
    private onYesOnclickListener mYesOnclickListener;//确定按钮被点击了的监听器
    private Button mAddBtn;
    public VipInfoDialog(@NonNull MainActivity context) {
        super(context,context.getString(R.string.vip_dialog_title_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(this);

        //初始化成员变量
        mVip_name = findViewById(R.id.vip_name);
        mVip_sex = findViewById(R.id.vip_sex);
        mVip_p_num = findViewById(R.id.vip_p_num);
        mVip_card_id = findViewById(R.id.vip_card_id);
        mVip_balance = findViewById(R.id.vip_balance);
        mVip_integral = findViewById(R.id.vip_integral);
        mVipGrade = findViewById(R.id.vip_grade_tv);
        mVipDiscount = findViewById(R.id.vip_discount);

        //初始化搜索条件输入框
        initSearchCondition();
        initAddVipBtn();
        initModifyBtn();
        initChargeBtn();

        //初始化数字键盘
        initKeyboardView();

    }
    @Override
    protected int getContentLayoutId(){
        if (mContext.lessThan7Inches()){
            return R.layout.mobile_vip_info_dialog_layout;
        }else
            return R.layout.vip_info_dialog_layout;
    }
    @Override
    public void onAttachedToWindow(){
        if (CustomApplication.isPracticeMode()){
            MyDialog.toastMessage(mContext.getString(R.string.not_enter_practice));
            dismiss();
            return;
        }
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
    }

    private void initChargeBtn(){
        final Button chargeBtn = findViewById(R.id.vip_charge);
        if (null != chargeBtn)
            chargeBtn.setOnClickListener(view -> {
                if (verifyVipDepositPermissions(mContext)){
                    if (mVip != null){
                        final AbstractVipChargeDialog chargeDialog = new NormalVipChargeDialog(mContext,mVObj);
                        chargeDialog.exec();
                        showVipInfo(chargeDialog.getVip());
                    }else{
                        searchVip(mSearchContent.getText().toString(),chargeBtn.getId());
                    }
                }
            });
    }


    private void initModifyBtn(){
        final Button modifiyBtn = findViewById(R.id.vip_modify);
        if (null != modifiyBtn)
            modifiyBtn.setOnClickListener(view -> {
                if (AddVipInfoDialog.verifyVipModifyOrAddPermissions(mContext)){
                    if (mVip != null){
                        final AddVipInfoDialog dialog = new AddVipInfoDialog(mContext,mContext.getString(R.string.modify_vip_sz),mVObj);
                        dialog.setOnShowListener(dialog12 -> mSearchContent.clearFocus());
                        dialog.setOnDismissListener(dialog1 -> mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300));
                        dialog.setYesOnclickListener(dialog14 -> {
                            showVipInfo(dialog14.getVipInfo());
                            dialog14.dismiss();
                        }).show();
                    }else{
                        searchVip(mSearchContent.getText().toString(),modifiyBtn.getId());
                    }
                }
            });
    }

    private void initAddVipBtn(){
        final Button add_btn = findViewById(R.id.vip_add);
        if (null != add_btn){
            add_btn.setOnClickListener(view -> {
                if (AddVipInfoDialog.verifyVipModifyOrAddPermissions(mContext)){
                    final AddVipInfoDialog dialog = new AddVipInfoDialog(mContext,mContext.getString(R.string.add_vip_sz),(VipInfo) null);
                    dialog.setOnShowListener(dialog12 -> mSearchContent.clearFocus());
                    dialog.setOnDismissListener(dialog1 -> mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300));
                    dialog.setYesOnclickListener(dialog13 -> {
                        VipInfo jsonObject = dialog13.getVipInfo();
                        if (jsonObject != null){
                            mSearchContent.setText(jsonObject.getMobile());
                            mSearchBtn.callOnClick();
                        }
                        dialog13.dismiss();
                    }).show();
                }
            });
            mAddBtn = add_btn;
        }
    }

    private void initKeyboardView(){
        final KeyboardView view = findViewById(R.id.keyboard_view);
        view.layout(R.layout.keyboard_layout);
        view.setCurrentFocusListener(() -> {
            final View focus = getCurrentFocus();
            if (focus instanceof EditText){
                return (EditText) focus;
            }
            return null;
        });
        view.setCancelListener(v -> closeWindow());
        view.setOkListener(v -> {
            if (mVip == null)
                searchVip(mSearchContent.getText().toString(), 0);
            else {
                if (mYesOnclickListener != null)
                    mYesOnclickListener.onYesClick(VipInfoDialog.this);
            }
        });
        mSearchBtn =  view.getOkBtn();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchCondition(){
        mSearchContent = findViewById(R.id.search_content);
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

    public static JSONArray searchVip(final String mobile) throws JSONException {
        JSONObject object = CustomApplication.getConnParam(),ret_json;
        final HttpRequest httpRequest = new HttpRequest();
        JSONArray vips = null;
        object.put("appid",object.getString("appId"));
        object.put("mobile",mobile);
        ret_json = httpRequest.sendPost(object.getString("server_url") + "/api/member/get_member_info",HttpRequest.generate_request_parm(object,object.getString("appSecret")),true);
        switch (ret_json.getIntValue("flag")){
            case 0:
                throw new JSONException(ret_json.getString("info"));
            case 1:
                ret_json = JSON.parseObject(ret_json.getString("info"));
                switch (ret_json.getString("status")){
                    case "n":
                        throw new JSONException(ret_json.getString("info"));
                    case "y":
                        vips = JSON.parseArray(ret_json.getString("list"));
                        break;
                }
                break;
        }
        return vips;
    }

    private void searchVip(final String ph_num, int btn_id){
        if(ph_num != null && ph_num.length() != 0){
            mProgressDialog.setMessage("正在查询会员...").show();
             CustomApplication.execute(()->{
                try {
                    mHandler.obtainMessage(MessageID.QUERY_VIP_INFO_ID,btn_id,0, searchVip(ph_num)).sendToTarget();
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员信息错误：" + e.getMessage()).sendToTarget();
                }

            });
        }else{
            MyDialog.ToastMessage(mSearchContent,mSearchContent.getHint().toString(), getWindow());
        }
    }

    private void showVipInfo(VipInfo object){
        if (null != object){
            Logger.d(object);
            mVip = (JSONObject) JSON.toJSON(object);
            mVObj = object;

            mVipGrade.setText(object.getGradeName());

            mSearchBtn.setText(mContext.getString(R.string.OK));
            mVip_name.setText(object.getName());
            mVip_sex.setText(object.getSex());
            mVip_p_num.setText(object.getMobile());

            mVipDiscount.setText(String.valueOf(object.getDiscount()));
            mVip_card_id.setText(object.getCard_code());
            mVip_balance.setText(String.format(Locale.CHINA,"%.2f",object.getMoney_sum()));
            mVip_integral.setText(String.format(Locale.CHINA,"%.2f",object.getPoints_sum()));

            showAddBtn(View.GONE);
        }
    }

    private void showAddBtn(int i){
        final View space = findViewById(R.id.add_space);
        if (space != null && space.getVisibility() != i)space.setVisibility(i);
        if (mAddBtn != null && mAddBtn.getVisibility() != i)mAddBtn.setVisibility(i);
    }

    private void clearVipInfo(){
        if (mVip != null){
            final CharSequence space = mContext.getText(R.string.space_sz);
            mSearchBtn.setText(mContext.getString(R.string.search_sz));
            mVip = null;
            mVip_name.setText(space);
            mVip_sex.setText(space);
            mVip_p_num.setText(space);
            mVipGrade.setText(space);
            mVipDiscount.setText(space);
            mVip_card_id.setText(space);
            mVip_balance.setText(space);
            mVip_integral.setText(space);
            showAddBtn(View.VISIBLE);
        }
    }

    @Deprecated
    public JSONObject getVip(){
        return mVip;
    }
    public VipInfo getVipBean(){
        return mVObj;
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

    public static boolean verifyVipDepositOrderPermissions(final @NonNull MainActivity context){
        return context.verifyPermissions("24",null);
    }

    public static boolean verifyVipDepositPermissions(final @NonNull MainActivity context){
        return context.verifyPermissions("23",null);
    }

    private static class Myhandler extends Handler {
        private final WeakReference<VipInfoDialog> weakHandler;
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
                        MyDialog.ToastMessage(msg.obj.toString(), dialog.getWindow());
                    break;
                case MessageID.QUERY_VIP_INFO_ID:
                    if (msg.obj instanceof JSONArray){
                        final JSONArray array = (JSONArray)msg.obj;
                        if (array.size() == 1){
                            Logger.d_json(array.getJSONObject(0));
                            dialog.showVipInfo(array.getJSONObject(0).toJavaObject(VipInfo.class));

                            //触发修改或充值按钮点击事件；做这两个操作之前客户可能没查询会员，必须先查询再操作。
                            final Button btn = dialog.findViewById(msg.arg1);
                            if (btn != null)btn.callOnClick();
                        }else{//提示选择对话框

                        }
                    }
                    break;
            }
        }
    }

}
