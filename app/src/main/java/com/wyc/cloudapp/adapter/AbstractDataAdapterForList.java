package com.wyc.cloudapp.adapter;

import android.os.Looper;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.application.CustomApplication;

import java.util.List;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: AbstractDataAdapterForObject
 * @Description: bean 对象数据适配器基类
 * @Author: wyc
 * @CreateDate: 2021-06-07 17:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-07 17:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractDataAdapterForList<E,T extends AbstractDataAdapter.SuperViewHolder> extends AbstractDataAdapter<List<E>,T> {
    public void setDataForList(final List<E> array){
        mData = array;
        if (Looper.myLooper() != Looper.getMainLooper()){
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }else
            notifyDataSetChanged();
    }
    public List<E> getList(){
        return mData;
    }
    public boolean isEmpty(){
        return getItemCount() == 0;
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
