package com.wyc.cloudapp.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.snackbar.Snackbar;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.callback.WindowCallback;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public final class MyDialog extends AbstractDialogContext {
    @SuppressLint("StaticFieldLeak")
    private static MyDialog mDup;
    private Button mYes,mNo;//mYes确定按钮、mNo取消按钮
    private TextView mMessage;//mTitle标题文本、mMessage消息提示文本
    private String mMessageStr = "";//从外界设置的消息文本
    private final Context mContext;
    private IconType mContentIconType = IconType.INFO;
    private boolean mIsYes,mIsNo;
    //按钮文本的显示内容
    private String mYesStr,mNoStr;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private LifecycleEventObserver mLifecycleEventObserver;
    public MyDialog  setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (str != null) {
            mNoStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
        this.mIsNo = true;

        return this;
    }

    public MyDialog setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
        if (str != null) {
            mYesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
        this.mIsYes = true;

        return  this;
    }

    public MyDialog(Context context,final String title) {
        super(context,title,R.style.MyDialog);
        this.mContext = context;
    }

    public MyDialog(Context context,final String title, IconType type){
        this(context,title);
        mContentIconType = type;
        if (context instanceof LifecycleOwner && Looper.myLooper() == Looper.getMainLooper()){
            mLifecycleEventObserver = new LifecycleEventObserver() {
                @Override
                protected void finalize(){
                    Logger.d("MyDialog's LifecycleObserver finalized");
                }
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (Lifecycle.Event.ON_DESTROY == event){
                        dismiss();
                        source.getLifecycle().removeObserver(this);
                    }
                }
            };
            ((LifecycleOwner)context).getLifecycle().addObserver(mLifecycleEventObserver);
        }
    }

    @Override
    protected void closeWindow() {
        super.closeWindow();
        if (noOnclickListener != null)noOnclickListener.onNoClick(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mLifecycleEventObserver != null){
            if (Looper.myLooper() != Looper.getMainLooper()){
                getWindow().getDecorView().post(()-> ((LifecycleOwner)mContext).getLifecycle().removeObserver(mLifecycleEventObserver));
            }else
                ((LifecycleOwner)mContext).getLifecycle().removeObserver(mLifecycleEventObserver);
        }
        mDup = null;
    }

    public enum IconType {
        INFO,WARN,ERROR,ASK;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();
        //初始化界面数据
        initData();

    }

    protected double getWidthRatio(){
        if (Utils.lessThan7Inches(mContext)){
            return  0.95;
        }
        return CustomApplication.getDimension(R.dimen.size_368);
    }

    @Override
    public Context getPrivateContext() {
        return mContext;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mydialog_layout;
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        mYes.setOnClickListener(v -> {
            if (yesOnclickListener != null) {
                yesOnclickListener.onYesClick(MyDialog.this);
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        mNo.setOnClickListener(v -> {
            if (noOnclickListener != null) {
                noOnclickListener.onNoClick(MyDialog.this);
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果设置按钮的文字
        if (mYesStr != null) {
            mYes.setText(mYesStr);
        }
        if (mNoStr != null) {
            mNo.setText(mNoStr);
        }

    }
    /**
     * 初始化界面控件
     */
    private void initView() {
        mYes = findViewById(R.id.yes);
        mNo = findViewById(R.id.no);
        mMessage = findViewById(R.id.content);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    public MyDialog setMessage(String message) {
        mMessageStr = message;
        return  this;
    }


    @Override
    public void show(){
        super.show();

        showBtn();
        showIcon();
        initEvent();
    }

    private void showIcon(){
        Drawable drawable = null;
        switch (mContentIconType){
            case WARN:
                drawable = mContext.getResources().getDrawable(R.drawable.warn,null);
                break;
            case ERROR:
                drawable = mContext.getResources().getDrawable(R.drawable.error,null);
                break;
            case ASK:
                drawable = mContext.getResources().getDrawable(R.drawable.ask,null);
                break;
            default:
                drawable = mContext.getResources().getDrawable(R.drawable.infor,null);
                break;
        }
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        mMessage.setCompoundDrawables(drawable,null,null,null);
        if (mMessageStr != null) {
            mMessage.setText(mMessageStr);
        }
    }
    private void showBtn(){
        if (mIsYes && !mIsNo) {
            noOnclickListener = null;
            mNo.setVisibility(View.GONE);
            mYes.setText(mYesStr);
            mYes.setVisibility(View.VISIBLE);
        } else if (mIsNo && !mIsYes) {
            yesOnclickListener = null;
            mYes.setVisibility(View.GONE);
            mNo.setText(mNoStr);
            mNo.setVisibility(View.VISIBLE);
        } else {
            mNo.setVisibility(View.VISIBLE);
            mYes.setVisibility(View.VISIBLE);
        }
    }

    public static void displayMessage(final Context context,final String message){
        final MyDialog dialog = new MyDialog(context,"提示信息",IconType.INFO);
        dialog.setMessage(message).setNoOnclickListener("确定", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(final Context context,final String message){
        if (mDup == null) mDup = new MyDialog(context,"提示信息", IconType.ERROR);
        mDup.setMessage(mDup.mMessageStr + "\n" +message).setNoOnclickListener("取消", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(final Context context,final String message,final onNoOnclickListener no){
        final MyDialog dialog = new MyDialog(context,"提示信息", IconType.ERROR);
        dialog.setMessage(message).setNoOnclickListener("取消",no).show();
    }

    public static void displayAskMessage(final Context context,final String message,final onYesOnclickListener yes,final onNoOnclickListener no){
        final MyDialog dialog = new MyDialog(context, "提示信息",IconType.ASK);
        dialog.setMessage(message).setYesOnclickListener("是",yes).setNoOnclickListener("否", no).show();
    }

    public static int showMessageToModalDialog(final Context context,final String message){
        if (Looper.myLooper() == null)Looper.prepare();
        final MyDialog dialog = new MyDialog(context,"提示信息",IconType.ASK);
        dialog.setMessage(message).setYesOnclickListener("是", myDialog -> myDialog.setCodeAndExit(1)).setNoOnclickListener("否", myDialog -> myDialog.setCodeAndExit(0)).show();
        return dialog.exec();
    }

    public static void showErrorMessageToModalDialog(final Context context, final String message){
        final MyDialog dialog = new MyDialog(context,"错误信息",IconType.ERROR);
        dialog.setMessage(message).setNoOnclickListener("取消", myDialog -> {
            myDialog.setCodeAndExit(0);
        }).show();
        dialog.exec();
    }

    public static boolean ToastMessage(View anchor, final String message, final Window window, boolean b){
        if(!b)ToastMessage(anchor,message, window);//条件为假是提示信息
        return b;
    }

    public static void toastMessage(final String message){
        CustomApplication.showGlobalToast(message);
    }

    public static void toastMessage(final @StringRes int id){
        CustomApplication.showGlobalToast(CustomApplication.self().getString(id));
    }

    public static void ToastMessage(final String message, final Window window){
        @SuppressLint("InflateParams")
        View bg = LayoutInflater.from(CustomApplication.self()).inflate(R.layout.toast_bg,null);
        if (bg != null){
            TextView mess = bg.findViewById(R.id.message);
            if (mess != null){
                final Toast toast = new Toast(CustomApplication.self());
                if (null != window){
                    window.setCallback(new WindowCallback(window,toast));
                }
                toast.setView(bg);
                mess.setTextColor(Color.WHITE);
                mess.setText(message);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
                mess.setOnClickListener(v -> toast.cancel());
            }
        }
    }

    public static void ToastMessage(View anchor, final String message, final Window window){
        @SuppressLint("InflateParams")
        final View bg = LayoutInflater.from(CustomApplication.self()).inflate(R.layout.toast_bg,null);
        if (bg != null){
            final TextView mess = bg.findViewById(R.id.message);
            if (mess != null){
                final Toast toast = new Toast(CustomApplication.self());
                if (null != window){
                    window.setCallback(new WindowCallback(window,toast));
                }
                toast.setView(bg);
                mess.setTextColor(Color.WHITE);
                mess.setText(message);
                if (null == anchor){
                    toast.setGravity(Gravity.CENTER,0,0);
                }else{
                    int[] location = new int[2];
                    anchor.getLocationOnScreen(location);
                    toast.setGravity( Gravity.TOP|Gravity.START,location[0] - Utils.dpToPx(CustomApplication.self(),72),location[1] + anchor.getMeasuredHeight() / 4);
                }
                toast.show();
            }
        }
    }

    public static void SnackBarMessage(final Window window, final String message, View anchor){
        if (null != window){
            final Snackbar snackbar = Snackbar.make(window.getDecorView(),message, Snackbar.LENGTH_LONG);
            window.setCallback(new WindowCallback(window,snackbar));
            if (anchor != null)snackbar.setAnchorView(anchor);
            final View snackbar_view = snackbar.getView();
            snackbar_view.setBackgroundResource(R.drawable.snackbar_background);
            final TextView tvSnackbarText = snackbar_view.findViewById(R.id.snackbar_text);
            tvSnackbarText.setTextSize(20);
            snackbar.show();
        }
    }

    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
          void onYesClick(MyDialog myDialog);
    }
    public interface onNoOnclickListener {
          void onNoClick(MyDialog myDialog);
    }
}