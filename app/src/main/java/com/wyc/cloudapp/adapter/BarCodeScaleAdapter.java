package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BarCodeScaleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER = -1;
    private static final int CONTENT = -2;
    private Context mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDoubleClickListener mOnItemDoubleClickListener;
    public BarCodeScaleAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }
    static class ContentHolder extends RecyclerView.ViewHolder {
        private TextView row_id,product_type,scale_ip,scale_port,goods_category,down_status,scale_rmk;
        private CheckBox s_checked;
        private View mCurrentLayoutItemView;//当前布局的item
        ContentHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            row_id = itemView.findViewById(R.id.row_id);
            s_checked =  itemView.findViewById(R.id.s_checked);
            product_type =  itemView.findViewById(R.id.product_type);
            scale_ip =  itemView.findViewById(R.id.scale_ip);
            scale_port =  itemView.findViewById(R.id.scale_port);
            goods_category =  itemView.findViewById(R.id.goods_category);
            down_status =  itemView.findViewById(R.id.down_status);
            scale_rmk =  itemView.findViewById(R.id.scale_rm);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView;
        RecyclerView.ViewHolder holder;
        if (i == HEADER){
            itemView = View.inflate(mContext, R.layout.barcode_scale_header_layout, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.table_row_height)));
            holder = new HeaderHolder(itemView);
        }else{
            itemView = View.inflate(mContext, R.layout.barcode_scale_detail_layout, null);
            holder = new ContentHolder(itemView);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder myViewHolder, int i) {
        if (myViewHolder instanceof ContentHolder){
            JSONObject content = mDatas.optJSONObject(i - 1);
            if (content != null){
                ContentHolder contentHolder = (ContentHolder)myViewHolder;
                contentHolder.row_id.setText(String.valueOf(i));

                contentHolder.s_checked.setChecked(false);
                contentHolder.product_type.setText(content.optString("product_type"));
                contentHolder.scale_ip.setText(content.optString("scale_ip"));
                contentHolder.scale_port.setText(content.optString("scale_port"));
                contentHolder.goods_category.setText(content.optString("goods_category"));
                contentHolder.down_status.setText("");
                contentHolder.scale_rmk.setText(content.optString("remark"));

                contentHolder.mCurrentLayoutItemView.setOnTouchListener(new ClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    if (mOnItemDoubleClickListener != null){
                        mOnItemDoubleClickListener.onClick(v,i);
                    }
                }, v -> {
                    setSelectStatus(v);
                    if (mOnItemClickListener != null){
                        mOnItemClickListener.onClick(v,i); }
                }));

                if (mCurrentItemIndex == i){
                    setSelectStatus(contentHolder.mCurrentLayoutItemView);
                    mCurrentItemView = contentHolder.mCurrentLayoutItemView;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 1 : mDatas.length() + 1;
    }

    @Override
    public int getItemViewType(int position){
        if (0 == position){
            return HEADER;
        }
        return CONTENT;
    }

    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }
    public interface OnItemDoubleClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
    public void setOnItemDoubleClickListener(OnItemDoubleClickListener onItemDoubleClickListener){
        this.mOnItemDoubleClickListener = onItemDoubleClickListener;
    }
    public JSONObject getCurrentContent() {
        return mDatas.optJSONObject(mCurrentItemIndex);
    }

    private void setCurrentItemIndexAndItemView(View v){
        TextView tv_id;
        mCurrentItemView = v;
        if (null != mCurrentItemView && (tv_id = mCurrentItemView.findViewById(R.id.pay_method_id)) != null){
            String id = tv_id.getText().toString();
            if (mDatas != null ){
                for (int i = 0,length = mDatas.length();i < length;i ++){
                    JSONObject json = mDatas.optJSONObject(i);
                    if (id.equals(json.optString("pay_method_id"))){
                        mCurrentItemIndex = i;
                        return;
                    }
                }
            }
        }
        mCurrentItemIndex = -1;
    }

    public int getCurrentItemIndex(){
        return mCurrentItemIndex;
    }

    public @NonNull JSONArray getDatas(){
        return mDatas;
    }

    private void deleteScalse(int index){
        Logger.d("index:%d",index);
        if (0 <= index && index < mDatas.length()){
            mDatas.remove(index);
            if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                mCurrentItemIndex = -1;
                mCurrentItemView = null;
            }
            notifyDataSetChanged();
        }
    }

    private void setSelectStatus(View v){
        if(null != mCurrentItemView){
            mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
        }
        v.setBackgroundColor(mContext.getResources().getColor(R.color.pink,null));
        setCurrentItemIndexAndItemView(v);
    }

    public void clearPayDetail(){
        Utils.ClearJsons(mDatas);
        notifyDataSetChanged();
    }
}
