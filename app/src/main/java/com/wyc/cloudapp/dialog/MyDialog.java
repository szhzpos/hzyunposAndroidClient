package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.wyc.cloudapp.R;

import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import static android.content.Context.WINDOW_SERVICE;

public class MyDialog extends Dialog {
    private Button mYes,mNo;//mYes确定按钮、mNo取消按钮
    private TextView mTitle,mMessage;//mTitle标题文本、mMessage消息提示文本
    private String mTitleStr;//从外界设置的title文本
    private String mMessageStr;//从外界设置的消息文本
    private Context mContext;
    private IconType mContentIconType = IconType.INFO;
    private boolean mIsYes,mIsNo;
    //按钮文本的显示内容
    private String mYesStr,mNoStr;

    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器

    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param str
     * @param onNoOnclickListener
     */
    public MyDialog  setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (str != null) {
            mNoStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
        this.mIsNo = true;

        return this;
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param onYesOnclickListener
     */
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

        setContentView(this.getLayoutInflater().inflate(R.layout.mydialog_layout, null));
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
        if (mMessageStr != null) {
            mMessage.setText(mMessageStr);
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
        findViewById(R.id._close).setOnClickListener(v -> this.dismiss());
        mYes = findViewById(R.id.yes);
        mNo = findViewById(R.id.no);
        mTitle = findViewById(R.id.title_text);
        mMessage = findViewById(R.id.content);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
        Drawable drawable = null;
        switch (mContentIconType){
            case INFO:
                drawable = mContext.getResources().getDrawable(R.drawable.infor,null);
                break;
            case WARN:
                drawable = mContext.getResources().getDrawable(R.drawable.warn,null);
                break;
            case ERROR:
                drawable = mContext.getResources().getDrawable(R.drawable.error,null);
                break;
            case ASK:
                drawable = mContext.getResources().getDrawable(R.drawable.ask,null);
                break;
        }
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        mMessage.setCompoundDrawables(drawable,null,null,null);
    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public MyDialog setTitle(String title) {
        mTitleStr = title;
        return  this;
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    public MyDialog setMessage(String message) {
        mMessageStr = message;
        return  this;
    }


    @Override
    public void show(){
        super.show();
        if (mIsYes && !mIsNo) {
            mNo.setVisibility(View.GONE);
            mYes.setVisibility(View.VISIBLE);
        } else if (mIsNo && !mIsYes) {
            mYes.setVisibility(View.GONE);
            mNo.setVisibility(View.VISIBLE);
        } else {
            mNo.setVisibility(View.VISIBLE);
            mYes.setVisibility(View.VISIBLE);
        }
    }
    public static void displayMessage(String message, Context context){
        final MyDialog builder  = new	MyDialog(context, IconType.INFO);
        builder.setTitle("提示信息").setMessage(message).setNoOnclickListener("确定", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(String message, Context context){
        final MyDialog builder  = new	MyDialog(context, IconType.ERROR);
        builder.setTitle("提示信息").setMessage(message).setNoOnclickListener("取消", Dialog::dismiss).show();
    }

    public static void displayErrorMessage(String message, Context context,onNoOnclickListener no){
        final MyDialog builder  = new	MyDialog(context, IconType.ERROR);
        builder.setTitle("提示信息").setMessage(message).setNoOnclickListener("取消",no).show();
    }

    public static void displayWarnMessage(String message, Context context){
        final MyDialog builder  = new	MyDialog(context, IconType.WARN);
        builder.setTitle("提示信息").setMessage(message).setYesOnclickListener("确定", Dialog::dismiss)
                .setNoOnclickListener("取消", Dialog::dismiss).show();
    }

    public static void displayAskMessage(String message, Context context,MyDialog.onYesOnclickListener yes,MyDialog.onNoOnclickListener no){
        final MyDialog builder  = new	MyDialog(context, IconType.ASK);
        builder.setTitle("提示信息").setMessage(message).setYesOnclickListener("是",yes)
                .setNoOnclickListener("否", no).show();
    }

    public static void displayMessage(String message,String sz,Context context ){
        final MyDialog builder  = new	MyDialog(context);
        builder.setTitle("提示信息").setMessage(message).setNoOnclickListener(sz, Dialog::dismiss).show();
    }

    public static void ToastMessage(final String message,final Context context){
        Toast toast = Toast.makeText(context,message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    public static void ToastMessage(@NonNull View v, final String message, View anchor){
        Snackbar snackbar = Snackbar.make(v,message, Snackbar.LENGTH_SHORT);
        if (anchor != null)snackbar.setAnchorView(anchor);
        View snackbar_view = snackbar.getView();
        snackbar_view.setBackgroundResource(R.drawable.snackbar_background);
        TextView tvSnackbarText = snackbar_view.findViewById(R.id.snackbar_text);
        tvSnackbarText.setTextSize(18);
        snackbar.show();
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