package com.wyc.cloudapp.adapter.business;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: AbstractBusinessOrderDetailsDataAdapter
 * @Description: 业务单据商品明细适配器
 * @Author: wyc
 * @CreateDate: 2021/3/2 17:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/2 17:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractBusinessOrderDetailsDataAdapter<T extends AbstractTableDataAdapter.SuperViewHolder > extends AbstractDataAdapter<T> {
    public AbstractBusinessOrderDetailsDataAdapter(MainActivity activity) {
        super(activity);
    }
    public void addDetails(@Nullable final JSONObject object){
        if (object != null){
            if (mDatas == null)mDatas = new JSONArray();

            mDatas.add(object);

            notifyDataSetChanged();
        }
    }
}
