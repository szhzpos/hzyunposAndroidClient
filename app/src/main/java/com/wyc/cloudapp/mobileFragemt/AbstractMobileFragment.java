package com.wyc.cloudapp.mobileFragemt;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.MobileNavigationActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.MobileVipChargeDialog;

/*
 * 负责加载功能布局，每个布局的叶节点为TextView或其子类。
 * 叶节点的点击事件可以直接启动指定的Activity或完成某项功能。
 * Activity 的类名保存在叶节点的tag属性中。
 * */
public abstract class AbstractMobileFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(getRootLayoutId(), container, false);
        ViewGroup main_linearLayout;
        if (root instanceof ScrollView){
            main_linearLayout = root.findViewById(getMainLayoutId());
        }else
            main_linearLayout = (ViewGroup) root;

        if (main_linearLayout != null){
            int _count = main_linearLayout.getChildCount(),child_count;
            for (int i = 0;i < _count;i ++){
                final View child = main_linearLayout.getChildAt(i);
                if (child instanceof ViewGroup){
                    final ViewGroup viewGroup = (ViewGroup)child;
                    child_count = viewGroup.getChildCount();
                    for (int j = 0; j < child_count;j ++){
                        final View view = viewGroup.getChildAt(j);
                        view.setOnClickListener(mClickListener);
                    }
                }
            }
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    abstract protected int getRootLayoutId();
    abstract protected int getMainLayoutId();

    private final View.OnClickListener mClickListener = v -> {
        final Context context = getContext();
        if (null != context){
            final int v_id = v.getId();
            try {
                if (v_id == R.id.data_exchange_tv){
                    CustomApplication.self().manualSync();
                }else if (v_id ==R.id.fd_shift_exchange_tv){
                    final Activity activity = getActivity();
                    if (activity instanceof MobileNavigationActivity){
                        ((MobileNavigationActivity)activity).transfer();
                    }
                }else if(v_id == R.id.vip_charge_tv){
                    final MobileVipChargeDialog dialog = new MobileVipChargeDialog((MainActivity) getActivity());
                    dialog.show();
                }else{
                    final Intent intent = new Intent();
                    intent.setClassName(context,context.getPackageName().concat(".") + v.getTag());
                    if (v instanceof TextView)intent.putExtra("title",((TextView)v).getText());
                    startActivity(intent);
                }
            }catch (ActivityNotFoundException e){
                e.printStackTrace();
                MyDialog.ToastMessage("暂不支持此功能!",context,null);
            }
        }
    };
}