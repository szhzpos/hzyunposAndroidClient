package com.wyc.cloudapp.dialog.vip;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;

public class MobileVipChargeDialog extends AbstractDialogMainActivity {
    public MobileVipChargeDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.vip_charge_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSoftKeyBoardListener();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_vip_charge_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void initSoftKeyBoardListener(){
        final View main_window = getWindow().getDecorView();
        SoftKeyBoardListener.setListener(main_window, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                main_window.scrollBy(0,height);
            }

            @Override
            public void keyBoardHide(int height) {
                main_window.scrollBy(0,-height);
            }
        });
    }
}
