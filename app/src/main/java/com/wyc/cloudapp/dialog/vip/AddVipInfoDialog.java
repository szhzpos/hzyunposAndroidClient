package com.wyc.cloudapp.dialog.vip;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.logger.Logger;
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
    private int mBirthdayType;
    private VipInfo mVip;
    private JSONArray mVipGrade;

    public AddVipInfoDialog(@NonNull MainActivity context, final String title, final VipInfo vip) {//如果vip为null则新增会员，否则修改会员
        super(context,title);
        mVip = vip;
    }

    public AddVipInfoDialog(@NonNull MainActivity context, final String title, final JSONArray grade) {
        super(context,title);
        mVipGrade = grade;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        m_card_id_et = findViewById(R.id.n_card_id);
        m_vip_name_et = findViewById(R.id.n_vip_name);

        mProgressDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(Looper.myLooper(),this);

        initCancelBtn();
        initOkBtn();
        initVipPhoneNum();
        initVipBirthdayEt();
        initVipSex();
        initBirthdayType();
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.add_vip_dialog_layout;
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        if (mVipGrade == null){
            //查询会员级别
            queryVipLevel();
        }else {
            initVipLevel();
        }

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
                    m_vip_p_num_et.requestFocus();
                    MyDialog.ToastMessage(m_vip_p_num_et.getHint().toString(), getWindow());
                    return;
                }
                if (m_card_id_et.length() == 0){
                    m_card_id_et.requestFocus();
                    MyDialog.ToastMessage(m_card_id_et.getHint().toString(), getWindow());
                    return;
                }
                if (m_vip_birthday_et.length() == 0){
                    m_vip_birthday_et.requestFocus();
                    MyDialog.ToastMessage(m_vip_birthday_et.getHint().toString(), getWindow());
                    return;
                }

                addVipInfo();
            });
    }

    public VipInfo getVipInfo(){
        String phone_num = m_vip_p_num_et.getText().toString();
        if (mMemberId != null){
            mVip.setMember_id(Integer.parseInt(mMemberId));
        }else{
            mVip = new VipInfo();
            mVip.setLogin_pwd(phone_num.substring(phone_num.length() - 6));
        }
        mVip.setMobile(phone_num);
        mVip.setName(m_vip_name_et.getText().toString());
        mVip.setBirthday_type(mBirthdayType);
        mVip.setBirthday(m_vip_birthday_et.getText().toString());
        mVip.setCard_code(m_card_id_et.getText().toString());
        mVip.setGrade_id(Integer.parseInt(mVipGradeId));
        mVip.setSex(mSex);
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

    private void initVipPhoneNum(){
        m_vip_p_num_et = findViewById(R.id.n_vip_p_num);
        m_vip_p_num_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mVip == null){
                    m_card_id_et.setText(s);
                }
            }
        });
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
            if (sz_male.equals(mVip.getSex())){
                arrayAdapter.add(sz_male);
                arrayAdapter.add(sz_woman);
            }else if(sz_woman.equals(mVip.getSex())){
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
        Spinner m_vip_sex = findViewById(R.id.n_vip_sex);
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

    private void initBirthdayType(){
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);

        arrayAdapter.add("农历");
        arrayAdapter.add("新历");

        Spinner b_type = findViewById(R.id.birthday_type);
        b_type.setAdapter(arrayAdapter);
        b_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    mBirthdayType = 2;
                }else {
                    mBirthdayType = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (mVip != null){
            if (1 == mVip.getBirthday_type()){
                b_type.setSelection(1);
            }else{
                b_type.setSelection(0);
            }
        }
    }

    private void queryVipLevel(){
        mProgressDialog.setMessage(mContext.getString(R.string.query_vip_grade_hint)).show();
        mProgressDialog.setCancelable(false);
        CustomApplication.execute(()->{
            String url = mContext.getUrl() + InterfaceURL.VIP_GRADE,sz_param;
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
                                mVipGrade = JSON.parseArray(ret_json.getString("data"));
                                mHandler.obtainMessage(MessageID.QUERY_VIP_LEVEL_ID).sendToTarget();
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
    private void initVipLevel(){
        final Spinner m_vip_level = findViewById(R.id.n_vip_level);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.drop_down_style);

        final JSONArray array = mVipGrade;
        if (array != null){
            int selectIndex = 0,size = array.size();
            if (size != 0){
                for(int i = 0;i < size;i++){
                    final JSONObject object = array.getJSONObject(i);
                    arrayAdapter.add(object.getString("grade_name"));
                    if (object.containsKey("sel")){
                        selectIndex = i;
                    }
                }
                m_vip_level.setAdapter(arrayAdapter);
                m_vip_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        mVipGradeId = Utils.getNullStringAsEmpty(array.getJSONObject(position),"grade_id");
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                m_vip_level.setSelection(selectIndex);
            }
        }
    }
    private void addVipInfo(){
        mProgressDialog.setMessage("正在上传会员信息...").refreshMessage().show();
        CustomApplication.execute(()->{
            final String sz_url = mContext.getUrl();
            String url = sz_url + "/api/member/mk",sz_param;
            JSONObject object = (JSONObject) JSON.toJSON(getVipInfo()),ret_json;

            if (mMemberId != null){
                url = sz_url + "/api/member/up";
                object.put("member_id",mMemberId);
            }

            if (object != null){
                final HttpRequest httpRequest = new HttpRequest();
                try {
                    object.put("appid",mContext.getAppId());
                    sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());

                    Logger.d(sz_param);

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
        mMemberId = String.valueOf(mVip.getMember_id());

        m_vip_p_num_et.setText(mVip.getMobile());
        m_vip_p_num_et.setEnabled(false);

        m_vip_name_et.setText(mVip.getName());

        m_card_id_et.setText(mVip.getCard_code());
        m_card_id_et.setEnabled(false);

        m_vip_birthday_et.setText(mVip.getBirthday());
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
                        MyDialog.ToastMessage(msg.obj.toString(), dialog.getWindow());
                    break;
                case MessageID.QUERY_VIP_LEVEL_ID:
                    dialog.initVipLevel();
                    break;
                    case MessageID.ADD_VIP_INFO_ID:
                        if (msg.obj instanceof String)
                            MyDialog.ToastMessage(msg.obj.toString(), dialog.getWindow());

                        if (dialog.mYesOnclickListener != null){
                            dialog.mYesOnclickListener.onYesClick(dialog);
                        }
                        break;
            }
        }
    }
}
