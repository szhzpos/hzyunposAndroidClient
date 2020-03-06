package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoodsTypeViewAdapter extends RecyclerView.Adapter<GoodsTypeViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private View mPreView;
    private GoodsInfoViewAdapter mGoodsInfoAdapter;
    public GoodsTypeViewAdapter(Context context,GoodsInfoViewAdapter adapter){
        this.mContext = context;
        this.mGoodsInfoAdapter = adapter;
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView category_id,category_name;
        View mCurrentItem;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentItem = itemView;

            category_id = itemView.findViewById(R.id.category_id);
            category_name =  itemView.findViewById(R.id.category_name);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.goods_type_info_content, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(88,
                ViewGroup.LayoutParams.MATCH_PARENT);
        itemView.setLayoutParams(lp);

        itemView.setOnClickListener(view -> {
            TextView name,category_id;
            if (null != mPreView){
                mPreView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
                name = mPreView.findViewById(R.id.category_name);
                name.setTextColor(mContext.getResources().getColor(R.color.blue,null));
            }
            view.setBackgroundColor(mContext.getResources().getColor(R.color.blue,null));
            name = view.findViewById(R.id.category_name);
            name.setTextColor(mContext.getResources().getColor(R.color.white,null));

            mPreView = view;
            category_id = view.findViewById(R.id.category_id);
            if (null != mGoodsInfoAdapter)mGoodsInfoAdapter.setDatas(category_id.getText().toString());
        });
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject goods_type_info = mDatas.optJSONObject(i);
            if (goods_type_info != null){
                myViewHolder.category_id.setText(goods_type_info.optString("category_id"));
                myViewHolder.category_name.setText(goods_type_info.optString("name"));

                if (i == 0){//触发第一个类别查询
                    myViewHolder.mCurrentItem.callOnClick();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.length();
    }

    public JSONObject getItem(int i){
       return mDatas == null ? null : mDatas.optJSONObject(i);
    }

    public void setDatas(){
        StringBuilder err = new StringBuilder();
        mDatas = SQLiteHelper.getList("select category_id,name from shop_category where parent_id='0'",0,0,false,err);
        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
        }
    }

}
