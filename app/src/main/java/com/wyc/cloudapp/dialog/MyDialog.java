package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import com.wyc.cloudapp.R;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Administrator on 2018-03-15.
 */

public class MyDialog extends Dialog {
    private Button mYes,mNo;//mYes确定按钮、mNo取消按钮
    private TextView mTitle,mMessage;//mTitle标题文本、mMessage消息提示文本
    private String mTitleStr;//从外界设置的title文本
    private String mMessageStr;//从外界设置的消息文本
    private Context mContext;
    private int mMessageColor = Color.WHITE;
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

    @Override
    public void  onBackPressed(){
        if (noOnclickListener != null)noOnclickListener.onNoClick(this);
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(this.getLayoutInflater().inflate(R.layout.mydialog, null));
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
            mMessage.setTextColor(mMessageColor);
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
        mYes = (Button) findViewById(R.id.yes);
        mNo = (Button) findViewById(R.id.no);
        mTitle = (TextView) findViewById(R.id.title_text);
        mMessage = (TextView) findViewById(R.id.content);

        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = (int)(0.4 * point.x); // 宽度
                dialogWindow.setAttributes(lp);
            }
        }
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
    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onYesOnclickListener {
          void onYesClick(MyDialog myDialog);
    }

    public interface onNoOnclickListener {
          void onNoClick(MyDialog myDialog);
    }

    public MyDialog setTitleColor(int c){
        mMessageColor = c;
        return this;
    }

}