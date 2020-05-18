package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.wyc.cloudapp.R;

public class DigitKeyboardPopup extends PopupWindow {
    private EditText mFocusView;
    public DigitKeyboardPopup(@NonNull Context context) {
        super(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.digit_keyboard_layout,null);
        setContentView(view);
        initKeyboard(view);

        setBackgroundDrawable(context.getDrawable(R.color.transparent));
        setOutsideTouchable(true);
    }

    @Override
    public void showAsDropDown(View v){
        super.showAsDropDown(v,- v.getWidth() / 2,0);
        if (v instanceof EditText) {
            mFocusView = (EditText) v;
        }
    }
    private void initKeyboard(View keyboard){
        final ConstraintLayout keyboard_linear_layout = keyboard.findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                final View tmp_v = keyboard_linear_layout.getChildAt(i);
                tmp_v.setOnClickListener(mKeyboardListener);
            }
    }
    private View.OnClickListener mKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int v_id = view.getId();
            final EditText et_view =  mFocusView;
            if (null != et_view){
                final Editable editable = et_view.getText();
                int index = et_view.getSelectionStart(),end = et_view.getSelectionEnd();
                if (v_id == R.id._back){
                    if (index !=end && end == editable.length()){
                        editable.clear();
                    }else{
                        if (index != 0 && editable.length() != 0)
                            editable.delete(editable.length() - 1,editable.length());
                    }
                }else{
                    if (et_view.getSelectionStart() != et_view.getSelectionEnd()){
                        editable.replace(0,editable.length(),((Button)view).getText());
                        et_view.setSelection(editable.length());
                    }else
                        editable.append(((Button)view).getText());
                }
            }
        }
    };

}
