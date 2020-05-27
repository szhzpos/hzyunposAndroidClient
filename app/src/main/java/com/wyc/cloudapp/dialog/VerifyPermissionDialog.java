package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class VerifyPermissionDialog extends DialogBaseOnMainActivityImp {
    private EditText mCasId;
    private String mPerName;
    public VerifyPermissionDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.per_dialog_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCasIdEt();
        initInfoTv();
        initKeyboard();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.verify_permissions_dilaog_layout;
    }

    @Override
    public void closeWindow(){
        Logger.i("操作员:%s,取消授权.",mContext.getCashierInfo().getString("cas_id"));
        MyDialog.ToastMessage("用户取消授权！",mContext,mContext.getWindow());
        super.closeWindow();
    }

    public void setHintPerName(final String info){
        mPerName = mContext.getString(R.string.per_hint_2_sz,info);
    }
    public String getContent(){
        return mCasId.getText().toString();
    }

    private void initInfoTv(){
        final TextView info_tv = findViewById(R.id.info_tv);
        if (info_tv != null && mPerName != null){
            info_tv.setText(mPerName);
        }
    }
    private void initCasIdEt(){
        final EditText et = findViewById(R.id.cas_id);
        et.setSelectAllOnFocus(true);
        et.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        et.postDelayed(et::requestFocus,300);
        mCasId = et;
    }

    private void initKeyboard(){
        final ConstraintLayout keyboard_linear_layout = findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                final View tmp_v = keyboard_linear_layout.getChildAt(i);
                tmp_v.setOnClickListener(mKeyboardListener);
            }
    }

    private View.OnClickListener mKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int v_id = view.getId();
            final EditText et_view =  mCasId;
            if (null != et_view){
                final Editable editable = et_view.getText();
                int index = et_view.getSelectionStart(),end = et_view.getSelectionEnd();
                switch (v_id){
                    case R.id._back:
                        if (index !=end && end == editable.length()){
                            editable.clear();
                        }else{
                            if (index != 0 && editable.length() != 0)
                                editable.delete(editable.length() - 1,editable.length());
                        }
                        break;
                    case R.id.ok:
                        setExitCode(1);
                        dismiss();
                        break;
                        default:
                            if (et_view.getSelectionStart() != et_view.getSelectionEnd()){
                                editable.replace(0,editable.length(),((Button)view).getText());
                                et_view.setSelection(editable.length());
                            }else
                                editable.append(((Button)view).getText());
                            break;
                }
            }
        }
    };
}
