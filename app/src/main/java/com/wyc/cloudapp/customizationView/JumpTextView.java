package com.wyc.cloudapp.customizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.CustomizationView
 * @ClassName: JumpTextView
 * @Description: Fragment点击跳转叶节点并且做权限检查
 * @Author: wyc
 * @CreateDate: 2021/1/27 15:09
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/1/27 15:09
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class JumpTextView extends androidx.appcompat.widget.AppCompatTextView {
    private final String mPermissionId;
    private final boolean mHide;
    public JumpTextView(Context context) {
        this(context,null);
    }
    public JumpTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public JumpTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JumpTextView,0,0);
        mPermissionId = typedArray.getString(R.styleable.JumpTextView_perId);
        mHide = typedArray.getBoolean(R.styleable.JumpTextView_hide,false);
        verifyPermission(context);
        typedArray.recycle();
    }

    private boolean verifyPermission(final Context context){
        boolean code = true;
        if (Utils.isNotEmpty(mPermissionId)){
            if (context instanceof MainActivity){
                MainActivity activity = (MainActivity)context;
                if (!(code = activity.verifyPermissions(mPermissionId,null,false)) && mHide){
                    setVisibility(GONE);
                }
            }
        }
        return code;
    }

    public boolean isHide() {
        return mHide;
    }

    public String getPermissionId() {
        return mPermissionId;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){
            if (!verifyPermission(getContext())){
                MyDialog.ToastMessage(this,CustomApplication.self().getString(R.string.not_permission_hint), null);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean callOnClick() {
        if (verifyPermission(getContext())){
            return super.callOnClick();
        }else MyDialog.ToastMessage(CustomApplication.self().getString(R.string.not_permission_hint), null);
        return false;
    }
}
