package com.wyc.cloudapp.adapter;

import android.os.Looper;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.application.CustomApplication;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: AbstractDataAdapter
 * @Description: Json数据适配器基类
 * @Author: wyc
 * @CreateDate: 2021/2/23 15:36
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/23 15:36
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractDataAdapterForJson<T extends AbstractDataAdapter.SuperViewHolder> extends AbstractDataAdapter<JSONArray,T> {
    public void setDataForArray(final JSONArray array){
        mData = array;
        if (Looper.myLooper() != Looper.getMainLooper()){
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }else
            notifyDataSetChanged();
    }
    public JSONArray getData(){
        return mData;
    }
    public void clear(){
        if (mData != null){
            mData.clear();
            notifyDataSetChanged();
        }
    }
    @Override
    public int getItemCount() {
        return mData == null ? 0: mData.size();
    }
}
