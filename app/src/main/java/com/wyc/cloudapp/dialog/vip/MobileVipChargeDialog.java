package com.wyc.cloudapp.dialog.vip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;

import java.util.Locale;

public class MobileVipChargeDialog extends AbstractDialogMainActivity {
    private View mRoot;
    private JSONObject mVip;
    private EditText mSearchContent,mChargeAmtEt,mRemarkEt;
    private TextView mVip_name,mVip_sex,mVip_p_num,mVip_card_id,mVip_balance,mVip_integral,mVipGrade,mVipDiscount;
    private CustomProgressDialog mProgressDialog;
    public MobileVipChargeDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.vip_charge_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(mContext);

        initVipInfoFields();
        initSearchContent();
        initChargeAmt();
        initRemark();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_vip_charge_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        final Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRoot = window.getDecorView();
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    private void initVipInfoFields(){
        mVip_name = findViewById(R.id.vip_name);
        mVip_sex = findViewById(R.id.vip_sex);
        mVip_p_num = findViewById(R.id.vip_p_num);
        mVip_card_id = findViewById(R.id.vip_card_id);
        mVip_balance = findViewById(R.id.vip_balance);
        mVip_integral = findViewById(R.id.vip_integral);
        mVipGrade = findViewById(R.id.vip_grade_tv);
        mVipDiscount = findViewById(R.id.vip_discount);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText _search_content = findViewById(R.id.m_search_content);
        _search_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    final InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        _search_content.addTextChangedListener(new TextWatcher() {
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
        _search_content.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getX() > (_search_content.getWidth() - _search_content.getCompoundPaddingRight())){
                        searchVip(_search_content.getText().toString());
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        });
        mSearchContent = _search_content;
    }

    private void searchVip(final String mobile){
        if(mobile != null && mobile.length() != 0){
            mProgressDialog.setMessage("正在查询会员...").show();
            CustomApplication.execute(()->{
                try {
                    final JSONArray array = VipInfoDialog.searchVip(mobile);
                    mRoot.post(()-> showVipInfo(array.getJSONObject(0)));
                } catch (JSONException e) {
                    e.printStackTrace();
                    mRoot.post(()->{
                        MyDialog.ToastMessage(e.getMessage(),mContext,getWindow());
                    });
                }
                mRoot.post(()->mProgressDialog.dismiss());
            });
        }else{
            MyDialog.ToastMessage(mSearchContent,mSearchContent.getHint().toString(),mContext,getWindow());
        }
    }

    private void showVipInfo(JSONObject object){
        if (null != object){
            mVip = object;

            final String grade_name = object.getString("grade_name");
            if (grade_name != null)mVipGrade.setText(grade_name);

            mVip_name.setText(object.getString("name"));
            mVip_sex.setText(object.getString("sex"));
            mVip_p_num.setText(object.getString("mobile"));

            mVipDiscount.setText(object.getString("discount"));
            mVip_card_id.setText(object.getString("card_code"));
            mVip_balance.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("money_sum")));
            mVip_integral.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("points_sum")));
        }
    }

    private void clearVipInfo(){
        if (mVip != null){
            final CharSequence space = mContext.getText(R.string.space_sz);
            mVip_name.setText(space);
            mVip_sex.setText(space);
            mVip_p_num.setText(space);
            mVipGrade.setText(space);
            mVipDiscount.setText(space);
            mVip_card_id.setText(space);
            mVip_balance.setText(space);
            mVip_integral.setText(space);
        }
    }

    private void initChargeAmt(){
        mChargeAmtEt = findViewById(R.id.mobile_charge_amt);
    }

    private void initRemark(){
        mRemarkEt = findViewById(R.id.mobile_charge_remark);
    }



    private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        private int rootViewVisibleHeight;
        private boolean isShow = true;
        @Override
        public void onGlobalLayout() {
            //获取当前根视图在屏幕上显示的大小
            final Rect r = new Rect();
            //获取rootView在窗体的可视区域
            mRoot.getWindowVisibleDisplayFrame(r);
            int visibleHeight = r.height();
            if (rootViewVisibleHeight == 0) {
                rootViewVisibleHeight = visibleHeight;
                return;
            }

            //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
            if (rootViewVisibleHeight == visibleHeight) {
                return;
            }

            //根视图显示高度变小超过200，可以看作软键盘显示了
            if (rootViewVisibleHeight - visibleHeight > 200) {
                if (mSearchContent != getCurrentFocus()){
                    mRoot.scrollBy(0,rootViewVisibleHeight - visibleHeight);
                    isShow = true;
                }
                rootViewVisibleHeight = visibleHeight;
                return;
            }

            //根视图显示高度变大超过200，可以看作软键盘隐藏了
            if (visibleHeight - rootViewVisibleHeight > 200) {
                if (isShow){
                    mRoot.scrollBy(0,-(visibleHeight - rootViewVisibleHeight));
                    isShow = false;
                }
                rootViewVisibleHeight = visibleHeight;
            }
        }
    };
}
