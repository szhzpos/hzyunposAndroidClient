package com.wyc.cloudapp.adapter.business;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.business.AbstractMobileBusinessOrderActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.utils.Utils;

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
public abstract class AbstractBusinessOrderDataAdapter<T extends AbstractTableDataAdapter.SuperViewHolder > extends AbstractDataAdapter<T> implements View.OnClickListener {
    final MainActivity mContext;
    public AbstractBusinessOrderDataAdapter(MainActivity activity) {
        mContext = activity;
    }

    @Override
    public void onClick(View v) {
        if (mContext instanceof AbstractMobileBusinessOrderActivity){
            final AbstractMobileBusinessOrderActivity activity = (AbstractMobileBusinessOrderActivity)mContext;

            final Intent intent = new Intent();
            intent.putExtra("order_id",Utils.getViewTagValue(v,""));
            if (activity.isFindSourceOrderId()){
                activity.setResult(Activity.RESULT_OK,intent);
                activity.finish();
            }else {
                intent.setClass(activity, activity.jumpAddTarget());
                intent.putExtra("title", mContext.getString(R.string.order_detail_sz));
                activity.startActivity(intent);
            }
        }else
            throw new IllegalArgumentException("mContext must extends AbstractMobileBusinessOrderActivity!");
    }
}
