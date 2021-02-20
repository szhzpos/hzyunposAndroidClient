package com.wyc.cloudapp.mobileFragemt;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.wyc.cloudapp.CustomizationView.TopDrawableTextView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.MyDialog;

public class ReportFragment extends AbstractJumpFragment {

    public ReportFragment(final MainActivity activity) {
        super(activity);
    }

    @Override
    protected void viewCreated(boolean created) {
        super.viewCreated(created);

    }

    @Override
    protected int getRootLayout() {
        return R.layout.mobile_report_fragment_layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected int getMainViewId() {
        return R.id.main_function_layout;
    }

    @Override
    protected void triggerItemClick(View v) {
        final Intent intent = new Intent();
        final String title = ((TextView)v).getText().toString();
        intent.putExtra("title",title);
        intent.setClassName(mContext,mContext.getPackageName().concat(".") + v.getTag());
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            MyDialog.ToastMessage("暂不支持" + title,mContext,null);
        }
    }
}