package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.wyc.cloudapp.CustomizationView.TopDrawableTextView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractTransferDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileTransferDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.mobileFragemt.BackgroundFragment;
import com.wyc.cloudapp.mobileFragemt.BoardFragment;
import com.wyc.cloudapp.mobileFragemt.CashierDeskFragment;
import com.wyc.cloudapp.mobileFragemt.MyFragment;
import com.wyc.cloudapp.mobileFragemt.ReportFragment;
import com.wyc.cloudapp.utils.MessageID;

import static com.wyc.cloudapp.utils.MessageID.PAY_REQUEST_CODE;

public final class MobileNavigationActivity extends AbstractMobileActivity implements CustomApplication.MessageCallback {
    private FragmentManager mFragmentManager;
    private CustomProgressDialog mProgressDialog;
    private ScanCallback mScanCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
        mProgressDialog = new CustomProgressDialog(this);

        initFunctionBtn();
        initSyncManagement();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_navigation;
    }

    @Override
    public void finalize(){
        Logger.d("MobileNavigationActivity finalized");
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){//条码回调
        if (resultCode == RESULT_OK ){
            final String _code = intent.getStringExtra("auth_code");
             if (requestCode == PAY_REQUEST_CODE){
                if (mScanCallback != null)mScanCallback.callback(_code);
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    @Override
    public void disposeHangBill(){
        final Intent intent = new Intent(this, MobileCashierActivity.class);
        intent.putExtra("disposeHang",true);
        startActivity(intent);
    }

    @Override
    public void setScanCallback(final ScanCallback callback){
        mScanCallback = callback;
    }

    public void transfer(){
        if (AbstractTransferDialog.verifyTransferPermissions(this)){
            final AbstractTransferDialog transferDialog = new MobileTransferDialog(this);
            transferDialog.verifyTransfer();
        }
    }

    private void initSyncManagement(){
        mApplication.registerHandleMessage(this);
        mApplication.sync_order_info();
        mApplication.start_sync(false);
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

    @Override
    public void handleMessage(Handler handler, Message msg) {
        switch (msg.what){
            case MessageID.DIS_ERR_INFO_ID:
            case MessageID.SYNC_ERR_ID://资料同步错误
                if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
                if (msg.obj instanceof String)
                    MyDialog.displayErrorMessage(null,msg.obj.toString(),this);
                break;
            case MessageID.SYNC_FINISH_ID:
                if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
                CustomApplication.self().start_sync(false);
                break;
            case MessageID.TRANSFERSTATUS_ID://传输状态
            case MessageID.NETWORKSTATUS_ID://网络状态
                if (msg.obj instanceof Boolean){

                }
                break;
            case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                if (mProgressDialog != null){
                    mProgressDialog.setMessage(msg.obj.toString()).refreshMessage();
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.setCancel(false).show();
                    }
                }
                break;
            case MessageID.START_SYNC_ORDER_INFO_ID:
                Toast.makeText(this,"开始上传数据",Toast.LENGTH_SHORT).show();
                break;
            case MessageID.FINISH_SYNC_ORDER_INFO_ID:
                Toast.makeText(this,"数据上传完成",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}