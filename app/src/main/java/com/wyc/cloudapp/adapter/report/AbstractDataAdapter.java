package com.wyc.cloudapp.adapter.report;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: AbstractDataAdapter
 * @Description: 数据适配器基类
 * @Author: wyc
 * @CreateDate: 2021/2/23 15:36
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/23 15:36
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractDataAdapter <T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    protected MainActivity mContext;
    protected JSONArray mDatas;

    public void setDataForArray(final JSONArray array){
        mDatas = array;
        notifyDataSetChanged();
    }
    public JSONArray getData(){
        return mDatas;
    }
    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }
}
