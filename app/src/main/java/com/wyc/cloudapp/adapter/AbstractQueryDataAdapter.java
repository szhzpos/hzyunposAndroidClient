package com.wyc.cloudapp.adapter;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.orderDialog.RetailOrderDetailsDialog;
import com.wyc.cloudapp.utils.Utils;

public abstract class AbstractQueryDataAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    protected MainActivity mContext;
    protected JSONArray mDatas;
    protected View mCurrentItemView;

    public abstract void setDatas(final String sql);

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null) {
            View child;
            int selected_color, item_color,text_color;
            if (s) {
                selected_color = mContext.getColor(R.color.listSelected);
                item_color = Color.YELLOW;
                text_color = mContext.getColor(R.color.white);
            }else {
                selected_color = mContext.getColor(R.color.white);
                item_color = mContext.getColor(R.color.appColor);
                text_color = mContext.getColor(R.color.text_color);
            }
            view.setForeground(mContext.getDrawable(selected_color));
            if (view instanceof LinearLayout){
                LinearLayout linearLayout = (LinearLayout)view;
                int count = linearLayout.getChildCount();
                for (int i = 0;i < count;i++){
                    child = linearLayout.getChildAt(i);
                    if (child instanceof TextView){
                        final TextView tv = ((TextView) child);
                        switch (tv.getId()) {
                            case R.id.order_code:
                            case R.id.sale_refund:
                            case R.id.retail_order_code:
                            case R.id.refund_order_code:
                            case R.id.pay_m_name:
                                tv.setTextColor(item_color);
                                break;
                            case R.id.order_status:
                            case R.id.refund_status:
                                int status = Utils.getViewTagValue(child,2);
                                if (status == 1 || status == 4){
                                    tv.setTextColor(mContext.getColor(R.color.orange_1));
                                }else{
                                    tv.setTextColor(text_color);
                                }
                                break;
                            default:
                                tv.setTextColor(text_color);
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    protected void setCurrentItemView(View v){
        if (mCurrentItemView == null){
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }else if(mCurrentItemView != v){
            setViewBackgroundColor(mCurrentItemView,false);
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }
    }

    protected JSONObject getCurrentOrder(){
        if (null != mCurrentItemView){
            final TextView order_code_tv = mCurrentItemView.findViewById(R.id.order_code);
            if (null != order_code_tv){
                final String sz_order_code = order_code_tv.getText().toString();
                for (int i = 0,size = mDatas.size();i < size;i ++){
                    final JSONObject object = mDatas.getJSONObject(i);
                    if (object != null && sz_order_code.equals(object.getString("order_code"))){
                        return object;
                    }
                }
            }
        }
        return new JSONObject();
    }


    protected boolean isClickView(final View view,float x,float y){
        if (view == null)return false;
        float v_x = view.getX(),v_y = view.getY();
        return x >= v_x && x <= v_x + view.getWidth() && y >= v_y && y <= v_y + view.getHeight();
    }

}
