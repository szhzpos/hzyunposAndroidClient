package com.wyc.cloudapp.activity.mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.CustomizationView.DrawableCenterTextView;
import com.wyc.cloudapp.mobileFragemt.BackgroundFragment;
import com.wyc.cloudapp.mobileFragemt.CashierDeskFragment;
import com.wyc.cloudapp.mobileFragemt.MyFragment;
import com.wyc.cloudapp.mobileFragemt.ReportFragment;

public class MobileNavigationActivity extends AbstractMobileActivity {
    private FragmentManager mFragmentManager;
    private DrawableCenterTextView mCurrentNavView;
    private Fragment mCurrentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentManager = getSupportFragmentManager();

        initFunctionBtn();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_navigation;
    }

    private void initFunctionBtn(){
        final LinearLayout fun_nav = findViewById(R.id.fun_nav);
        for(int i = 0,count = fun_nav.getChildCount();i < count; i++){
            final DrawableCenterTextView drawableCenterTextView = (DrawableCenterTextView) fun_nav.getChildAt(i);
            drawableCenterTextView.setOnClickListener(mNavClick);
            if (drawableCenterTextView.getId() == R.id._mobile_cas_desk_tv){
                drawableCenterTextView.callOnClick();
            }
        }
    }

    private final View.OnClickListener mNavClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final DrawableCenterTextView textView = (DrawableCenterTextView)v;
            if (mCurrentNavView != null){
                if (mCurrentNavView != textView){
                    mCurrentNavView.setTextColor(getColor(R.color.mobile_fun_view_no_click));
                    mCurrentNavView.triggerAnimation(false);

                    textView.setTextColor(getColor(R.color.mobile_fun_view_click));
                    textView.triggerAnimation(true);
                    mCurrentNavView = textView;
                }else return;
            }else{
                textView.setTextColor(getColor(R.color.mobile_fun_view_click));
                textView.triggerAnimation(true);
                mCurrentNavView = textView;
            }

            final FragmentTransaction ft = mFragmentManager.beginTransaction();

            Fragment current ;
            final int id = v.getId();
            if (id == R.id._mobile_board_tv) {//first
                current = new BackgroundFragment();
            }else if(id == R.id._mobile_bg_tv){//second
                current = new BackgroundFragment();
            }else if (id == R.id._mobile_report_tv){//fourth
                current = new ReportFragment();
            }else  if(id == R.id._mobile_my_tv){//fifth
                current = new MyFragment();
            }else{//third
                current = new CashierDeskFragment();
            }

            ft.add(R.id.mobile_fragment_container,current);

            if (mCurrentFragment != null)ft.remove(mCurrentFragment);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.show(current);

            mCurrentFragment = current;

            ft.commit();
        }
    };

}