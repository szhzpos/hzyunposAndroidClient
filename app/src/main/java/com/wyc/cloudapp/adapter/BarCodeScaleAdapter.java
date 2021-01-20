package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class BarCodeScaleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    private static final int HEADER = -1;
    private static final int CONTENT = -2;
    private Context mContext;
    private JSONArray mDatas;
    private Map<String,TextView> mCurrentItemIndexMap;
    public BarCodeScaleAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
        mCurrentItemIndexMap = new HashMap<>();
    }

    @Override
    public void onClick(View v) {
        setSelectStatus(v);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final View v = (View) buttonView.getParent();
        final TextView _id = v.findViewById(R.id._id),down_status = v.findViewById(R.id.down_status);
        if (_id != null && null != down_status){
            final String sz_id = _id.getText().toString();
            if (isChecked){
                mCurrentItemIndexMap.put(sz_id,down_status);
            }else {
                mCurrentItemIndexMap.remove(sz_id);
            }
        }
    }

    static class ContentHolder extends RecyclerView.ViewHolder {
        private final TextView _id,row_id,product_type,scale_ip,scale_port,g_c_name,g_c_id,down_status,scale_rmk;
        private final CheckBox s_checked;
        ContentHolder(View itemView) {
            super(itemView);
            _id = itemView.findViewById(R.id._id);
            row_id = itemView.findViewById(R.id.row_id);
            s_checked =  itemView.findViewById(R.id.s_checked);
            product_type =  itemView.findViewById(R.id.product_type);
            scale_ip =  itemView.findViewById(R.id.scale_ip);
            scale_port =  itemView.findViewById(R.id.scale_port);
            g_c_name =  itemView.findViewById(R.id.g_c_name);
            g_c_id =  itemView.findViewById(R.id.g_c_id);
            down_status =  itemView.findViewById(R.id.down_status);
            scale_rmk =  itemView.findViewById(R.id.scale_rm);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.barcode_scale_detail_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new ContentHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder myViewHolder, int i) {
        final JSONObject content = mDatas.getJSONObject(i);
        if (content != null){
            ContentHolder contentHolder = (ContentHolder)myViewHolder;
            contentHolder.row_id.setText(String.valueOf(i + 1));
            contentHolder._id.setText(content.getString("_id"));
            contentHolder.product_type.setText(content.getString("s_product_t"));
            contentHolder.scale_ip.setText(content.getString("scale_ip"));
            contentHolder.scale_port.setText(content.getString("scale_port"));
            contentHolder.g_c_name.setText(content.getString("g_c_name"));
            contentHolder.g_c_id.setText(content.getString("g_c_id"));
            // contentHolder.down_status.setText("");
            contentHolder.scale_rmk.setText(content.getString("remark"));

            contentHolder.s_checked.setChecked(false);
            contentHolder.s_checked.setOnCheckedChangeListener(this);
            contentHolder.itemView.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 1 : mDatas.size();
    }

    @Override
    public int getItemViewType(int position){
        return CONTENT;
    }

    public JSONArray getCurrentScalseInfos() {
        final JSONArray scalses = new JSONArray();
        JSONObject object;
        for (String t_id : mCurrentItemIndexMap.keySet()){
            for (int j = 0,length = mDatas.size();j < length;j++){
                object = mDatas.getJSONObject(j);
                if (object != null && t_id.equals(object.getString("_id"))){
                    scalses.add(object);
                    break;
                }
            }
        }
        return scalses;
    }


    public TextView getTextStatus(final String id){
        return mCurrentItemIndexMap.get(id);
    }

    public Map<String,TextView> getCurrentItemIndexMap(){
        return mCurrentItemIndexMap;
    }

    public @NonNull JSONArray getDatas(){
        return mDatas;
    }
    public void setDatas(){
        StringBuilder err = new StringBuilder();
        mDatas = SQLiteHelper.getListToJson("SELECT _id,s_manufacturer,s_class_id,s_product_t,scale_ip,scale_port,g_c_id,g_c_name,remark FROM barcode_scalse_info",err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载称信息错误：" + err,mContext,null);
        }
    }

    public void deleteScale(){
        JSONObject object;
        Logger.d("mCurrentItemIndexMap:%s", mCurrentItemIndexMap);
        Logger.d("mDatas:%s",mDatas);
        for (final String t_id : mCurrentItemIndexMap.keySet()){
            for (int i = 0,size = mDatas.size();i < size;i++){
                object = mDatas.getJSONObject(i);
                if (object != null){
                    if (object.getString("_id").equals(t_id)){
                        StringBuilder err = new StringBuilder();
                        if (SQLiteHelper.execDelete("barcode_scalse_info","_id=?",new String[]{t_id},err) >= 0){
                            mDatas.remove(i);
                            mCurrentItemIndexMap.remove(t_id);
                        }else{
                            MyDialog.ToastMessage("删除称信息错误：" + err,mContext,null);
                        }
                        break;
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addScale(@NonNull JSONObject scale){
        StringBuilder err = new StringBuilder();
        int svae_type = 0;
        JSONObject object;

        if (scale.containsKey("_id")){
            svae_type = 1;
            for (int i = 0,size = mDatas.size();i < size;i++){
                object = mDatas.getJSONObject(i);
                if (object != null){
                    if (object.getString("_id").equals(scale.getString("_id"))){
                        mDatas.remove(i);
                        notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
        if (SQLiteHelper.saveFormJson(scale,"barcode_scalse_info",null,svae_type,err)){
            setDatas();
        }else{
            MyDialog.ToastMessage("保存条码秤错误：" + err,mContext,null);
        }
    }

    private void setSelectStatus(View v){
        CheckBox checkBox = v.findViewById(R.id.s_checked);
        if (checkBox != null){
            if (checkBox.isChecked()){
                checkBox.setChecked(false);
            }else{
                checkBox.setChecked(true);
            }
        }
    }
}
