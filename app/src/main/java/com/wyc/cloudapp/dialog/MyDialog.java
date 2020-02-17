package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;

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

    private Button yes;//确定按钮
    private Button no;//取消按钮
    private TextView titleTv;//消息标题文本
    private TextView messageTv;//消息提示文本
    private String titleStr;//从外界设置的title文本
    private String messageStr;//从外界设置的消息文本
    private Context context;
    private boolean isYes,isNo;
    //确定文本和取消文本的显示内容
    private String yesStr, noStr;

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
            noStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
        this.isNo = true;

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
            yesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
        this.isYes = true;

        return  this;
    }

    public MyDialog(Context context) {
        super(context,R.style.MyDialog);
        this.context = context;
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
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick(MyDialog.this);
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoClick(MyDialog.this);
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            titleTv.setText(titleStr);
        }
        if (messageStr != null) {
            messageTv.setText(messageStr);
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            yes.setText(yesStr);
        }
        if (noStr != null) {
            no.setText(noStr);
        }

    }
    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);
        titleTv = (TextView) findViewById(R.id.title_text);
        messageTv = (TextView) findViewById(R.id.content);

        WindowManager m = (WindowManager)context.getSystemService(WINDOW_SERVICE);
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
        titleStr = title;
        return  this;
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    public MyDialog setMessage(String message) {
        messageStr = message;
        return  this;
    }


    @Override
    public void show(){
        super.show();
        if (isYes && !isNo) {
            this.no.setVisibility(View.GONE);
            this.yes.setVisibility(View.VISIBLE);
        } else if (isNo && !isYes) {
            this.yes.setVisibility(View.GONE);
            this.no.setVisibility(View.VISIBLE);
        } else {
            this.no.setVisibility(View.VISIBLE);
            this.yes.setVisibility(View.VISIBLE);
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