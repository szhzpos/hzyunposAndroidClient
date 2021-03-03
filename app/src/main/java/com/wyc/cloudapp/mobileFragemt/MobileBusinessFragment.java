package com.wyc.cloudapp.mobileFragemt;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

public final class MobileBusinessFragment extends AbstractJumpFragment {
    @Override
    protected int getRootLayout() {
        return R.layout.mobile_business_fragment_layout;
    }

    @Override
    protected int getMainViewId() {
        return R.layout.mobile_business_fragment_layout;
    }

    @Override
    protected void triggerItemClick(View v) {
        if (Utils.isNotEmpty(mContext.getPtUserId())){
            final Intent intent = new Intent();
            intent.setClassName(mContext,mContext.getPackageName().concat(".") + v.getTag());
            final String title = v instanceof TextView ? ((TextView)v).getText().toString() : "";
            intent.putExtra("title",title);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                e.printStackTrace();
                MyDialog.ToastMessage("暂不支持" + title,mContext,null);
            }
        }else {
            MyDialog.ToastMessage("当前用户没有没权限处理此业务!",mContext,null);
        }
    }
}
