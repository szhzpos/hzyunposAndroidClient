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

import static android.content.Context.WINDOW_SERVICE;

public class ConnSettingDialog extends Dialog {
    private Context mContext;
    public ConnSettingDialog(Context context) {
        super(context,R.style.MyDialog);
        this.mContext = context;
    }

    @Override
    public void  onBackPressed(){
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(this.getLayoutInflater().inflate(R.layout.con_param_setting_dialog, null));
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();

    }
       /**
     * 初始化界面控件
     */
    private void initView() {
        Button save = findViewById(R.id.save),
                cancel = findViewById(R.id.cancel);

        save.setOnClickListener((View v)->{

        });
        cancel.setOnClickListener((View v)->{
            ConnSettingDialog.this.dismiss();
        });

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

}