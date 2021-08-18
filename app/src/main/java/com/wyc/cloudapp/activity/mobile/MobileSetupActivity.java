package com.wyc.cloudapp.activity.mobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.fragment.AbstractBaseFragment;
import com.wyc.cloudapp.mobileFragemt.MobileBusinessPrintFragment;
import com.wyc.cloudapp.mobileFragemt.MobilePrintFormatFragment;

public class MobileSetupActivity extends AbstractMobileActivity {
    private FragmentManager mFragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();

        intiFragment();
    }

    private void intiFragment(){
        final Intent intent = getIntent();
        final String name = intent.getStringExtra("w");
        if (null != name)showFragment(name);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_setup;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    public void setRightTitle(final String title,final View.OnClickListener listener ){
        setRightText(title);
        setRightListener(listener);
    }

    private void showFragment(final String name){
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        AbstractBaseFragment current = null;
        switch (name){
            case "RPS"://零售打印设置
                current = new MobilePrintFormatFragment();
                break;
            case "BPS":
                current = new MobileBusinessPrintFragment();
                break;
            default:
                break;
        }
        if (current != null){
            ft.replace(R.id.mobile_setup_fragment_container,current);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.show(current);
            ft.commit();
        }else
            MyDialog.ToastMessage("暂不支持此功能!", getWindow());
    }

    private static void start(final String which,final String title){
        final Context context = CustomApplication.self();
        context.startActivity(new Intent(context, MobileSetupActivity.class).putExtra("w",which).putExtra(AbstractMobileActivity.TITLE_KEY,title));
    }

    public static void startRetailPrintSetting(final String title){
        start("RPS",title);
    }
    public static void startBusinessPrintSetting(final String title){
        start("BPS",title);
    }
}