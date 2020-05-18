package com.wyc.cloudapp.dialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.DialogFragment;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.FragmentPagerAdapter;

public class ParameterSettingDialog extends DialogFragment {

    private Context mContext;

    public ParameterSettingDialog(@NonNull Context context) {
        mContext = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.parameter_setting_dialog_layout, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final TabLayout tabLayout = view.findViewById(R.id.param_tab);
        final ViewPager2 viewPager2 = view.findViewById(R.id.view_pager);
        final FragmentPagerAdapter adapter =  new FragmentPagerAdapter(this);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2,(tab, position) -> tab.setText(adapter.getItem(position).getTitle())).attach();

        view.findViewById(R.id._close).setOnClickListener(v -> this.dismiss());
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Dialog dialog = getDialog();
        if (null != dialog){
            final Window window = dialog.getWindow();
            if (null != window){
                final WindowManager.LayoutParams params = window.getAttributes();
                params.width = (int)mContext.getResources().getDimension(R.dimen.param_setting_dialog_width);
                params.height =(int)mContext.getResources().getDimension(R.dimen.param_setting_dialog_height);
                window.setAttributes(params);
            }
        }
    }

}
