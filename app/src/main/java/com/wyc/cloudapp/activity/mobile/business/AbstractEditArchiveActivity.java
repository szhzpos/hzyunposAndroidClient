package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.view.ViewStub;
import android.widget.Button;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.dialog.CustomProgressDialog;

public abstract class AbstractEditArchiveActivity extends AbstractMobileActivity {
    protected boolean modify;
    private CustomProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initBtn();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initLayout(){
        final ViewStub viewStub = findViewById(R.id.layout_content);
        viewStub.setLayoutResource(getLayout());
        viewStub.inflate();
    }

    private void initBtn(){
        final Button ok_btn = findViewById(R.id.ok_btn),cancel_btn = findViewById(R.id.cancel_btn);
        ok_btn.setOnClickListener(v -> sure());
        cancel_btn.setOnClickListener(v -> onBackPressed());
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
    protected abstract void sure();


    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_abstract_edit_archive;
    }
}