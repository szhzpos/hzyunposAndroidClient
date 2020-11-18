package com.wyc.cloudapp.dialog;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.CustomizationView.KeyboardView;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;

import static android.content.Context.WINDOW_SERVICE;

public final class VerifyPermissionDialog extends AbstractDialogMainActivity {
    private EditText mCasContent;
    private String mPerName;
    private Button mOkBtn;
    private OnFinishListener mFinishListener;
    private boolean lessThan7Inches = false;
    public VerifyPermissionDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.per_dialog_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCasIdEt();
        initInfoTv();
        initKeyboardView();

        if (lessThan7Inches){
            initWindowSize();
        }

    }

    @Override
    protected int getContentLayoutId() {
        if (lessThan7Inches = mContext.lessThan7Inches(null))return R.layout.mobile_verify_permissions_dilaog_layout;
        return R.layout.verify_permissions_dilaog_layout;
    }

    @Override
    public void closeWindow(){
        Logger.i("操作员:%s,取消授权.",mContext.getCashierInfo().getString("cas_code"));
        MyDialog.ToastMessage("您取消了授权！",mContext,getWindow());
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
        view.setCurrentFocusListenner(() -> {
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

    private void initWindowSize(){//初始化窗口尺寸
        final WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            final Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                final WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = (int)point.x;
                dialogWindow.setAttributes(lp);
            }
        }
    }

    public interface OnFinishListener{
        void onFinish(VerifyPermissionDialog dialog);
    }
    public void setFinishListener(OnFinishListener listener){
        mFinishListener = listener;
    }
}
