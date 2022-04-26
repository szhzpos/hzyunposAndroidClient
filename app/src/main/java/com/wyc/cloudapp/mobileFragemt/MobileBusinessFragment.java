package com.wyc.cloudapp.mobileFragemt;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

public final class MobileBusinessFragment extends AbstractJumpFragment {
    @Override
    protected int getRootLayout() {
        return R.layout.mobile_business_fragment_flowlayout;
    }

    @Override
    protected int getMainViewId() {
        return R.layout.mobile_business_fragment_flowlayout;
    }

    @Override
    protected void triggerItemClick(View v) {
        if (Utils.isNotEmpty(mContext.getPtUserId())){
            final Intent intent = new Intent();
            intent.setClassName(mContext,mContext.getPackageName().concat(".") + v.getTag());
            final String title = v instanceof TextView ? ((TextView)v).getText().toString() : "";
            intent.putExtra(AbstractDefinedTitleActivity.TITLE_KEY,title);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                e.printStackTrace();
                MyDialog.toastMessage("暂不支持" + title);
            }
        }else {
            MyDialog.toastMessage(getString(R.string.not_permission_hint));
        }
    }
}
