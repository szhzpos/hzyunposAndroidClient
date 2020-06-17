package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;


public class TreeListAdapter extends RecyclerView.Adapter<TreeListAdapter.MyViewHolder> {
    private JSONArray mDatas;
    private Context mContext;
    private boolean mSingleSel = true;
    private Drawable mUnfoldDb,mFoldDb;

/*    Item{
        parend_id,p_ref,level,unfold,isSel,item_id,item_name;
    }*/
    private TreeListAdapter(Context context){
            this.mContext = context;
            mUnfoldDb = context.getDrawable(R.drawable.unfold);
            mFoldDb = context.getDrawable(R.drawable.fold);
            mDatas = new JSONArray();
        }

    public TreeListAdapter(Context context,boolean sigle){
        this(context);
        mSingleSel = sigle;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        CheckBox mul_cb;
        RadioButton single_rb;
        TextView item_id,item_name,row_id;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            row_id = itemView.findViewById(R.id.row_id);
            icon = itemView.findViewById(R.id.item_ico);
            mul_cb = itemView.findViewById(R.id.multiple_cb);
            single_rb = itemView.findViewById(R.id.single_rb);
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

            holder.row_id.setText(String.valueOf(position));

            boolean is_unfold = item.getBooleanValue("unfold");
            boolean is_sel = item.getBooleanValue("isSel");

            final ImageView ico = holder.icon;
            if (!Utils.getNullObjectAsEmptyJsonArray(item,"kids").isEmpty()){
                if (ico.getVisibility() == View.GONE)ico.setVisibility(View.VISIBLE);
                ico.setOnClickListener(unfoldIcoListener);
                if (is_unfold){
                    ico.setImageDrawable(mUnfoldDb);
                }else {
                    ico.setImageDrawable(mFoldDb);
                }
            }else {
                if (ico.getVisibility() == View.VISIBLE)ico.setVisibility(View.GONE);
            }

            final CheckBox item_sel_cb =  holder.mul_cb;
            final RadioButton item_single_rb = holder.single_rb;
            if (mSingleSel){
                item_sel_cb.setVisibility(View.GONE);
                item_sel_cb.setOnCheckedChangeListener(null);

                item_single_rb.setVisibility(View.VISIBLE);
                item_single_rb.setOnCheckedChangeListener(null);
                if (is_sel){
                    if (!item_single_rb.isChecked())item_single_rb.setChecked(true);
                }else {
                    if (item_single_rb.isChecked())item_single_rb.setChecked(false);
                }
                item_single_rb.setOnCheckedChangeListener(checkedChangeListener);

            }else{
                item_single_rb.setVisibility(View.GONE);
                item_single_rb.setOnCheckedChangeListener(null);

                item_sel_cb.setVisibility(View.VISIBLE);
                item_sel_cb.setOnCheckedChangeListener(null);
                if (is_sel){
                    if (!item_sel_cb.isChecked())item_sel_cb.setChecked(true);
                }else {
                    if (item_sel_cb.isChecked())item_sel_cb.setChecked(false);
                }
                item_sel_cb.setOnCheckedChangeListener(checkedChangeListener);
            }

            holder.item_id.setText(item.getString("item_id"));
            holder.item_name.setText(item.getString("item_name"));

            holder.mCurrentLayoutItemView.setOnClickListener(itemListener);
            holder.mCurrentLayoutItemView.setPadding( 12 * item.getIntValue("level"),0,0,0);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    public void setDatas(final JSONArray datas,final JSONArray items){
        if (datas != null)mDatas = datas;

        setSelectedItem(items);

        notifyDataSetChanged();
    }
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
        final View v = (View) buttonView.getParent();
        final TextView row_id_tv = v.findViewById(R.id.row_id);
        if (row_id_tv != null){
            int row_id = Integer.valueOf(row_id_tv.getText().toString());
            if (row_id >= 0 && row_id < mDatas.size()){
                final JSONObject object = mDatas.getJSONObject(row_id);
                if (mSingleSel)clearSelected();
                selectItem(object,isChecked);
                buttonView.post(this::notifyDataSetChanged);
            }
        }
    };
    private void clearSelected(){
        if (mDatas != null){
            for (Object o :mDatas){
                if (o instanceof JSONObject){
                    final JSONObject object = (JSONObject)o;
                    if (object.getBooleanValue("isSel"))object.put("isSel",false);
                }
            }
        }
    }
    private void selectItem(final JSONObject object,boolean isChecked){
        if (null != object){
            object.put("isSel",isChecked);
            if (!mSingleSel){
                final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(object,"kids");
                for (int i = 0,size = kids.size();i < size;i++){
                    selectItem(kids.getJSONObject(i),isChecked);
                }
            }
        }
    }

