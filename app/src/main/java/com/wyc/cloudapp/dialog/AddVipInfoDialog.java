package com.wyc.cloudapp.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;

import org.json.JSONObject;

import java.util.Locale;

public class AddVipInfoDialog extends Dialog {
    private Context mContext;
    private EditText m_vip_p_num,m_card_id,m_vip_name,m_vip_birthday;
    Spinner m_vip_level,m_vip_sex;
    private onYesOnclickListener mYesOnclickListener;//确定按钮被点击了的监听器
    public AddVipInfoDialog(@NonNull Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.add_vip_dialog_content);

        //初始化成员变量
        m_vip_p_num = findViewById(R.id.n_vip_p_num);
        m_card_id = findViewById(R.id.n_card_id);
        m_vip_name = findViewById(R.id.n_vip_name);
        m_vip_birthday = findViewById(R.id.n_vip_birthday);
        m_vip_level = findViewById(R.id.n_vip_level);
        m_vip_sex = findViewById(R.id.n_vip_sex);

        //初始化按钮
        findViewById(R.id._close).setOnClickListener(view->AddVipInfoDialog.this.dismiss());
        findViewById(R.id.cancel).setOnClickListener(view->AddVipInfoDialog.this.dismiss());
        findViewById(R.id._ok).setOnClickListener(view -> {
            if (mYesOnclickListener != null){
                mYesOnclickListener.onYesClick(AddVipInfoDialog.this);
            }
        });
        m_vip_birthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    Calendar c = Calendar.getInstance();
                    // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
                    new DatePickerDialog(view.getContext(),
                            // 绑定监听器
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    m_vip_birthday.setText(String.format(Locale.CHINA,"%d-%02d-%02d", view.getYear(), view.getMonth() + 1,view.getDayOfMonth()));
                                }
                            }
                            // 设置初始日期
                            , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                            .get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

    }

    public AddVipInfoDialog setYesOnclickListener(onYesOnclickListener listener) {
        if (listener != null){
            mYesOnclickListener = listener;
        }
        return this;
    }

    public JSONObject getVipInfo(){
        JSONObject object = new JSONObject();


        return object;
    }

    public interface onYesOnclickListener {
        void onYesClick(AddVipInfoDialog dialog);
    }

}
