package com.wyc.cloudapp.adapter;

import android.os.Looper;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;

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
    protected JSONArray mDatas;

    public static class SuperViewHolder extends RecyclerView.ViewHolder {
        public SuperViewHolder(View itemView) {
            super(itemView);
        }

        protected <T extends View> T  findViewById(@IdRes int id){
            return itemView.findViewById(id);
        }

        @Override
        protected void finalize(){
            Logger.d(getClass().getName() + " finalized");
        }
    }

    public void setDataForArray(final JSONArray array){
        mDatas = array;
        if (Looper.myLooper() != Looper.getMainLooper()){
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }else
            notifyDataSetChanged();
    }
    public JSONArray getData(){
        return mDatas;
    }
    public boolean isEmpty(){
        return getItemCount() == 0;
    }
    public void clear(){
        if (mDatas != null){
            mDatas.clear();
            notifyDataSetChanged();
        }
    }
    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }
}