    private View.OnClickListener unfoldIcoListener = (view)->{
        final View parent = (View) view.getParent();
        final TextView row_id_tv = parent.findViewById(R.id.row_id);
        int row_id = Integer.valueOf(row_id_tv.getText().toString());
        if (row_id >= 0 && row_id < mDatas.size()){
            final JSONObject object = mDatas.getJSONObject(row_id);
            boolean unfold = object.getBooleanValue("unfold");
            object.put("unfold",!unfold);
            final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(object,"kids");
            if (!unfold){
                JSONObject item;
                for (int i = 0,size = kids.size();i < size;i++){
                    item = kids.getJSONObject(i);
                    mDatas.add(row_id + i + 1,item);
                }
            }else {
                deleteChildren(kids,row_id + 1);
            }
            row_id_tv.post(this::notifyDataSetChanged);
        }
    };

    private View.OnClickListener itemListener = (v)->{
        CompoundButton compoundButton;
        if (mSingleSel){
            compoundButton = v.findViewById(R.id.single_rb);
        }else {
            compoundButton = v.findViewById(R.id.multiple_cb);
        }
        if (null != compoundButton){
            compoundButton.setChecked(!compoundButton.isChecked());
        }
    };

    private void deleteChildren(final JSONArray kids,int index){
        if (kids != null){
            JSONObject object;
            for (int i = 0,size = kids.size();i < size;i++){
                object = kids.getJSONObject(i);
                if (object.getBooleanValue("unfold")){
                    object.put("unfold",false);
                    deleteChildren(Utils.getNullObjectAsEmptyJsonArray(object,"kids"),index);
                }
                if (index >= 0 && index < mDatas.size())mDatas.remove(index);
            }
        }
    }

    private void setSelectedItem(final JSONArray array){
        if (array != null && !array.isEmpty()){
            for (int i = 0,size = array.size();i < size;i++){
                setSelectedItem(array.getJSONObject(i));
            }
        }
    }
    private  void setSelectedItem(final JSONObject object){
        if (object != null){
            JSONObject item;
            for (int i = 0,size = mDatas.size();i < size;i++){
                item = mDatas.getJSONObject(i);
                if (setSelectedItem(item,object,i + 1)){
                    break;
                }
            }
        }
    }

    private boolean setSelectedItem(final JSONObject item,final JSONObject object,int index){
        if (Utils.getNullStringAsEmpty(object,"item_id").equals(item.getString("item_id"))){
            item.put("isSel",true);
            unfoldParentItem(mDatas,item.getString("parent_id"));
            return true;
        }else {
            final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(item,"kids");
            for (int j = 0,j_size = kids.size();j < j_size;j++){
                if (setSelectedItem(kids.getJSONObject(j),object,index + 1 +j)){
                    return true;
                }
            }
        }
        return false;
    }

    private void unfoldParentItem(final JSONArray array,final String parent_id){
        if (parent_id != null){
            JSONObject item;
            for (int i = 0,size = array.size();i < size;i++){
                item = array.getJSONObject(i);
                final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(item,"kids");
                if (parent_id.equals(item.getString("item_id"))){
                    Logger.d("i:%d,parent_id:%s,item_id:%s",i,parent_id,item.getString("item_id"));
                    if (!item.getBooleanValue("unfold")){

                        unfoldParentItem(array,item.getString("parent_id"));

                        item.put("unfold",true);
                        for (int k = 0,length = kids.size();k < length;k++){
                            array.add(i + 1 + k,kids.getJSONObject(k));
                        }

                    }
                }else {
                    for (int j = 0,j_size = kids.size();j < j_size;j++){
                        unfoldParentItem(kids,parent_id);
                    }
                }
            }
        }
    }

    public JSONArray getMultipleSelectedContent(){
        final JSONArray objects = new JSONArray();
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (object.getBooleanValue("isSel")){
                object.remove("kids");
                objects.add(object);
            }
        }
        return objects;
    }

    public JSONObject getSingleSelectedContent(){
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (object.getBooleanValue("isSel")){
                object.remove("kids");
                return object;
            }
        }
        return new JSONObject();
    }

}
