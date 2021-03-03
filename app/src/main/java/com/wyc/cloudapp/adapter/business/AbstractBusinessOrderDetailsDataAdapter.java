package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import java.time.chrono.MinguoDate;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: AbstractBusinessOrderDetailsDataAdapter
 * @Description: 业务单据商品明细适配器
 * @Author: wyc
 * @CreateDate: 2021/3/2 17:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/2 17:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class AbstractBusinessOrderDetailsDataAdapter<T extends AbstractTableDataAdapter.SuperViewHolder > extends AbstractDataAdapter<T> {
    private View mCurrentItemView;
    private int mCurrentItemIndex = -1;
    private OnItemSelectListener mSelectListener;
    public AbstractBusinessOrderDetailsDataAdapter(MainActivity activity) {
        super(activity);
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        holder.itemView.setTag(position);
        if (mCurrentItemIndex == position){
            setSelectStatus(holder.itemView);
        }
        holder.itemView.setOnClickListener(mListener);
    }

    private final View.OnClickListener mListener = v -> {
        mCurrentItemIndex = Utils.getViewTagValue(v,-1);
        setSelectStatus(v);
        final JSONObject object = item();
        if (null != object && mSelectListener != null)mSelectListener.onSelect(object);
    };

    private JSONObject item(){
        return (mDatas == null || mCurrentItemIndex == -1 || mCurrentItemIndex >= mDatas.size()) ? null : mDatas.getJSONObject(mCurrentItemIndex);
    }

    public interface OnItemSelectListener{
        void onSelect(JSONObject item);
    }

    public void setItemListener(OnItemSelectListener listener){
        mSelectListener = listener;
    }

    protected void setSelectStatus(View v){
        TextView goods_name;
        if(null != mCurrentItemView){
            goods_name = mCurrentItemView.findViewById(R.id.name_tv);
            goods_name.clearAnimation();
            goods_name.setTextColor(mContext.getColor(R.color.good_name_color));
        }
        goods_name = v.findViewById(R.id.name_tv);
        goods_name.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        goods_name.setTextColor(mContext.getColor(R.color.blue));

        mCurrentItemView = v;
    }

    protected String[] getCumulativeKey(){
        return new String[]{"xnum"};
    }

    public void addDetails(@Nullable final JSONObject object,int index,boolean modify){ //index >=0 需要把第一个参数累加到index位置
        if (object != null){
            if (mDatas == null)mDatas = new JSONArray();

            if (index >= 0){
                final JSONObject o = (JSONObject) mDatas.remove(index);
                if (modify){
                    mDatas.add(index,object);
                }else {
                    final String[] keys = getCumulativeKey();
                    for (String key : keys){
                        o.put(key,o.getDoubleValue(key) + object.getDoubleValue(key));
                    }
                    mDatas.add(index,o);
                }
            }else{
                mDatas.add(object);
                index = mDatas.size() - 1;
            }
            mCurrentItemIndex = index;
            notifyDataSetChanged();
        }
    }
    public int isExist(final JSONObject object){
        int size = 0;
        if (null == mDatas || (size = mDatas.size()) == 0 || null == object)return -1;
        for (int i = 0;i < size;i ++){
            final JSONObject obj = mDatas.getJSONObject(i);
            final String key = "barcode_id";
            if (Utils.getNullStringAsEmpty(object,key).equals(obj.getString(key))){//注：如果加入了组合商品需要把gp_id加入判断
                return i;
            }
        }
        return -1;
    }

    public abstract JSONObject updateGoodsDetails(final JSONObject object);
}
