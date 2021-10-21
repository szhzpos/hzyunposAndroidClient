package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.CustomizationView.KeyboardView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;

public final class VerifyPermissionDialog extends AbstractDialogMainActivity {
    private EditText mCasContent;
    private String mPerName;
    private Button mOkBtn;
    private OnFinishListener mFinishListener;

    public VerifyPermissionDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.per_dialog_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCasIdEt();
        initInfoTv();
        initKeyboardView();
    }

    @Override
    protected int getContentLayoutId() {
        if (mContext.lessThan7Inches())return R.layout.mobile_verify_permissions_dilaog_layout;
        return R.layout.verify_permissions_dilaog_layout;
    }

    @Override
    public void closeWindow(){
        Logger.i("操作员:%s,取消授权.",mContext.getCashierCode());
        MyDialog.ToastMessage("您取消了授权！", getWindow());
        super.closeWindow();
    }

    public void keyListenerCallBack(){
        if (mOkBtn != null)mOkBtn.callOnClick();
    }

    public void setHintPerName(final String info){
        mPerName = mContext.getString(R.string.per_hint_2_sz,info);
    }
    public String getContent(){
        return mCasContent.getText().toString();
    }

    private void initInfoTv(){
        final TextView info_tv = findViewById(R.id.info_tv);
        if (info_tv != null && mPerName != null){
            info_tv.setText(mPerName);
        }
    }
    private void initCasIdEt(){
        final EditText et = findViewById(R.id.cas_content);
        et.setTransformationMethod(new PasswordEditTextReplacement());
        mCasContent = et;
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
            if (mFinishListener != null)mFinishListener.onFinish(VerifyPermissionDialog.this);
        });
        final Button btn = view.getOkBtn();
        if (btn != null){
            btn.setText(mContext.getString(R.string.OK));
            mOkBtn = btn;
        }
    }

    @Override
    protected double getWidthRatio(){
        //返回值： //小于0 是系统WRAP_CONTENT、MATCH_PARENT 在0到1直接为屏幕比例 大于1为具体大小
        return mContext.lessThan7Inches()? 0.95 : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public interface OnFinishListener{
        void onFinish(VerifyPermissionDialog dialog);
    }
    public void setFinishListener(OnFinishListener listener){
        mFinishListener = listener;
    }
}
