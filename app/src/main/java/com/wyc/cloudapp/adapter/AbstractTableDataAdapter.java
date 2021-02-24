package com.wyc.cloudapp.adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

/*
* 表格数据适配器父类。对表格行选择操作统一实现，如需定制可重写setCurrentItemView、setViewBackgroundColor方法，setItemClickListener可设置回调，回调返回用JSON对象表示的当前行的数据，
* 重写getCurrentRecord可改变默认行为。
* */
public abstract class AbstractTableDataAdapter<T extends AbstractTableDataAdapter.SuperViewHolder> extends AbstractDataAdapter<T> {
    protected View mCurrentItemView;
    private int mCurrentItemIndex = -1;
    private ItemClickCallBack mItemClickCallback;

    public AbstractTableDataAdapter(MainActivity activity) {
        super(activity);
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        if (mCurrentItemIndex == position + 1){
            mCurrentItemView = holder.itemView;
            setViewBackgroundColor(mCurrentItemView,true);
        }
    }

    @Override
    public void onViewRecycled (SuperViewHolder holder){
        if (holder.itemView == mCurrentItemView){
            setViewBackgroundColor(holder.itemView,false);//当前行回收过后有可能用于显示未选中的行，需要重置颜色
        }
    }

    public void setDatas(final String order_code) {

    }

    protected void setCurrentItemView(View v){
        if (mCurrentItemView == null){
            setCurrentItemViewAndIndex(v);
            setViewBackgroundColor(v,true);
        }else if(mCurrentItemView != v){
            setViewBackgroundColor(mCurrentItemView,false);
            setCurrentItemViewAndIndex(v);
            setViewBackgroundColor(v,true);
        }else {
            setViewBackgroundColor(v,false);
            setCurrentItemViewAndIndex(null);
        }
        triggerItemClick();
    }

    protected void triggerItemClick(){
        if (mItemClickCallback != null)mItemClickCallback.onClick(getCurrentRecord());
    }

    protected View.OnClickListener mItemClickListener = this::setCurrentItemView;

    protected void setViewBackgroundColor(final View view, boolean s){
        if(view != null){
            int text_color,selected_color;
            if (s){
                selected_color = mContext.getColor(R.color.listSelected);
                if (isNormalStatus(view))
                    text_color = mContext.getColor(R.color.white);
                else
                    text_color = mContext.getColor(R.color.orange_1);
            } else {
                if (isNormalStatus(view))
                    text_color = mContext.getColor(R.color.text_color);
                else
                    text_color = mContext.getColor(R.color.orange_1);

                selected_color = mContext.getColor(R.color.white);
            }
            view.setBackgroundColor(selected_color);
            if (view instanceof ViewGroup){
                final ViewGroup linearLayout = (ViewGroup)view;
                int count = linearLayout.getChildCount();
                View ch;
                for (int i = 0;i < count;i++){
                    ch = linearLayout.getChildAt(i);
                    if (ch instanceof TextView){
                        ((TextView) ch).setTextColor(text_color);
                    }
                }
            }
        }
    }

    void setCurrentItemViewAndIndex(final View v){
        mCurrentItemView = v;
        if (v == null){
            mCurrentItemIndex = -1;
        }else{
            final TextView tv = v.findViewById(R.id.row_id);
            if (tv == null){
                mCurrentItemIndex = -1;
            }else{
                try {
                    mCurrentItemIndex = Integer.parseInt(tv.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    mCurrentItemIndex = -1;
                }
            }
        }
    }

    private boolean isNormalStatus(final @NonNull View view){
        //判断数据行是否正常。数据行所在的view需存在id为getStatusViewId的返回值的子view，将数据行的状态值存放在子view的tag中,子类可重写getNormalStatus定制状态值,重写getStatusViewId定制子view；
        return Utils.getViewTagValue(view.findViewById(getStatusViewId()),1) == getNormalStatus();
    }

    protected int getNormalStatus(){//
        return 1;
    }
    protected int getStatusViewId(){
        return R.id._status;
    }

    protected JSONObject getCurrentRecord(){
        return (mDatas == null || mCurrentItemIndex == -1 || mCurrentItemIndex > mDatas.size()) ? null : mDatas.getJSONObject(mCurrentItemIndex - 1);
    }

    public interface ItemClickCallBack{
        void onClick(final JSONObject record);
    }
    public void setItemClickListener(final ItemClickCallBack clickCallBack){
        mItemClickCallback = clickCallBack;
    }
}
