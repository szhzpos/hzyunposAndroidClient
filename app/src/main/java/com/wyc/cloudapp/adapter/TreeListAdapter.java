package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;


public class TreeListAdapter extends RecyclerView.Adapter<TreeListAdapter.MyViewHolder> {
    private JSONArray mDatas;
    private Context mContext;
    private boolean sigleSel = true;
    private Drawable mUnfoldDb,mFoldDb;

/*    Item{
        parend_id,level,unfold,isSel,item_id,item_name;
    }*/
    public TreeListAdapter(Context context){
        this.mContext = context;
        mUnfoldDb = context.getDrawable(R.drawable.minus);
        mFoldDb = context.getDrawable(R.drawable.plus);
        mDatas = new JSONArray();
    }

    public TreeListAdapter(Context context,boolean sigle){
        this(context);
        sigleSel = sigle;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        CheckBox selected_status;
        TextView item_id,item_name,row_id;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            row_id = itemView.findViewById(R.id.row_id);
            icon = itemView.findViewById(R.id.item_ico);
            selected_status = itemView.findViewById(R.id.item_checked);
            item_id = itemView.findViewById(R.id.item_id);
            item_name = itemView.findViewById(R.id.item_name);
        }
    }
    @NonNull
    @Override
    public TreeListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.tree_item_layout, null);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_50)));
        return new TreeListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject item = mDatas.getJSONObject(position);
        if (item != null){

            holder.row_id.setText(String.valueOf(position));;

            boolean is_unfold = item.getBooleanValue("unfold");
            boolean is_sel = item.getBooleanValue("isSel");

            setMargins(holder.icon,item.getIntValue("level"));
            if (is_unfold){
                holder.icon.setImageDrawable(mUnfoldDb);
            }else {
                holder.icon.setImageDrawable(mFoldDb);
            }
            if (sigleSel)
                holder.selected_status.setVisibility(View.GONE);
            else{
                holder.selected_status.setOnCheckedChangeListener(checkedChangeListener);
                if (is_sel){
                    if (!holder.selected_status.isChecked())holder.selected_status.setChecked(true);
                }else {
                    holder.selected_status.setChecked(false);
                }
            }

            holder.item_id.setText(item.getString("item_id"));
            holder.item_name.setText(item.getString("item_name"));
            holder.mCurrentLayoutItemView.setOnClickListener(itemListener);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private void setMargins(final View view,int num){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins( 12 * num,0,0,0);
        view.setLayoutParams(params);
    }

    public void setDatas(final JSONArray datas){
        mDatas = datas;
        notifyDataSetChanged();
    }
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
        final View v = (View) buttonView.getParent();
        final TextView row_id_tv = v.findViewById(R.id.row_id);
        if (row_id_tv != null){
            int row_id = Integer.valueOf(row_id_tv.getText().toString());
            if (row_id >= 0 && row_id < mDatas.size()){
                final JSONObject object = mDatas.getJSONObject(row_id);
                object.put("isSel",isChecked);
                final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(object,"kids");
                JSONObject kid_json;
                for (int i = 0,size = kids.size();i < size;i++){
                    kid_json = kids.getJSONObject(i);
                    kid_json.put("isSel",isChecked);
                }
            }
        }
    };

    private View.OnClickListener itemListener = (view)->{
        final TextView row_id_tv = view.findViewById(R.id.row_id);
        int row_id = Integer.valueOf(row_id_tv.getText().toString());
        if (row_id >= 0 && row_id < mDatas.size()){
            final JSONObject object = mDatas.getJSONObject(row_id);
            boolean unfold = object.getBooleanValue("unfold");
            object.put("unfold",!unfold);

            final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(object,"kids");
            if (!unfold){
                for (int i = 0,size = kids.size();i < size;i++){
                    mDatas.add(row_id + i + 1,kids.getJSONObject(i));
                }
            }else {
                for (int i = 0,size = kids.size();i < size;i++){
                    mDatas.remove(row_id + 1);
                }
            }
            notifyDataSetChanged();
        }
    };

}
