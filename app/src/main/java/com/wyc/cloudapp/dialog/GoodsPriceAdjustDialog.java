package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.CustomizationView.KeyboardView;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;

public final class GoodsPriceAdjustDialog extends AbstractDialogBaseOnMainActivityImp {
    public GoodsPriceAdjustDialog(@NonNull MainActivity context) {
        super(context, context.getText(R.string.price_adjust_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initKeyboardView();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.goods_price_adjust_dialog_layout;
    }

    private void initKeyboardView(){
        final KeyboardView view = findViewById(R.id.keyboard_view);
        view.layout(R.layout.change_price_keyboard_layout);
        view.setCurrentFocusListenner(() -> {
            final View focus = getCurrentFocus();
            if (focus instanceof EditText){
                return (EditText) focus;
            }
            return null;
        });
        view.setCancelListener(v -> closeWindow());
        view.setOkListener(v -> {

        });
    }
}
