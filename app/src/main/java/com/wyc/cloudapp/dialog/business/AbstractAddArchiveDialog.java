package com.wyc.cloudapp.dialog.business;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.business
 * @ClassName: AbstractAddArchiveDialog
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021/5/11 17:03
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/11 17:03
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractAddArchiveDialog extends AbstractDialogMainActivity {
    public AbstractAddArchiveDialog(@NonNull MainActivity context, CharSequence title) {
        super(context, title);
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initBtn();
    }

    private void initLayout(){
        final ViewStub viewStub = findViewById(R.id.layout_content);
        viewStub.setLayoutResource(getLayout());
        viewStub.inflate();
    }

    private void initBtn(){
        final Button ok_btn = findViewById(R.id.ok_btn),cancel_btn = findViewById(R.id.cancel_btn);
        ok_btn.setOnClickListener(v -> sure());
        cancel_btn.setOnClickListener(v -> closeWindow());
    }

    @Override
    protected double getWidthRatio(){
        return 0.98;
    }

    protected abstract int getLayout();
    protected abstract void sure();

    @Override
    protected int getContentLayoutId() {
        return R.layout.add_archive_base_dialog;
    }
}
