package com.wyc.cloudapp.dialog.vip;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class AddVipInfoDialog extends AbstractDialogMainActivity {
    private EditText m_vip_p_num_et, m_card_id_et, m_vip_name_et, m_vip_birthday_et;
    private onYesOnclickListener mYesOnclickListener;//确定按钮被点击了的监听器
    private String mVipGradeId,mMemberId;
    private CustomProgressDialog mProgressDialog;
    private Myhandler mHandler;
    private String mSex;
    private Spinner m_vip_sex;
    private JSONObject mVip;

    public AddVipInfoDialog(@NonNull MainActivity context, final String title, final JSONObject vip) {//如果vip为null则新增会员，否则修改会员
        super(context,title);
        mVip = vip;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        m_vip_p_num_et = findViewById(R.id.n_vip_p_num);
        m_card_id_et = findViewById(R.id.n_card_id);
        m_vip_name_et = findViewById(R.id.n_vip_name);
        m_vip_sex = findViewById(R.id.n_vip_sex);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(Looper.myLooper(),this);

        initCancelBtn();
        initOkBtn();
        initVipBirthdayEt();
        initVipSex();

    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.add_vip_dialog_layout;
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        //查询会员级别
        queryVipLevel();
        //显示会员信息
        if (mVip != null){
            showVipInfo();
        }
    }

    public static boolean verifyVipModifyOrAddPermissions(final MainActivity context){
        return context.verifyPermissions("22",null);
    }

    private void initCancelBtn(){
        final Button cancel_btn = findViewById(R.id.cancel);
        cancel_btn.setOnClickListener(view->closeWindow());
    }

    private void initOkBtn(){
        final Button ok_btn = findViewById(R.id._ok);
        if (null != ok_btn)
            ok_btn.setOnClickListener(view -> {
                if (m_vip_p_num_et.length() < 11){
                    MyDialog.ToastMessage("请填写会员手机号！",mContext,getWindow());
                }else{
                    addVipInfo();
                }
            });
    }

    public JSONObject getVipInfo(){
        String phone_num = m_vip_p_num_et.getText().toString();
        if (mVip != null){
            mVip.put("member_id",mMemberId);
        }else{
            mVip = new JSONObject();
            mVip.put("login_pwd",phone_num.substring(phone_num.length() - 6));
        }
        mVip.put("mobile",phone_num);
        mVip.put("name", m_vip_name_et.getText());
        mVip.put("birthday", m_vip_birthday_et.getText());
        mVip.put("card_code", m_card_id_et.getText());
        mVip.put("grade_id",mVipGradeId);
        mVip.put("sex",mSex);
        return mVip;
    }

    public AddVipInfoDialog setYesOnclickListener(onYesOnclickListener listener) {
        if (listener != null){
            mYesOnclickListener = listener;
        }
        return this;
    }
    public interface onYesOnclickListener {
        void onYesClick(AddVipInfoDialog dialog);
    }

    private void initVipBirthdayEt(){
        final EditText et = m_vip_birthday_et = findViewById(R.id.n_vip_birthday);
        et.setOnFocusChangeListener((view, b) -> {Utils.hideKeyBoard((EditText) view);if (b)view.callOnClick();});
        et.setOnClickListener(view->{
            Calendar c = Calendar.getInstance();
            // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
            new DatePickerDialog(view.getContext(),
                    // 绑定监听器
                    (view1, year, monthOfYear, dayOfMonth) -> et.setText(String.format(Locale.CHINA,"%d-%02d-%02d", view1.getYear(), view1.getMonth() + 1, view1.getDayOfMonth()))
                    // 设置初始日期
                    , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                    .get(Calendar.DAY_OF_MONTH)).show();});
    }
    private void initVipSex(){
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);
        final String sz_male = "男",sz_woman = "女";
        if (mVip != null){
            if (sz_male.equals(mVip.getString("sex"))){
                arrayAdapter.add(sz_male);
                arrayAdapter.add(sz_woman);
            }else if(sz_woman.equals(mVip.getString("sex"))){
                arrayAdapter.add(sz_woman);
                arrayAdapter.add(sz_male);
            }else{
                arrayAdapter.add("-");
                arrayAdapter.add(sz_woman);
                arrayAdapter.add(sz_male);
            }
        }else{
            arrayAdapter.add(sz_male);
            arrayAdapter.add(sz_woman);
        }
        m_vip_sex.setAdapter(arrayAdapter);
        m_vip_sex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView)
                    mSex = ((TextView)view).getText().toString();
                else
                    mSex = "-";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void queryVipLevel(){
        mProgressDialog.setMessage("正在查询会员级别...").show();
        mProgressDialog.setCancelable(false);
        CustomApplication.execute(()->{
            String url = mContext.getUrl() + "/api/member/get_member_grade",sz_param;
            JSONObject object = new JSONObject(),ret_json;
            HttpRequest httpRequest = new HttpRequest();
            try {
                object.put("appid",mContext.getAppId());
                sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
                ret_json = httpRequest.sendPost(url,sz_param,true);
                switch (ret_json.getIntValue("flag")){
                    case 0:
                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员级别错误：" + ret_json.getString("info")).sendToTarget();
                        break;
                    case 1:
                        ret_json = JSON.parseObject(ret_json.getString("info"));
                        switch (ret_json.getString("status")){
                            case "n":
                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员级别错误：" + ret_json.getString("info")).sendToTarget();
                                break;
                            case "y":
                                mHandler.obtainMessage(MessageID.QUERY_VIP_LEVEL_ID,JSON.parseArray(ret_json.getString("grade_list"))).sendToTarget();
                                break;
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员级别错误：" + e.getMessage()).sendToTarget();
            }

        });
    }
    private void initVipLevel(final JSONArray array){
        final Spinner m_vip_level = findViewById(R.id.n_vip_level);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.drop_down_style);

        if (array.size() != 0){
            mVipGradeId = array.getJSONObject(0).getString("grade_id");
            for(int i = 0,length = array.size();i < length;i++){
                JSONObject object = array.getJSONObject(i);
                arrayAdapter.add(object.getString("grade_name"));
            }
            m_vip_level.setAdapter(arrayAdapter);
            m_vip_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    JSONObject jsonObject = array.getJSONObject(position);
                    if (jsonObject != null)
                        mVipGradeId = jsonObject.getString("grade_id");
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }
    private void addVipInfo(){
        mProgressDialog.setMessage("正在上传会员信息...").refreshMessage().show();
        CustomApplication.execute(()->{
            final String sz_url = mContext.getUrl();
            String url = sz_url + "/api/member/mk",sz_param;
            JSONObject object = getVipInfo(),ret_json;

            if (mVip.containsKey("member_id")){
                url = sz_url + "/api/member/up";
            }

            if (object != null){
                final HttpRequest httpRequest = new HttpRequest();
                try {
                    object.put("appid",mContext.getAppId());
                    sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());

                    ret_json = httpRequest.sendPost(url,sz_param,true);
                    switch (ret_json.getIntValue("flag")){
                        case 0:
                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"上传会员信息错误：" + ret_json.getString("info")).sendToTarget();
                            break;
                        case 1:
                            ret_json = JSON.parseObject((ret_json.getString("info")));
                            switch (ret_json.getString("status")){
                                case "n":
                                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"上传会员信息错误：" + ret_json.getString("info")).sendToTarget();
                                    break;
                                case "y":
                                    mHandler.obtainMessage(MessageID.ADD_VIP_INFO_ID,ret_json.getString("info")).sendToTarget();
                                    break;
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,"查询会员级别错误：" + e.getMessage()).sendToTarget();
                }
            }
        });
    }
    private void showVipInfo(){
        mMemberId = mVip.getString("member_id");

        m_vip_p_num_et.setText(mVip.getString("mobile"));
        m_vip_p_num_et.setEnabled(false);

        m_vip_name_et.setText(mVip.getString("name"));

        m_card_id_et.setText(mVip.getString("card_code"));
        m_card_id_et.setEnabled(false);

        m_vip_birthday_et.setText(mVip.getString("birthday"));
    }

    private static class Myhandler extends Handler {
        private final WeakReference<AddVipInfoDialog> weakHandler;
        private Myhandler(Looper looper,AddVipInfoDialog dialog){
            super(looper);
            this.weakHandler = new WeakReference<>(dialog);
        }
        public void handleMessage(@NonNull Message msg){
            final AddVipInfoDialog dialog = weakHandler.get();
            if (null == dialog)return;
            if (dialog.mProgressDialog != null && dialog.mProgressDialog.isShowing())dialog.mProgressDialog.dismiss();
            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(msg.obj.toString(),dialog.mContext,dialog.getWindow());
                    break;
                case MessageID.QUERY_VIP_LEVEL_ID:
                    if (msg.obj instanceof JSONArray){
                        dialog.initVipLevel((JSONArray) msg.obj);
                    }
                    break;
                    case MessageID.ADD_VIP_INFO_ID:
                        if (msg.obj instanceof String)
                            MyDialog.ToastMessage(msg.obj.toString(),dialog.mContext,dialog.getWindow());

                        if (dialog.mYesOnclickListener != null){
                            dialog.mYesOnclickListener.onYesClick(dialog);
                        }
                        break;
            }
        }
    }
}
