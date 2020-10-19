package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.callback.WindowCallback;
import com.wyc.cloudapp.utils.Utils;

public final class MyDialog extends Dialog {
    private Button mYes,mNo;//mYes确定按钮、mNo取消按钮
    private TextView mTitle,mMessage;//mTitle标题文本、mMessage消息提示文本
    private String mTitleStr;//从外界设置的title文本
    private String mMessageStr;//从外界设置的消息文本
    private final Context mContext;
    private IconType mContentIconType = IconType.INFO;
    private boolean mIsYes,mIsNo;
    //按钮文本的显示内容
    private String mYesStr,mNoStr;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器


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

    public MyDialog(Context context) {
        super(context,R.style.MyDialog);
        this.mContext = context;
    }

    public MyDialog(Context context, IconType type){
        this(context);
        mContentIconType = type;
    }

    public enum IconType {
        INFO,WARN,ERROR,ASK;
    }

    @Override
    public void  onBackPressed(){
        if (noOnclickListener != null)noOnclickListener.onNoClick(this);
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mydialog_layout);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();

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
        //如果用户自定了title和message
        if (mTitleStr != null) {
            mTitle.setText(mTitleStr);
        }
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
        findViewById(R.id._close).setOnClickListener(v -> {
            if (noOnclickListener != null){
                noOnclickListener.onNoClick(MyDialog.this);
            }else
                this.dismiss();
        });
        mYes = findViewById(R.id.yes);
        mNo = findViewById(R.id.no);
        mTitle = findViewById(R.id.title_text);
        mMessage = findViewById(R.id.content);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    public MyDialog setTitle(String title) {
        mTitleStr = title;
        return  this;
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
    }
    @Override
    public void dismiss(){
        super.dismiss();
        if (mMessageStr != null && mMessageStr.length() != 0){
            mMessageStr = null;
        }
        if (mTitleStr != null && mTitleStr.length() != 0){
            mTitleStr = null;
        }
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

    private void setYes(boolean b){
        mIsYes = b;
    }
    private void setNo(boolean b){
        mIsNo = b;
    }

    private void setContentIconType(IconType type){
        mContentIconType = type;
    }

    public static void displayMessage(MyDialog dialog,String message, Context context){
        if (dialog == null)
            dialog = new	MyDialog(context, IconType.INFO);
        else{
            dialog.setContentIconType(IconType.INFO);
            dialog.setNo(false);
        }
        dialog.setTitle("提示信息").setMessage(message).setNoOnclickListener("确定", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(MyDialog dialog,String message, Context context){
        if (dialog == null)
            dialog = new MyDialog(context, IconType.ERROR);
        else{
            dialog.setContentIconType(IconType.ERROR);
            dialog.setYes(false);
        }
        dialog.setTitle("提示信息").setMessage(message).setNoOnclickListener("取消", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(MyDialog dialog,String message, Context context,onNoOnclickListener no){
        if (dialog == null)
            dialog  = new	MyDialog(context, IconType.ERROR);
        else{
            dialog.setContentIconType(IconType.ERROR);
            dialog.setYes(false);
        }
        dialog.setTitle("提示信息").setMessage(message).setNoOnclickListener("取消",no).show();
    }

    public static void displayAskMessage(MyDialog dialog,String message, Context context,MyDialog.onYesOnclickListener yes,MyDialog.onNoOnclickListener no){
        if (dialog == null)
            dialog = new MyDialog(context, IconType.ASK);
        else{
            dialog.setContentIconType(IconType.ASK);
            dialog.setYes(true);
            dialog.setNo(true);
        }
        dialog.setTitle("提示信息").setMessage(message).setYesOnclickListener("是",yes).setNoOnclickListener("否", no).show();
    }

    public static int showMessageToModalDialog(final Context context,final String message){
        final JEventLoop loop = new JEventLoop();
        final MyDialog dialog = new MyDialog(context, IconType.ASK);
        dialog.setTitle("提示信息").setMessage(message).setYesOnclickListener("是", myDialog -> {
            myDialog.dismiss();
            loop.done(1);
        }).setNoOnclickListener("否", myDialog -> {
            myDialog.dismiss();
            loop.done(0);
        }).show();
        return loop.exec();
    }

    public static boolean ToastMessage(View anchor,final String message,final Context context,final Window window,boolean b){
        if(!b)ToastMessage(anchor,message,context,window);//条件为假是提示信息
        return b;
    }

    public static void ToastMessage(final String message, @NonNull final Context context, final Window window){
        final Toast toast = new Toast(context);
        if (null != window){
            window.setCallback(new WindowCallback(window,toast));
        }else if (context instanceof Activity){
            Window w = ((Activity)context).getWindow();
            w.setCallback(new WindowCallback(w,toast));
        }
        View bg = LayoutInflater.from(context).inflate(R.layout.toast_bg,null);
        if (bg != null){
            TextView mess = bg.findViewById(R.id.message);
            if (mess != null){
                toast.setView(bg);
                mess.setTextColor(Color.WHITE);
                mess.setText(message);
                toast.setGravity(Gravity.CENTER,0,0);
                //toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
    public static void ToastMessage(View anchor,final String message, @NonNull final Context context, final Window window){
        final Toast toast = new Toast(context);
        if (null != window){
            window.setCallback(new WindowCallback(window,toast));
        }else if (context instanceof Activity){
            Window w = ((Activity)context).getWindow();
            w.setCallback(new WindowCallback(w,toast));
        }
        final View bg = LayoutInflater.from(context).inflate(R.layout.toast_bg,null);
        if (bg != null){
            final TextView mess = bg.findViewById(R.id.message);
            if (mess != null){
                toast.setView(bg);
                mess.setTextColor(Color.WHITE);
                mess.setText(message);
                if (null == anchor){
                    toast.setGravity(Gravity.CENTER,0,0);
                }else{
                    int[] location = new int[2];
                    anchor.getLocationOnScreen(location);
                    toast.setGravity( Gravity.TOP|Gravity.START,location[0] - Utils.dpToPx(context,72),location[1] + anchor.getMeasuredHeight() / 4);
                }
                toast.show();
            }
        }
    }

    public static boolean SnackbarMessage(final Window window, final String message, View anchor, boolean b){
        if(!b) SnackbarMessage( window, message, anchor);
        return b;
    }
    public static void SnackbarMessage(final Window window, final String message, View anchor){
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