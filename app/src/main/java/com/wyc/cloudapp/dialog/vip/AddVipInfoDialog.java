package com.wyc.cloudapp.dialog.vip;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class AddVipInfoDialog extends Dialog {
    private Context mContext;
    private EditText m_vip_p_num,m_card_id,m_vip_name,m_vip_birthday;
    private onYesOnclickListener mYesOnclickListener;//确定按钮被点击了的监听器
    private String mVipGradeId,mMemberId;
    private CustomProgressDialog mProgressDialog;
    private Myhandler mHandler;
    private String mAppId,mAppScret,mUrl,mSex;
    private Spinner m_vip_sex;
    private JSONObject mVip;

    AddVipInfoDialog(@NonNull Context context,JSONObject vip,final String url,final String appid,final String appScret) {//如果vip为null则新增会员，否则修改会员
        super(context);
        mContext = context;
        mVip = vip;
        mUrl = url;
        mAppId = appid;
        mAppScret = appScret;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.add_vip_dialog_layout);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(this);

        //初始化成员变量
        m_vip_p_num = findViewById(R.id.n_vip_p_num);
        m_card_id = findViewById(R.id.n_card_id);
        m_vip_name = findViewById(R.id.n_vip_name);
        m_vip_sex = findViewById(R.id.n_vip_sex);

        //初始化按钮
        findViewById(R.id._close).setOnClickListener(view->AddVipInfoDialog.this.dismiss());
        findViewById(R.id.cancel).setOnClickListener(view->AddVipInfoDialog.this.dismiss());
        findViewById(R.id._ok).setOnClickListener(view -> {
            if (m_vip_p_num.length() < 11){
                MyDialog.ToastMessage("请填写会员手机号！",mContext,getWindow());
            }else{
                addVipInfo();
            }
        });

        //初始化生日text
        initVipBirthday();
        //初始化性别
        initVipSex();

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

    public JSONObject getVipInfo(){
        String phone_num = m_vip_p_num.getText().toString();
        if (mVip != null){
            mVip.put("member_id",mMemberId);
        }else{
            mVip = new JSONObject();
            mVip.put("login_pwd",phone_num.substring(phone_num.length() - 6));
        }
        mVip.put("mobile",phone_num);
        mVip.put("name",m_vip_name.getText());
        mVip.put("birthday",m_vip_birthday.getText());
        mVip.put("card_code",m_card_id.getText());
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

    private void initVipBirthday(){
        m_vip_birthday = findViewById(R.id.n_vip_birthday);
        m_vip_birthday.setOnFocusChangeListener((view, b) -> {Utils.hideKeyBoard((EditText) view);if (b)view.callOnClick();});
        m_vip_birthday.setOnClickListener(view->{
            Calendar c = Calendar.getInstance();
            // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
            new DatePickerDialog(view.getContext(),
                    // 绑定监听器
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            m_vip_birthday.setText(String.format(Locale.CHINA,"%d-%02d-%02d", view.getYear(), view.getMonth() + 1,view.getDayOfMonth()));
                        }
                    }
                    // 设置初始日期
                    , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                    .get(Calendar.DAY_OF_MONTH)).show();});
    }
    private void initVipSex(){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);
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
        CustomApplication.execute(()->{
            String url = mUrl + "/api/member/get_member_grade",sz_param;
            JSONObject object = new JSONObject(),ret_json;
            HttpRequest httpRequest = new HttpRequest();
            try {
                object.put("appid",mAppId);
                sz_param = HttpRequest.generate_request_parm(object,mAppScret);
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
        Spinner m_vip_level = findViewById(R.id.n_vip_level);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);

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
            String url = mUrl + "/api/member/mk",sz_param;
            JSONObject object = getVipInfo(),ret_json;

            if (mVip.containsKey("member_id")){
                url = mUrl + "/api/member/up";
            }

            if (object != null){
                HttpRequest httpRequest = new HttpRequest();
                try {
                    object.put("appid",mAppId);
                    sz_param = HttpRequest.generate_request_parm(object,mAppScret);

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
        ((TextView)findViewById(R.id.title)).setText(R.string.modify_vip_sz);
        mMemberId = mVip.getString("member_id");
        m_vip_p_num.setText(mVip.getString("mobile"));
        m_vip_name.setText(mVip.getString("name"));
        m_card_id.setText(mVip.getString("card_code"));
        m_vip_birthday.setText(mVip.getString("birthday"));
    }

    private static class Myhandler extends Handler {
        private WeakReference<AddVipInfoDialog> weakHandler;
        private Myhandler(AddVipInfoDialog dialog){
            this.weakHandler = new WeakReference<>(dialog);
        }
        public void handleMessage(@NonNull Message msg){
            AddVipInfoDialog dialog = weakHandler.get();
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