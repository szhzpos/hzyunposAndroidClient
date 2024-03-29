package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.client.android.CaptureActivity;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.customizationView.TopDrawableTextView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.cashierDesk.MobileCashierActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractTransferDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileTransferDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.mobileFragemt.BoardFragment;
import com.wyc.cloudapp.mobileFragemt.MobileBusinessFragment;
import com.wyc.cloudapp.mobileFragemt.MobileCashierDeskFragment;
import com.wyc.cloudapp.mobileFragemt.MyFragment;
import com.wyc.cloudapp.mobileFragemt.ReportFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.wyc.cloudapp.constants.ScanCallbackCode.PAY_REQUEST_CODE;

public final class MobileNavigationActivity extends AbstractDefinedTitleActivity implements CustomApplication.MessageCallback {
    private FragmentManager mFragmentManager;
    private CustomProgressDialog mProgressDialog;
    private WeakReference<ScanCallback> mScanCallback;
    private Fragment mCurrentFragment;
    private TopDrawableTextView mCurrentNavView;
    private View mCashierDesk;
    private float x1,y1;
    private final List<Integer> mBtnId = new ArrayList<>();
    private boolean isMoved = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mFragmentManager = getSupportFragmentManager();

        initFunctionBtn();
        initSyncManagement();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_navigation;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                isMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:{
                //有可能没触发ACTION_DOWN，这里更新成第一个ACTION_MOVE事件的值
                float m_x = event.getX(),m_y = event.getY();
                if (x1 == -1 || y1 == -1){
                    x1 = m_x;
                    y1 = m_y;
                    isMoved = false;
                }else {
                    float slop = ViewConfiguration.get(this).getScaledPagingTouchSlop();
                    Logger.d("x1:%f,m_x:%f,slop:%f,x1 - m_x:%f",x1,m_x,slop,x1 - m_x);
                    isMoved = Math.abs(x1 - m_x) > slop * 2  || Math.abs(y1 - m_y) > slop * 2;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
                float x1_tmp = x1;
                float y1_tmp = y1;

                x1 = -1;
                y1 = -1;

                if (!isMoved){
                    isMoved = false;

                    int current_id = mCurrentNavView == null ? -1 : mCurrentNavView.getId(),
                            current_index = mBtnId.indexOf(current_id);

                    float x2 = event.getX();
                    float y2 = event.getY();

                    Logger.d("x2:%f,y2:%f",x2,y2);

                    final int offsetTouchSlop = 168;

                    if(x1_tmp-x2 > Math.abs(y2-y1_tmp + offsetTouchSlop)) {//左划
                        if ((current_index += 1) > mBtnId.size() - 1){
                            current_index = 0;
                        }
                        final View view = findViewById(mBtnId.get(current_index));
                        if (null != view){
                            view.callOnClick();
                            return true;
                        }

                    } else if(x2-x1_tmp > Math.abs(y2-y1_tmp) + offsetTouchSlop) {//右划
                        if ((current_index -= 1) < 0){
                            current_index = mBtnId.size() - 1;
                        }
                        final View view = findViewById(mBtnId.get(current_index));
                        if (view != null){
                            view.callOnClick();
                            return true;
                        }
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected boolean hasSlide(){
        return false;
    }

    @Override
    public void onBackPressed(){
        if (mCurrentFragment instanceof MobileCashierDeskFragment){
            CustomApplication.runInMainThread(()->{
                if (MyDialog.showMessageToModalDialog(this,"是否退出?") == 1){
                    super.onBackPressed();
                    finish();
                }
            });
        }else {
            if (null != mCashierDesk)mCashierDesk.callOnClick();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){//条码回调
        if (resultCode == RESULT_OK ){
            final String _code = intent.getStringExtra(CaptureActivity.CALLBACK_CODE);
             if (requestCode == PAY_REQUEST_CODE){
                if (mScanCallback != null){
                    final ScanCallback callback = mScanCallback.get();
                    if (callback != null)callback.callback(_code);
                }
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    @Override
    public void setScanCallback(final ScanCallback callback){
        if (mScanCallback == null || callback != mScanCallback.get()){
            mScanCallback = new WeakReference<>(callback);
        }
    }

    @Override
    public void disposeHangBill(){
        final Intent intent = new Intent(this, MobileCashierActivity.class);
        intent.putExtra("disposeHang",true);
        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        mApplication.registerHandleMessage(null);
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
    }

    private void initFunctionBtn(){
        final LinearLayout fun_nav = findViewById(R.id.fun_nav);
        for(int i = 0,count = fun_nav.getChildCount();i < count; i++){
            final TopDrawableTextView topDrawableTextView = (TopDrawableTextView) fun_nav.getChildAt(i);
            topDrawableTextView.setOnClickListener(mNavClick);
            mBtnId.add(topDrawableTextView.getId());

            if (topDrawableTextView.getId() == R.id._mobile_cas_desk_tv){
                topDrawableTextView.postDelayed(topDrawableTextView::callOnClick,300);
                mCashierDesk = topDrawableTextView;
            }
        }
    }

    private final View.OnClickListener mNavClick = new View.OnClickListener() {
        private boolean setCurrentView(final View v){
            final TopDrawableTextView textView = (TopDrawableTextView)v;
            if (v.getId() == R.id._mobile_archive_tv){
                setMiddleText(getStoreName());
            }else
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
            if (id == R.id._mobile_archive_tv) {//first
                current = new BoardFragment();
            }else if(id == R.id._mobile_business_tv){//second
                current = new MobileBusinessFragment();
            }else if (id == R.id._mobile_report_tv){//fourth
                current = new ReportFragment();
            }else  if(id == R.id._mobile_my_tv){//fifth
                current = new MyFragment();
            }else{//third
                current = new MobileCashierDeskFragment();
            }
            if (mCurrentFragment != null)ft.remove(mCurrentFragment);
            ft.add(R.id.mobile_fragment_container,current);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.show(current);
            mCurrentFragment = current;
            ft.commit();
        }
        @Override
        public void onClick(View v) {
            if (setCurrentView(v))showFragment(v);
        }
    };

    private void showProgress(final String mess,boolean isCancel){
        if (mProgressDialog == null)mProgressDialog = new CustomProgressDialog(this);
        mProgressDialog.setMessage(mess).refreshMessage();
        mProgressDialog.setCancel(isCancel);
        mProgressDialog.show();
    }
    private void dismissProgress(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void handleMessage(Handler handler, Message msg) {
        switch (msg.what){
            case MessageID.DIS_ERR_INFO_ID:
            case MessageID.SYNC_ERR_ID://资料同步错误
                dismissProgress();
                if (msg.obj != null)
                    MyDialog.displayErrorMessage(this, msg.obj.toString());
                break;
            case MessageID.SYNC_FINISH_ID:
                dismissProgress();
                break;
            case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                if (msg.obj != null)
                    showProgress(msg.obj.toString(),false);
                break;
        }
    }
}