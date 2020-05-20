package com.wyc.cloudapp.dialog.vip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.print.PrintUtilsToBitbmp;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class VipInfoDialog extends DialogBaseOnMainActivityImp {
    private EditText mSearchContent;
    private CustomProgressDialog mProgressDialog;
    private Myhandler mHandler;
    private JSONObject mVip;
    private TextView mVip_name,mVip_sex,mVip_p_num,mVip_card_id,mVip_balance,mVip_integral;
    private Button mSearchBtn;
    private onYesOnclickListener mYesOnclickListener;//确定按钮被点击了的监听器
    private boolean mPrintStatus = true;
    public VipInfoDialog(@NonNull MainActivity context) {
        super(context,context.getString(R.string.vip_dialog_title_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.vip_info_dialog_layout);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(this);

        //初始化成员变量
        mVip_name = findViewById(R.id.vip_name);
        mVip_sex = findViewById(R.id.vip_sex);
        mVip_p_num = findViewById(R.id.vip_p_num);
        mVip_card_id = findViewById(R.id.vip_card_id);
        mVip_balance = findViewById(R.id.vip_balance);
        mVip_integral = findViewById(R.id.vip_integral);

        //初始化搜索条件输入框
        initSearchCondition();
        initAddVipBtn();
        initModifyBtn();
        initPrinterStatus();
        initChargeBtn();

        //初始化数字键盘
        initKeyboard();

    }

    @Override
    public void onAttachedToWindow(){

    }

    @Override
    public void onDetachedFromWindow(){

    }

    @Override
    public void keyListenerCallBack(){
        if (mSearchBtn != null)mSearchBtn.callOnClick();
    }

    private void initChargeBtn(){
        final Button chargeBtn = findViewById(R.id.vip_charge);
        if (null != chargeBtn)
            chargeBtn.setOnClickListener(view -> {
                if (mVip != null){
                    VipChargeDialogImp vipChargeDialogImp = new VipChargeDialogImp(mContext,mVip,mPrintStatus);
                    vipChargeDialogImp.setYesOnclickListener(dialog -> {
                        showVipInfo(dialog.getContent());
                        dialog.dismiss();
                    }).show();
                }else{
                    serchVip(mSearchContent.getText().toString(),chargeBtn.getId());
                }
            });
    }
    private void initPrinterStatus(){
        final ImageView imageView =  findViewById(R.id.v_printer_status);
        if (null != imageView){
            imageView.setOnClickListener(v -> {
                Bitmap printer = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.printer);
                if (mPrintStatus){
                    mPrintStatus = false;
                    imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorSignToBitmap(printer,15,15));
                    MyDialog.ToastMessage(imageView,"打印功能已关闭！",mContext,getWindow());
                }else{
                    mPrintStatus = true;
                    imageView.setImageBitmap(printer);
                    MyDialog.ToastMessage(imageView,"打印功能已开启！",mContext,getWindow());
                }
            });//打印状态
        }
    }
    private void initModifyBtn(){
        final Button modifiyBtn = findViewById(R.id.vip_modify);
        if (null != modifiyBtn)
            modifiyBtn.setOnClickListener(view -> {
                if (mVip != null){
                    AddVipInfoDialog dialog = new AddVipInfoDialog(mContext,mContext.getString(R.string.modify_vip_sz),mVip);
                    dialog.setOnShowListener(dialog12 -> mSearchContent.clearFocus());
                    dialog.setOnDismissListener(dialog1 -> mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300));
                    dialog.setYesOnclickListener(dialog14 -> {
                        showVipInfo(dialog14.getVipInfo());
                        dialog14.dismiss();
                    }).show();
                }else{
                    serchVip(mSearchContent.getText().toString(),modifiyBtn.getId());
                }
            });
    }

    private void initAddVipBtn(){
        final Button add_btn = findViewById(R.id.vip_add);
        if (null != add_btn)
            add_btn.setOnClickListener(view -> {
                AddVipInfoDialog dialog = new AddVipInfoDialog(mContext,mContext.getString(R.string.add_vip_sz),null);
                dialog.setOnShowListener(dialog12 -> mSearchContent.clearFocus());
                dialog.setOnDismissListener(dialog1 -> mSearchContent.postDelayed(()->{mSearchContent.requestFocus();},300));
                dialog.setYesOnclickListener(dialog13 -> {
                    JSONObject jsonObject = dialog13.getVipInfo();
                    if (jsonObject != null){
                        mSearchContent.setText(jsonObject.getString("mobile"));
                        mSearchBtn.callOnClick();
                    }
                    dialog13.dismiss();
                }).show();
            });
    }

    private void initKeyboard(){
        final ConstraintLayout keyboard_linear_layout = findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                View tmp_v = keyboard_linear_layout.getChildAt(i);
                int id = tmp_v.getId();
                if (tmp_v instanceof Button){
                    switch (id) {
                        case R.id._back:
                            tmp_v.setOnClickListener(v -> {
                                View view = getCurrentFocus();
                                if (view != null) {
                                    if (view.getId() == R.id.search_content) {
                                        EditText tmp_edit = ((EditText) view);
                                        int index = tmp_edit.getSelectionStart(), end = tmp_edit.getSelectionEnd();
                                        if (index != end && end == tmp_edit.getText().length()) {
                                            tmp_edit.setText(mContext.getString(R.string.space_sz));
                                        } else {
                                            if (index == 0) return;
                                            tmp_edit.getText().delete(index - 1, index);
                                        }
                                    }
                                }
                            });
                            break;
                        case R.id._ok:
                            mSearchBtn = (Button) tmp_v;
                            tmp_v.setOnClickListener(view -> {
                                if (mVip == null)
                                    serchVip(mSearchContent.getText().toString(), 0);
                                else {
                                    if (mYesOnclickListener != null)
                                        mYesOnclickListener.onYesClick(VipInfoDialog.this);
                                }
                            });
                            break;
                        default:
                            tmp_v.setOnClickListener(button_click);
                            break;
                    }
                }
            }
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

    public static JSONArray serchVip(final String mobile) throws JSONException {
        JSONObject object = new JSONObject(),ret_json;
        HttpRequest httpRequest = new HttpRequest();
        JSONArray vips = null;
        if (SQLiteHelper.getLocalParameter("connParam",object)){
            object.put("appid",object.getString("appId"));
            object.put("mobile",mobile);
            ret_json = httpRequest.sendPost(object.getString("server_url") + "/api/member/get_member_info",HttpRequest.generate_request_parm(object,object.getString("appScret")),true);
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
        }else{
            throw new JSONException(object.getString("info"));
        }
        return vips;
    }

    private void serchVip(final String ph_num, int btn_id){
        if(ph_num != null && ph_num.length() != 0){
            mProgressDialog.setMessage("正在查询会员...").show();
             CustomApplication.execute(()->{
                try {
                    mHandler.obtainMessage(MessageID.QUERY_VIP_INFO_ID,btn_id,0,serchVip(ph_num)).sendToTarget();
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员信息错误：" + e.getMessage()).sendToTarget();
                }

            });
        }else{
            MyDialog.ToastMessage(mSearchContent,mSearchContent.getHint().toString(),mContext,getWindow());
        }
    }

    private void showVipInfo(JSONObject object){
        mVip = object;
        mSearchBtn.setText(mContext.getString(R.string.OK));
        mVip_name.setText(object.getString("name"));
        mVip_sex.setText(object.getString("sex"));
        mVip_p_num.setText(object.getString("mobile"));
        mVip_card_id.setText(object.getString("card_code"));
        mVip_balance.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("money_sum")));
        mVip_integral.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("points_sum")));
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
                        if (array.size() == 1){
                            dialog.showVipInfo(array.getJSONObject(0));

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
