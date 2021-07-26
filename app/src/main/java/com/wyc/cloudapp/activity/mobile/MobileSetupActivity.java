package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.fragment.AbstractBaseFragment;
import com.wyc.cloudapp.mobileFragemt.MobilePrintFormatFragment;

public class MobileSetupActivity extends AbstractMobileActivity {
    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();

        intiFragment();
    }

    private void intiFragment(){
        final Intent intent = getIntent();
        final String name = intent.getStringExtra("frag");
        setMiddleText(intent.getStringExtra("title"));
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

    private void showFragment(final String name){
        final FragmentTransaction ft = mFragmentManager.beginTransaction();
        AbstractBaseFragment current = null;
        switch (name){
            case "PrintFormatFragment":
                current = new MobilePrintFormatFragment();
                break;
            default:
                break;
        }
        if (current != null){
            ft.add(R.id.mobile_setup_fragment_container,current);
            if (mCurrentFragment != null)ft.remove(mCurrentFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.show(current);
            mCurrentFragment = current;
            ft.commit();
        }else
            MyDialog.ToastMessage("暂不支持此功能!", getWindow());
    }
}