package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;

import static com.wyc.cloudapp.activity.mobile.business.MobileSelectGoodsActivity.TITLE_KEY;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.business
 * @ClassName: AbstractMobileBaseArchiveActivity
 * @Description: 新建基础档案基类
 * @Author: wyc
 * @CreateDate: 2021/5/10 10:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/10 10:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractMobileBaseArchiveActivity extends AbstractMobileActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle();
    }
    private void setTitle(){
        setMiddleText(title());
        setRightText(getString(R.string.add_sz));
        setRightListener(v -> add());
    }
    protected abstract void add();
    protected String title(){
        return getIntent().getStringExtra(TITLE_KEY);
    }
}
