package com.wyc.cloudapp.dialog;

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
import com.wyc.cloudapp.logger.Logger;

public class DigitKeyboardPopup extends PopupWindow {
    private EditText mFocusView;
    private final View mUpHintView;
    private final View mDownHintView;
    public DigitKeyboardPopup(@NonNull Context context) {
        super(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.digit_keyboard_layout,null);
        mUpHintView = view.findViewById(R.id.up);
        mDownHintView = view.findViewById(R.id.down);

        setContentView(view);
        initKeyboard(view);

        setBackgroundDrawable(context.getDrawable(R.color.transparent));
        setOutsideTouchable(true);
        setOnDismissListener(() -> {
            if (mFocusView != null)mFocusView.clearFocus();
        });
    }

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }


    @Override
    public void showAsDropDown(@NonNull View view){
        if (view instanceof EditText) {
            mFocusView = (EditText) view;
        }
        final View contentView = getContentView();
        contentView.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST);
        final int content_width = contentView.getMeasuredWidth();
        super.showAsDropDown(view,content_width - view.getWidth(),0);
    }

    public void showAtLocation(final View view){
        if (null != view){
            if (view instanceof EditText) {
                mFocusView = (EditText) view;
            }
            int[] windowPos = calculatePopWindowPos(view, getContentView());

            super.showAtLocation(view, android.view.Gravity.TOP | android.view.Gravity.START,windowPos[0] - view.getWidth()/2,windowPos[1]);
        }
    }

    private void initKeyboard(final View keyboard){
        final ConstraintLayout keyboard_linear_layout = keyboard.findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                final View tmp_v = keyboard_linear_layout.getChildAt(i);
                tmp_v.setOnClickListener(mKeyboardListener);
            }
    }
    private final View.OnClickListener mKeyboardListener = new View.OnClickListener() {
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
                        if (index != 0 && editable.length() != 0){
                            editable.delete(index - 1,index);
                        }

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

    private int[] calculatePopWindowPos(@NonNull final View anchorView,@NonNull final View contentView) {
        final int[] windowPos = new int[2];
        final int[] anchorLoc = new int[2];
        anchorView.getLocationInWindow(anchorLoc);
        final int anchorHeight = anchorView.getHeight();

        final View root_view = anchorView.getRootView();
        final int root_view_h = root_view.getHeight();;
        final int root_view_w = root_view.getWidth();


        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int content_height = contentView.getMeasuredHeight();
        final int content_width = contentView.getMeasuredWidth();
        final boolean isNeedShowUp = (root_view_h - anchorLoc[1] - anchorHeight < content_width);
        final View up = mUpHintView,down = mDownHintView;
        if (isNeedShowUp) {
            if (down != null && down.getVisibility() == View.GONE)down.setVisibility(View.VISIBLE);
            if (up != null && up.getVisibility() == View.VISIBLE)up.setVisibility(View.GONE);
            windowPos[0] = root_view_w - content_width;
            windowPos[1] = anchorLoc[1] - content_height;
        } else {

            if (up != null && up.getVisibility() == View.GONE)up.setVisibility(View.VISIBLE);
            if (down != null && down.getVisibility() == View.VISIBLE)down.setVisibility(View.GONE);

            windowPos[0] = root_view_w - content_width;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }
}
