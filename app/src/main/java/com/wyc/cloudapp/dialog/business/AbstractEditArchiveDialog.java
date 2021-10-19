package com.wyc.cloudapp.dialog.business;

import android.os.Bundle;
import android.view.ViewStub;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
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
public abstract class AbstractEditArchiveDialog extends AbstractDialogMainActivity {
    protected boolean modify;
    private CustomProgressDialog mProgressDialog;
    public AbstractEditArchiveDialog(@NonNull MainActivity context, CharSequence title, boolean b) {
        super(context, title);
        modify = b;
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initBtn();
    }
    protected final void showProgress(){
        final String mess = mContext.getString(R.string.hint_save_sz);
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(mContext,mess);
        else
            mProgressDialog.setMessage(mess).refreshMessage();

        if (!mProgressDialog.isShowing())mProgressDialog.show();
    }

    protected final void dismissProgress(){
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
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
        return 0.90;
    }

    protected abstract int getLayout();
    protected abstract void sure();

    @Override
    protected int getContentLayoutId() {
        return R.layout.add_archive_base_dialog;
    }
}
