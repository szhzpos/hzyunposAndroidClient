package com.wyc.cloudapp.adapter.business;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.AbstractMobileBusinessOrderActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: AbstractBusinessDataAdapter
 * @Description: 业务数据适配器抽象类
 * @Author: wyc
 * @CreateDate: 2021/2/22 17:09
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/22 17:09
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractBusinessDataAdapter<T extends AbstractTableDataAdapter.SuperViewHolder > extends AbstractDataAdapter<T> implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        if (mContext instanceof AbstractMobileBusinessOrderActivity){
            final AbstractMobileBusinessOrderActivity activity = (AbstractMobileBusinessOrderActivity)mContext;
            final Intent intent = new Intent();
            intent.setClass(mContext, activity.jumpAddTarget());
            intent.putExtra("order_id",(String) v.getTag());
            intent.putExtra("title", mContext.getString(R.string.order_detail_sz));
            mContext.startActivity(intent);
        }else
            throw new IllegalArgumentException("mContext must extends AbstractMobileBusinessOrderActivity!");
    }
}
