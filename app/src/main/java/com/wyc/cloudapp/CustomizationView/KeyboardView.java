package com.wyc.cloudapp.CustomizationView;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.wyc.cloudapp.R;
public final class KeyboardView extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private OnCurrentFocusListener mCurrentFocusListener;
    private Button mCancel,mOk;
    public KeyboardView(Context context) {
        this(context,null);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public void onMeasure(int widthMeaSpec,int heightMeaSpec){
        super.onMeasure(widthMeaSpec,heightMeaSpec);
    }

    @Override
    public void onLayout(boolean change,int left,int top,int right,int bottom){
        super.onLayout(change,left,top,right,bottom);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
    }

    private void initKeyboard(){
        final ConstraintLayout keyboard_layout = findViewById(R.id.keyboard);
        if (null != keyboard_layout)
            for (int i = 0,child  =  keyboard_layout.getChildCount(); i < child;i++){
                final View tmp_v =  keyboard_layout.getChildAt(i);
                int id = tmp_v.getId();
                if (tmp_v instanceof Button){
                    switch (id) {
                        case R.id._back:
                            tmp_v.setOnClickListener(v -> {
                                if (mCurrentFocusListener != null){
                                    final EditText view =  mCurrentFocusListener.getFocusEditText();
                                    if (view != null) {
                                        int index = view.getSelectionStart(), end = view.getSelectionEnd();
                                        if (index != end && end == view.getText().length()) {
                                            view.setText(mContext.getString(R.string.space_sz));
                                        } else {
                                            if (index == 0) return;
                                            view.getText().delete(index - 1, index);
                                        }
                                    }
                                }
                            });
                            break;
                        case R.id._cancel:
                            mCancel = (Button) tmp_v;
                            break;
                        case R.id._ok:
                            mOk = (Button) tmp_v;
                            break;
                        default:
                            tmp_v.setOnClickListener(this);
                            break;
                    }
                }
            }
    }

    @Override
    public void onClick(View v) {
        if (mCurrentFocusListener != null){
            final EditText view =  mCurrentFocusListener.getFocusEditText();
            if (view != null) {
                int index = view.getSelectionStart();
                final Editable editable = view.getText();
                final String sz_button = ((Button) v).getText().toString();
                if (index != view.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    }

    public interface OnCurrentFocusListener {
       EditText getFocusEditText();
   }
   public void setCurrentFocusListener(OnCurrentFocusListener onCurrentFocusListener){
       mCurrentFocusListener = onCurrentFocusListener;
   }

   public void setCancelListener(OnClickListener listener){
       if (mCancel != null)mCancel.setOnClickListener(listener);
   }
    public void setOkListener(OnClickListener listener){
        if (mOk != null)mOk.setOnClickListener(listener);
    }
    public Button getOkBtn(){
       return mOk;
    }

    public void layout(int res_id){
        LayoutInflater.from(mContext).inflate(res_id, this, true);
        initKeyboard();
    }


}
