package com.wyc.cloudapp.adapter;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.mobile.SelectTimeCardActivity;
import com.wyc.cloudapp.bean.TimeCardInfo;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: AbstractSelectAdapter
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021-07-21 16:03
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-21 16:03
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractSelectAdapter<E extends Parcelable,T extends AbstractDataAdapter.SuperViewHolder> extends AbstractDataAdapterForList<E,T> {
    private OnSelectFinishListener<E> mListener;
    public interface OnSelectFinishListener<E>{
        void onFinish(@NonNull E item);
    }
    public void setSelectListener(OnSelectFinishListener<E> mListener) {
        this.mListener = mListener;
    }
    public void invoke(E item){
        if (mListener != null)mListener.onFinish(item);
    }
    public boolean hasListener(){
        return null != mListener;
    }
}
