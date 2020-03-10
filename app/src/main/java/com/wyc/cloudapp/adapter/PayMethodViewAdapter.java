package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class PayMethodViewAdapter extends RecyclerView.Adapter<PayMethodViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;//当前选择的类别item
    public PayMethodViewAdapter(Context context){
        this.mContext = context;
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView pay_method_id,pay_method_name;
        MyViewHolder(View itemView) {
            super(itemView);
            pay_method_id = itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.pay_method_content, null);
        itemView.setLayoutParams( new RecyclerView.LayoutParams((int)mContext.getResources().getDimension(R.dimen.width_88), ViewGroup.LayoutParams.MATCH_PARENT));
        itemView.setOnClickListener(view -> mCurrentItemView = view);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject pay_method_info = mDatas.optJSONObject(i);
            String szImage;
            Drawable drawable = null;
            if (pay_method_info != null){
                szImage = pay_method_info.optString("pay_img");
                if (!"".equals(szImage)){
                    szImage = szImage.substring(szImage.lastIndexOf("/") + 1);
                    Logger.d("图片文件：%s",szImage);
                    drawable = Drawable.createFromPath(SQLiteHelper.IMG_PATH + szImage);
                }else{
                    drawable = mContext.getDrawable(R.drawable.default_pay);
                }
                if (drawable != null){
                    drawable.setBounds(0, 0, 32,32);
                    myViewHolder.pay_method_name.setCompoundDrawables(null,drawable,null,null);
                }
                myViewHolder.pay_method_id.setText(pay_method_info.optString("pay_method_id"));
                myViewHolder.pay_method_name.setText(pay_method_info.optString("name"));
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
        mDatas = SQLiteHelper.getList("select pay_method_id,name,pay_img  from pay_method where status = '1' order by sort",0,0,false,err);
        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
        }
    }

}
