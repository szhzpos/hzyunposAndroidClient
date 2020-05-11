package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

public class BaseDialog extends Dialog {
    protected MainActivity mContext;
    protected String mTitle;
    public BaseDialog(@NonNull MainActivity context,final String title) {
        super(context);
        mContext = context;
        mTitle = title;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setContentView(R.layout.base_dialog_layout);

        setTitle();
        closeWindow();
    }
    protected void setContentLayout(int res_id){
        final LinearLayout main_layout = findViewById(R.id.dialog_main_layout);
        if (null != main_layout){
            final View dialog_content = View.inflate(mContext, res_id, null);
            if (dialog_content != null)
                main_layout.addView(dialog_content,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }
    private void setTitle(){
        final TextView title_tv = findViewById(R.id.title);
        if (null != title_tv && null != mTitle){
            title_tv.setText(mTitle);
        }
    }

    protected void closeWindow(){
        final Button _close = findViewById(R.id._close);
        if (_close != null){
            _close.setOnClickListener(v -> BaseDialog.this.dismiss());
        }
    }

}
