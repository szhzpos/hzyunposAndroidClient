package com.wyc.cloudapp.dialog.CustomizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

@SuppressLint("AppCompatCustomView")
public class EditTextForHideKeyBoard extends EditText {
    private int mOnFocusTime = 300;
    public EditTextForHideKeyBoard(Context context) {
        super(context);
        init();
    }

    public EditTextForHideKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextForHideKeyBoard, 0, 0);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.EditTextForHideKeyBoard_onFocusTime) {
                mOnFocusTime = typedArray.getInteger(index, 300);
            }
        }
        typedArray.recycle();
        init();
    }

    public EditTextForHideKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public EditTextForHideKeyBoard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setSelectAllOnFocus(true);
        setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard(this));
        if (mOnFocusTime != 0)postDelayed(this::requestFocus,mOnFocusTime);
    }
}
