package com.wyc.cloudapp.adapter.report;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
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
    protected MainActivity mContext;
    protected JSONArray mDatas;

    public AbstractDataAdapter(MainActivity activity){
        mContext = activity;
    }

    public static class SuperViewHolder extends RecyclerView.ViewHolder {
        public SuperViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void finalize(){
            Logger.d(getClass().getName() + " finalized");
        }
    }

    public void setDataForArray(final JSONArray array){
        mDatas = array;
        notifyDataSetChanged();
    }
    public JSONArray getData(){
        return mDatas;
    }
    public boolean isEmpty(){
        return getItemCount() == 0;
    }
    public void clear(){
        if (mDatas != null)mDatas.clear();
    }
    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }
}
