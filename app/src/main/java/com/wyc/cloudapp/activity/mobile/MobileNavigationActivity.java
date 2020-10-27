package com.wyc.cloudapp.activity.mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.CustomizationView.TopDrawableTextView;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.mobileFragemt.BackgroundFragment;
import com.wyc.cloudapp.mobileFragemt.BoardFragment;
import com.wyc.cloudapp.mobileFragemt.CashierDeskFragment;
import com.wyc.cloudapp.mobileFragemt.MyFragment;
import com.wyc.cloudapp.mobileFragemt.ReportFragment;

public final class MobileNavigationActivity extends AbstractMobileActivity {
    private FragmentManager mFragmentManager;
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
            final TopDrawableTextView topDrawableTextView = (TopDrawableTextView) fun_nav.getChildAt(i);
            topDrawableTextView.setOnClickListener(mNavClick);
            if (topDrawableTextView.getId() == R.id._mobile_cas_desk_tv){
                topDrawableTextView.postDelayed(topDrawableTextView::callOnClick,300);
            }
        }
    }

    private final View.OnClickListener mNavClick = new View.OnClickListener() {
        private TopDrawableTextView mCurrentNavView;
        private Fragment mCurrentFragment;
        private boolean setCurrentView(final View v){
            final TopDrawableTextView textView = (TopDrawableTextView)v;
            setMiddleText(textView.getText().toString());
            if (mCurrentNavView != null){
                if (mCurrentNavView != textView){
                    mCurrentNavView.setTextColor(getColor(R.color.mobile_fun_view_no_click));
                    mCurrentNavView.triggerAnimation(false);

                    textView.setTextColor(getColor(R.color.mobile_fun_view_click));
                    textView.triggerAnimation(true);
                    mCurrentNavView = textView;
                }else return false;
            }else{
                textView.setTextColor(getColor(R.color.mobile_fun_view_click));
                textView.triggerAnimation(true);
                mCurrentNavView = textView;
            }
            return true;
        }
        private void showFragment(final View v){
            final FragmentTransaction ft = mFragmentManager.beginTransaction();

            Fragment current = null ;
            final int id = v.getId();
            if (id == R.id._mobile_board_tv) {//first
                current = new BoardFragment();
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
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            boolean support = false;
            if (id == R.id._mobile_board_tv) {//first

            }else if(id == R.id._mobile_bg_tv){//second

            }else if (id == R.id._mobile_report_tv){//fourth

            }else  if(id == R.id._mobile_my_tv){//fifth

            }else{//third
                support = true;
            }
            if(support){
                if (setCurrentView(v))showFragment(v);
            }else {
                MyDialog.ToastMessage("暂不支持此功能!",MobileNavigationActivity.this,null);
            }
        }
    };

}