package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.utils.Utils;

public abstract class AbstractDetailsDataAdapter <T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>  {
    protected MainActivity mContext;
    protected JSONArray mDatas;
    protected View mCurrentItemView;
    private ItemClickCallBack mItemClickCallback;
    protected void setCurrentItemView(View v){
        if (mCurrentItemView == null){
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }else if(mCurrentItemView != v){
            setViewBackgroundColor(mCurrentItemView,false);
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }else {
            setViewBackgroundColor(v,false);
            mCurrentItemView = null;
        }
        if (mItemClickCallback != null)mItemClickCallback.onClick(getCurrentPayRecord());
    }
    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null){
            int text_color,selected_color;
            if (s){
                selected_color = mContext.getColor(R.color.listSelected);
                text_color = mContext.getColor(R.color.white);
            } else {
                text_color = mContext.getColor(R.color.text_color);
                selected_color = mContext.getColor(R.color.white);
            }
            view.setBackgroundColor(selected_color);
            if (view instanceof LinearLayout){
                LinearLayout linearLayout = (LinearLayout)view;
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

    public interface ItemClickCallBack{
        void onClick(final JSONObject pay_record);
    }
    public void setItemClickListener(final ItemClickCallBack clickCallBack){
        mItemClickCallback = clickCallBack;
    }

    private JSONObject getCurrentPayRecord(){
        if (mCurrentItemView != null){
            final TextView name = mCurrentItemView.findViewById(R.id.pay_method_name);
            if (name != null){
                int pay_method_id = Utils.getViewTagValue(name,-1);
                if (pay_method_id != -1){
                    for (int i = 0,size = mDatas.size();i < size; i++){
                        final JSONObject pay_record = mDatas.getJSONObject(i);
                        if (null != pay_record && pay_method_id == pay_record.getIntValue("pay_method")){
                            return pay_record;
                        }
                    }
                }
            }
        }
        return null;
    }
}
