package com.wyc.cloudapp.CustomizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.DigitKeyboardPopup;
import com.wyc.cloudapp.utils.Utils;

@SuppressLint("AppCompatCustomView")
public class EditTextForHideKeyBoard extends EditText {
    private int mOnFocusTime = 300;
    private boolean mShowSoftKeyboard = false;
    private final OnFocusChangeListener[] mListeners = new OnFocusChangeListener[2];
    public EditTextForHideKeyBoard(Context context) {
        this(context,null);
    }

    public EditTextForHideKeyBoard(Context context, AttributeSet attrs) {
        this(context,attrs,R.attr.editTextStyle);
    }

    public EditTextForHideKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context,attrs,defStyleAttr,0);
    }

    public EditTextForHideKeyBoard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextForHideKeyBoard, 0, 0);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.EditTextForHideKeyBoard_onFocusTime) {
                mOnFocusTime = typedArray.getInteger(index, 300);
            }else if (index == R.styleable.EditTextForHideKeyBoard_showSoftKeyboard){
                mShowSoftKeyboard = typedArray.getBoolean(index,false);
            }
        }
        typedArray.recycle();
        init();
    }
    private void init(){
        setSelectAllOnFocus(true);
        mListeners[0] = (v, hasFocus) -> {
            Utils.hideKeyBoard(this);
            if (mShowSoftKeyboard && hasFocus){
                final DigitKeyboardPopup digitKeyboardPopup = new DigitKeyboardPopup(getContext());
                digitKeyboardPopup.showAtLocation(v);
            }
        };
        super.setOnFocusChangeListener((v, hasFocus) ->{
            for (OnFocusChangeListener listener:mListeners){
                if (null != listener)listener.onFocusChange(v,hasFocus);
            }
        });

        if (mOnFocusTime != 0)postDelayed(this::requestFocus,mOnFocusTime);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l){
        mListeners[1] = l;
    }
}
