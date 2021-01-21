package com.wyc.cloudapp.mobileFragemt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.MobilePurchaseOrder;

public class MobileBusinessFragment extends AbstractMobileFragment {
    public MobileBusinessFragment(final MainActivity activity) {
        super(activity);

    }

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
        final Intent intent = new Intent();
        intent.setClassName(mContext,mContext.getPackageName().concat(".") + v.getTag());
        if (v instanceof TextView)intent.putExtra("title",((TextView)v).getText());
        startActivity(intent);
        Toast.makeText(mContext,((TextView)v).getText(),Toast.LENGTH_LONG).show();
    }
}
