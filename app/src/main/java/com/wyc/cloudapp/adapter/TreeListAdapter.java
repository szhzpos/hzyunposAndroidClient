package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TreeListAdapter extends RecyclerView.Adapter<TreeListAdapter.MyViewHolder> {
    private JSONArray mDatas;
    private Context mContext;
    public TreeListAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        CheckBox selected_status;
        TextView item_id,item_name;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
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
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_50)));
        return new TreeListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject item = mDatas.optJSONObject(position);
        if (item != null){
            //holder.icon = itemView.findViewById(R.id.item_ico);
            holder.selected_status.setChecked(false);
            holder.item_id.setText(item.optString("item_id"));
            holder.item_name.setText(item.optString("item_name"));
            holder.mCurrentLayoutItemView.setOnClickListener(itemListener);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.length();
    }

    public void setDatas(JSONArray datas){
        mDatas = datas;
        notifyDataSetChanged();
    }

    private View.OnClickListener itemListener = (view)->{
        ImageView imageView = view.findViewById(R.id.item_ico);
        if (imageView != null){
            imageView.setImageDrawable(mContext.getDrawable(R.drawable.minus));
            TextView id = view.findViewById(R.id.item_id);
            StringBuilder err = new StringBuilder();
            JSONArray array = SQLiteHelper.getListToJson("select category_id item_id,name item_name from shop_category where parent_id=" + id.getText(),err);
            Utils.moveJsonArray(array,mDatas);
            notifyDataSetChanged();
        }
    };

}
