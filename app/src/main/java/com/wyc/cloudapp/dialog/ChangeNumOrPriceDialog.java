package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

import static android.content.Context.WINDOW_SERVICE;

public class ChangeNumOrPriceDialog extends Dialog {
    private Context mContext;
    private EditText new_price_text;
    private TextView mTitle;
    private String szTitle;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    public ChangeNumOrPriceDialog(Context context){
        super(context);
        this.mContext = context;
    }
    public ChangeNumOrPriceDialog(Context context,String sztitle){
        this(context);
        this.szTitle = sztitle;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(this.getLayoutInflater().inflate(R.layout.change_price_content, null));
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        new_price_text = findViewById(R.id.new_numOrprice_text);
        mTitle = findViewById(R.id.title_text);

        Button yes_button,no_button,back_button ;
        LinearLayout keyboard_linear_layout;

        yes_button = findViewById(R.id.yes);
        no_button = findViewById(R.id.no);
        back_button = findViewById(R.id._back);



        keyboard_linear_layout = findViewById(R.id.keyboard);

        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null){
                    yesOnclickListener.onYesClick(ChangeNumOrPriceDialog.this);
                }
            }
        });

        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null){
                    noOnclickListener.onNoClick(ChangeNumOrPriceDialog.this);
                }
            }
        });
        new_price_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)Utils.hideKeyBoard((EditText)v);
            }
        });
        new_price_text.postDelayed(()->{
            new_price_text.requestFocus();
        },300);
        new_price_text.setSelection(0);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view =  getCurrentFocus();
                if (view != null) {
                    EditText tmp_edit = ((EditText)view);
                    int index = tmp_edit.getSelectionStart();
                    if (index == 0)return;
                    switch (view.getId()){
                        case R.id.new_numOrprice_text:
                            if (index == tmp_edit.getText().toString().indexOf(".") + 1){
                                tmp_edit.setSelection(index - 1);
                            }else if (index > tmp_edit.getText().toString().indexOf(".")){
                                tmp_edit.getText().replace(index - 1, index,"0");
                                tmp_edit.setSelection(index - 1);
                            }else {
                                tmp_edit.getText().delete(index - 1, index);
                            }
                            break;
                    }
                }
            }
        });

        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            LinearLayout linearLayout = (LinearLayout)keyboard_linear_layout.getChildAt(i);
            for (int j = 0,len = linearLayout.getChildCount();j < len; j++){
                View tmp_v = linearLayout.getChildAt(j);
                int id = tmp_v.getId();
                if (tmp_v instanceof Button && !(id == R.id._back)){
                    tmp_v.setOnClickListener(button_click);
                }
            }
        }

/*        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
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
        }*/

    }

    @Override
    public void dismiss(){
        super.dismiss();
        new_price_text.setText("");
        if (szTitle != null)szTitle = null;
    }
    @Override
    public void show(){
        super.show();
        if (szTitle != null)mTitle.setText(szTitle);
    }

    public ChangeNumOrPriceDialog  setNoOnclickListener(onNoOnclickListener onNoOnclickListener) {
        this.noOnclickListener = onNoOnclickListener;
        return this;
    }

    public ChangeNumOrPriceDialog setYesOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.yesOnclickListener = onYesOnclickListener;
        return  this;
    }

    public double getNewNumOrPrice(){
        return  Double.valueOf(new_price_text.getText().toString());
    }

    public interface onYesOnclickListener {
        void onYesClick(ChangeNumOrPriceDialog myDialog);
    }

    public interface onNoOnclickListener {
        void onNoClick(ChangeNumOrPriceDialog myDialog);
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            EditText tmp_edit = ((EditText)view);
            int index = tmp_edit.getSelectionStart();
            String sz_button = ((Button) v).getText().toString();
            switch (view.getId()){
                case R.id.new_numOrprice_text:
                    if (".".equals(sz_button)) {
                        tmp_edit.setSelection(tmp_edit.getText().toString().indexOf(".") + 1);
                    }else {
                        if (index > tmp_edit.getText().toString().indexOf(".")){
                            if (index != tmp_edit.length())
                                tmp_edit.getText().delete(index,index + 1).insert(index,sz_button);
                        }else {
                            if(index == 0 && "0".equals(sz_button) )return;
                            tmp_edit.getText().insert(index, sz_button);
                        }
                    }
                    break;
            }

        }
    };

}
