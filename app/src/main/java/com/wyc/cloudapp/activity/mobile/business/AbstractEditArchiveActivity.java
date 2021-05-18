package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.view.ViewStub;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class AbstractEditArchiveActivity extends AbstractMobileActivity {
    private CustomProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();

        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        if (isExist() || MyDialog.showMessageToModalDialog(this,"是否退出?") == 1){
            super.onBackPressed();
        }
    }

    private void initLayout(){
        final ViewStub viewStub = findViewById(R.id.layout_content);
        viewStub.setLayoutResource(getLayout());
        viewStub.inflate();
    }

    protected final void showProgress(){
        final String mess = getString(R.string.hint_save_sz);
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(this,mess);
        else
            mProgressDialog.setMessage(mess).refreshMessage();

        if (!mProgressDialog.isShowing())mProgressDialog.show();
    }

    protected final void dismissProgress(){
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    protected abstract int getLayout();
    @OnClick(R.id.ok_btn)
    protected abstract void sure();
    @OnClick(R.id.cancel_btn)
    protected abstract void saveAndAdd();
    protected boolean isExist(){return true;}

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_abstract_edit_archive;
    }
}