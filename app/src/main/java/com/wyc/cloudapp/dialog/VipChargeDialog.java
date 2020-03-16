package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class VipChargeDialog extends Dialog {

    private EditText mC_amt;
    private Context mContext;
    private String mPayMethodId;
    public VipChargeDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.vip_charge_dialog_content);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //初始化金额text
        init_c_amount();

        //初始化支付方式
        intiPayMethod();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(view->VipChargeDialog.this.dismiss());
        findViewById(R.id._back).setOnClickListener(v -> {
            View view =  getCurrentFocus();
            if (view != null) {
                if (view.getId() == R.id._back) {
                    EditText tmp_edit = ((EditText)view);
                    int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                    if (index != end && end  == tmp_edit.getText().length()){
                        tmp_edit.setText(mContext.getString(R.string.space_sz));
                    }else{
                        if (index == 0)return;
                        tmp_edit.getText().delete(index - 1, index);
                    }
                }
            }
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id._ok)){
                tmp_v.setOnClickListener(button_click);
            }
        }
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.c_amt) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };

    private void init_c_amount(){
        mC_amt = findViewById(R.id.c_amt);
        mC_amt.setSelectAllOnFocus(true);
        mC_amt.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
        mC_amt.postDelayed(()->{mC_amt.requestFocus();},300);
    }

    private void intiPayMethod(){
        StringBuilder err = new StringBuilder();
        JSONArray array = SQLiteHelper.getList("select pay_method_id,name from pay_method where status = '1' order by sort",0,0,false,err);
        if (array != null){
            Spinner m_vip_level = findViewById(R.id.pay_method_spinner);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,R.layout.drop_down_style);
            if (array.length() != 0){
                mPayMethodId = array.optJSONObject(0).optString("pay_method_id");
                for(int i = 0,length = array.length();i < length;i++){
                    JSONObject object = array.optJSONObject(i);
                    arrayAdapter.add(object.optString("name"));
                }
                m_vip_level.setAdapter(arrayAdapter);
                m_vip_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        JSONObject jsonObject = array.optJSONObject(position);
                        if (jsonObject != null)
                            mPayMethodId = jsonObject.optString("pay_method_id");

                        Logger.d("mPayMethodId:%s",mPayMethodId);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }else {
            MyDialog.ToastMessage("初始化支付方式错误：" + err, mContext);
        }
    }
}
