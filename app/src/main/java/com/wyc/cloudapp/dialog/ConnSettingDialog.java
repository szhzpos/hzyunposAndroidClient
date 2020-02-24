package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Point;
import android.os.Bundle;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import android.text.Editable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.WINDOW_SERVICE;

public class ConnSettingDialog extends Dialog {
    private Context mContext;
    private EditText mUrl,mAppId,mAppscret;
    public ConnSettingDialog(Context context) {
        super(context,R.style.MyDialog);
        this.mContext = context;
    }

    @Override
    public void  onBackPressed(){
        super.onBackPressed();
    }

    @Override
    public void show(){
        super.show();
        JSONObject param = new JSONObject();
        if(SQLiteHelper.getLocalParameter("connParam",param)){
            mUrl.setText(param.optString("server_url"));
            mAppId.setText(param.optString("appId"));
            mAppscret.setText(param.optString("appScret"));
        }else{
            try {
                Utils.displayMessage(param.getString("info"),mContext);
            } catch (JSONException e) {
                Utils.displayMessage(e.getMessage(),mContext);
                e.printStackTrace();
            }
        }
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

        mUrl = findViewById(R.id.server_url);
        mAppId = findViewById(R.id.appId);
        mAppscret = findViewById(R.id.appSecret);

        mUrl.setSelectAllOnFocus(true);
        mAppscret.setSelectAllOnFocus(true);
        mAppId.setSelectAllOnFocus(true);

        save.setOnClickListener((View v)->{
            JSONObject json = new JSONObject(),param = new JSONObject();
            String url = mUrl.getText().toString();
            try {
                if (!url.contains("http")){
                    url = "http://" + url;
                }
                json.put("server_url",url);
                json.put("appId",mAppId.getText());
                json.put("appScret",mAppscret.getText());

                param.put("parameter_id","connParam");
                param.put("parameter_content",json);
                StringBuilder err = new StringBuilder();
                if (SQLiteHelper.replaceJson(param,"local_parameter",null,err)){
                    Utils.ToastMessage("保存成功！",mContext);
                    ConnSettingDialog.this.dismiss();
                }else
                    Utils.displayMessage(err.toString(),v.getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
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