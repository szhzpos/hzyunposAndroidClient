package com.wyc.cloudapp.adapter;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: AbstractDataAdapter
 * @Description: 数据适配器基类
 * @Author: wyc
 * @CreateDate: 2021-06-07 17:33
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-07 17:33
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractDataAdapter<D,T extends AbstractDataAdapter.SuperViewHolder> extends RecyclerView.Adapter<T>{
    protected D mData;
    public static class SuperViewHolder extends RecyclerView.ViewHolder {
        public SuperViewHolder(View itemView) {
            super(itemView);
        }

       final protected <T extends View> T  findViewById(@IdRes int id){
            return itemView.findViewById(id);
        }
    }
    public final boolean isEmpty(){
        return mData == null || getItemCount() == 0;
    }
}
