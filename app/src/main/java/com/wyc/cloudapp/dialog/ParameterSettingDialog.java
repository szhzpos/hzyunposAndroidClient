package com.wyc.cloudapp.dialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.FragmentPagerAdapter;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.fragment.AbstractParameterFragment;
import com.wyc.cloudapp.fragment.BaseParameterFragment;
import com.wyc.cloudapp.fragment.PeripheralSettingFragment;
import com.wyc.cloudapp.fragment.PrintFormatFragment;

import java.util.ArrayList;
import java.util.List;

public class ParameterSettingDialog extends AbstractDialogMainActivity {
    private ViewPager2 mViewPager2;
    ParameterSettingDialog(@NonNull MainActivity context) {
        super(context,context.getString(R.string.setup_sz));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        final List<AbstractParameterFragment> fragments = new ArrayList<>();
        fragments.add(new BaseParameterFragment());
        fragments.add(new PeripheralSettingFragment());
        fragments.add(new PrintFormatFragment());

        final TabLayout tabLayout = findViewById(R.id.param_tab);
        mViewPager2 = findViewById(R.id.view_pager);
        final FragmentPagerAdapter<AbstractParameterFragment> adapter =  new FragmentPagerAdapter<>(fragments,mContext);
        mViewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, mViewPager2,(tab, position) -> tab.setText(adapter.getItem(position).getTitle())).attach();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mViewPager2 != null)mViewPager2.setAdapter(null);//不重新设置null导致此dialog的生命周期与mContext一样长
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.parameter_setting_dialog_layout;
    }
    @Override
    protected void initWindowSize(){
        final Window window = getWindow();
        if (null != window){
            final WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int)mContext.getResources().getDimension(R.dimen.param_setting_dialog_width);
            params.height =(int)mContext.getResources().getDimension(R.dimen.param_setting_dialog_height);
            window.setAttributes(params);
        }
    }
}
