package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    protected int mCurrentItemIndex = -1;
    private ItemClickCallBack mItemClickCallback;
    protected void setCurrentItemView(View v){
        if (mCurrentItemView == null){
            setmCurrentItemViewAndIndex(v);
            setViewBackgroundColor(v,true);
        }else if(mCurrentItemView != v){
            setViewBackgroundColor(mCurrentItemView,false);
            setmCurrentItemViewAndIndex(v);
            setViewBackgroundColor(v,true);
        }else {
            setViewBackgroundColor(v,false);
            setmCurrentItemViewAndIndex(null);
        }
        if (mItemClickCallback != null)mItemClickCallback.onClick(getCurrentRecord());
    }
    protected void setViewBackgroundColor(final View view,boolean s){
        if(view!= null){
            int text_color,selected_color;
            if (s){
                selected_color = mContext.getColor(R.color.listSelected);
                text_color = mContext.getColor(R.color.white);
            } else {
                if (isNormalStatus(view))
                    text_color = mContext.getColor(R.color.text_color);
                else
                    text_color = mContext.getColor(R.color.orange_1);

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

    private void setmCurrentItemViewAndIndex(final View v){
        mCurrentItemView = v;
        if (v == null){
            mCurrentItemIndex = -1;
        }else{
            final TextView tv = v.findViewById(R.id.row_id);
            if (tv == null){
                mCurrentItemIndex = -1;
            }else{
                try {
                    mCurrentItemIndex = Integer.valueOf(tv.getText().toString());
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    mCurrentItemIndex = -1;
                }
            }
        }
    }

    private boolean isNormalStatus(final @NonNull View view){
        //判断数据行是否正常。数据行所在的view需存在id为_status的子view，将数据行的状态值存放在子veiw的tag中；
        return Utils.getViewTagValue(view.findViewById(R.id._status),0) == 1;
    }

    protected void setRowStatus(View view,int res_id){
        if(view!= null){
            int text_color;
            text_color = mContext.getColor(res_id);
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

    protected JSONObject getCurrentRecord(){
        return null;
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    public interface ItemClickCallBack{
        void onClick(final JSONObject record);
    }
    public void setItemClickListener(final ItemClickCallBack clickCallBack){
        mItemClickCallback = clickCallBack;
    }
}
