package com.wyc.cloudapp.dialog.business;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext;
import com.wyc.cloudapp.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.dialog.business
 * @ClassName: InventoryClearHintDialog
 * @Description: 未盘商品是否清零提示框
 * @Author: wyc
 * @CreateDate: 2022-02-08 14:57
 * @UpdateUser: 更新者
 * @UpdateDate: 2022-02-08 14:57
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class InventoryClearHintDialog extends AbstractDialogContext {
    private int mFlag = 0;
    private String mHint = "";

    public InventoryClearHintDialog(@NonNull Context context) {
        super(context, "提示");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
    }

    private void initView(){
        RadioButton zero_audit = findViewById(R.id.zero_audit),no_zero_audit = findViewById(R.id.no_zero_audit);
        zero_audit.setOnCheckedChangeListener(checkedChangeListener);
        no_zero_audit.setOnCheckedChangeListener(checkedChangeListener);

        TextView hint_tv = findViewById(R.id.hint_tv);
        hint_tv.setText(mHint);

        Button ok = findViewById(R.id.ok),cancel = findViewById(R.id.cancel);
        ok.setOnClickListener(clickListener);
        cancel.setOnClickListener(clickListener);
    }

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
        int id = buttonView.getId();
        if (id == R.id.zero_audit){
            if (isChecked)mFlag = 1;
        }else {
            if (isChecked)mFlag = 0;
        }
    };

    private final View.OnClickListener clickListener = v -> {
        int id = v.getId();
        if (id == R.id.ok){
            setCodeAndExit(1);
        }else if (id == R.id.cancel){
            closeWindow();
        }
    };

    public int getClearFlag(){
        Logger.d("flag:%d",mFlag);
        return mFlag;
    }
    public void setHintMsg(final String msg){
        mHint = msg;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.inventory_clear_hint;
    }
}
