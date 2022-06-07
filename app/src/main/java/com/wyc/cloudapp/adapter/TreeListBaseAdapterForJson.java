package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.utils.Utils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: TreeListBaseAdapter
 * @Description: 树形结构数据适配器
 * @Author: wyc
 * @CreateDate: 2021/3/8 15:39
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/8 15:39
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */



public abstract class TreeListBaseAdapterForJson<T extends TreeListBaseAdapter.MyViewHolder> extends TreeListBaseAdapter<JSONArray,JSONObject,T> {

    private final Context mContext;
    private boolean mSingleSel = true;
    private final Drawable mUnfoldDb,mFoldDb;
    private OnItemClick mItemClick;//不为null的时候会隐藏单选、多选按钮，改变mCurrentItemView的背景色来表示选中；此种模式下只支持选中单个项目并触发事件。
    private View mCurrentItemView;
    /*    Item{
            p_ref,level,unfold,isSel,item_id,item_name,kids; <p_ref , kids>存在上下级时必须存在
        }*/
    private TreeListBaseAdapterForJson(Context context){
        this.mContext = context;
        mUnfoldDb = context.getDrawable(R.drawable.unfold);
        mFoldDb = context.getDrawable(R.drawable.fold);
        mData = new JSONArray();
    }

    public TreeListBaseAdapterForJson(Context context, boolean single){
        this(context);
        mSingleSel = single;
    }


    protected final View getView(){
        View itemView = View.inflate(mContext, R.layout.tree_item_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_50)));
        return itemView;
    }

    abstract void bindContent(@NonNull T holder,final JSONObject object);
    protected int clickItemIndex(){
        return 0;
    }

    @Override
    public final void onBindViewHolder(@NonNull T holder, int position) {
        final int old_pos = position;
        final JSONObject item = mData.getJSONObject(position);
        if (item != null){

            holder.row_id.setText(String.valueOf(position));

            boolean is_unfold = item.getBooleanValue("unfold");
            boolean is_sel = item.getBooleanValue("isSel");

            final ImageView ico = holder.icon;
            if ( (position += 1) == mData.size())position -= 1;
            if (is_unfold || mData.getJSONObject(position).getJSONObject("p_ref") == item || !Utils.getNullObjectAsEmptyJsonArray(item,"kids").isEmpty()){
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
            if (mItemClick  == null){
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
            }else{
                if (is_sel)setViewBackgroundColor(mCurrentItemView = holder.itemView,true);
            }
            holder.item_id.setText(item.getString(COL_ID));


            holder.itemView.setPadding( 25 * item.getIntValue("level"),0,0,0);
            holder.itemView.setOnClickListener(itemListener);
            holder.itemView.setTag(item);

            bindContent(holder,item);

            if (old_pos == clickItemIndex()){
                holder.itemView.callOnClick();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    @Override
    public void onViewRecycled (@NonNull MyViewHolder holder){
        if (mItemClick != null && mCurrentItemView == holder.itemView)setViewBackgroundColor(mCurrentItemView,false);
    }

    @Override
    public void selectAll(boolean s) {
        if (!mSingleSel && mData != null){
            for (int i = 0,size = mData.size();i < size;i ++){
                mData.getJSONObject(i).put("isSel",s);
            }
            notifyItemRangeChanged(0,mData.size());
        }
    }

    @Override
    public TreeListBaseAdapterForJson<T> setData(final JSONArray data, final JSONArray items){
        if (data != null) mData = data;

        setSelectedItem(items);

        if (Looper.myLooper() != Looper.getMainLooper()){
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }else
            notifyDataSetChanged();

        return this;
    }
    @Override
    public void setData(final JSONArray data,boolean singleSel){
        mSingleSel = singleSel;
        setData(data,null);
    }


    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
        final View v = (View) buttonView.getParent();
        final TextView row_id_tv = v.findViewById(R.id.row_id);
        if (row_id_tv != null){
            int row_id = Integer.parseInt(row_id_tv.getText().toString());
            if (row_id >= 0 && row_id < mData.size()){
                final JSONObject object = Utils.getViewTagValue(v);
                if (mSingleSel){
                    clearSelected();
                    object.put("isSel",isChecked);
                }else
                    selectItem(object,row_id + 1,isChecked);
            }
        }
        CustomApplication.runInMainThread(this::notifyDataSetChanged);
    };

    private void clearSelected(){
        if (mData != null){
            for (Object o : mData){
                final JSONObject object = (JSONObject)o;
                if (object.getBooleanValue("isSel"))object.put("isSel",false);
            }
        }
    }
    private void selectItem(final JSONObject object,int index,boolean isChecked){
        if (null != object){
            object.put("isSel",isChecked);
            JSONObject child;
            for (int j = index, size = mData.size(); 0 <= j && j < size; j++){
                child = mData.getJSONObject(j);
                if (child.getJSONObject("p_ref") == object){
                    if (child.getBooleanValue("unfold")){
                        selectItem(child,j += 1,isChecked);
                    }
                    child.put("isSel",isChecked);
                }else {
                    break;
                }
            }
        }
    }

    private final View.OnClickListener unfoldIcoListener = (view)->{
        final View parent = (View) view.getParent();
        final TextView row_id_tv = parent.findViewById(R.id.row_id);
        int row_id = Integer.parseInt(row_id_tv.getText().toString());
        if (row_id >= 0 && row_id < mData.size()){
            final JSONObject object = mData.getJSONObject(row_id);
            boolean unfold = object.getBooleanValue("unfold"),is_sel = object.getBooleanValue("isSel");
            object.put("unfold",!unfold);
            if (!unfold){
                final JSONArray kids = (JSONArray) object.remove("kids");
                if (kids != null){
                    boolean single = mSingleSel;
                    JSONObject item;
                    for (int i = 0,size = kids.size();i < size;i++){
                        item = kids.getJSONObject(i);
                        if (!single && !item.getBooleanValue("isSel"))item.put("isSel",is_sel);
                        mData.add(row_id + i + 1,item);
                    }
                }
            }else {
                foldChildren(object,row_id + 1);
            }
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }
    };
    private void foldChildren(final JSONObject parent, int index){
        if (parent != null){
            final JSONArray kids = new JSONArray();
            JSONObject child;
            for (int j = index; 0 <= j && j < mData.size(); j++){
                child = mData.getJSONObject(index);
                if (child.getJSONObject("p_ref") == parent){
                    if (child.getBooleanValue("unfold")){
                        child.put("unfold",false);
                        foldChildren(child,index + 1);
                    }
                    kids.add(mData.remove(index));
                    j--;
                }else {
                    parent.put("kids",kids);
                    break;
                }
            }
        }
    }

    private final View.OnClickListener itemListener = (v)->{
        if (mItemClick != null){
            JSONObject jsonObject;
            if (mCurrentItemView != v){
                jsonObject = Utils.getViewTagValue(mCurrentItemView);
                if (!jsonObject.isEmpty())jsonObject.put("isSel",false);

                setViewBackgroundColor(mCurrentItemView,false);
                mCurrentItemView = v;
                setViewBackgroundColor(v,true);
            }
            jsonObject = Utils.getViewTagValue(v);
            if (!jsonObject.isEmpty()){
                jsonObject.put("isSel",true);
                mItemClick.OnClick(jsonObject);
            }
        }else{
            final CompoundButton compoundButton;
            if (mSingleSel){
                compoundButton = v.findViewById(R.id.single_rb);
            }else {
                compoundButton = v.findViewById(R.id.multiple_cb);
            }
            if (null != compoundButton){
                compoundButton.setChecked(!compoundButton.isChecked());
            }
        }
    };

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null){
            int text_color,selected_color;
            if (s){
                selected_color = mContext.getColor(R.color.listSelected);
                text_color = mContext.getColor(R.color.white);
            } else {
                text_color = mContext.getColor(R.color.text_color);
                selected_color = mContext.getColor(R.color.white);
            }
            view.setBackgroundColor(selected_color);
            if (view instanceof LinearLayout){
                final LinearLayout linearLayout = (LinearLayout)view;
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

    private JSONObject getCurrentItem(final @NonNull View view){
        final TextView row_id_tv = view.findViewById(R.id.row_id);
        if (row_id_tv != null){
            int row_id = Integer.parseInt(row_id_tv.getText().toString());
            if (row_id >= 0 && row_id < mData.size()){
                final JSONObject obj = mData.getJSONObject(row_id);
                obj.put("isSel",true);

                final JSONObject object = Utils.JsondeepCopy(obj);
                object.remove("kids");
                object.remove("p_ref");
                return object;
            }
        }
        return new JSONObject();
    }

    public interface OnItemClick{
        void OnClick(final JSONObject object);
    }
    public void setItemListener(OnItemClick click){
        mItemClick = click;
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
            for (int i = 0, size = mData.size(); i < size; i++){
                item = mData.getJSONObject(i);
                if (setSelectedItem(item,object,i)){
                    break;
                }
            }
        }
    }

    private boolean setSelectedItem(final JSONObject item,final JSONObject object,int first_index){
        if (Utils.getNullStringAsEmpty(object,COL_ID).equals(item.getString(COL_ID))){
            item.put("isSel",true);
            unfoldParentItem(item,first_index);
            return true;
        }else {
            final JSONArray kids = Utils.getNullObjectAsEmptyJsonArray(item,"kids");
            for (int j = 0,j_size = kids.size();j < j_size;j++){
                if (setSelectedItem(kids.getJSONObject(j),object,first_index)){
                    return true;
                }
            }
        }
        return false;
    }

    private void unfoldParentItem(final JSONObject item ,int index){
        if (item != null && item.getJSONObject("p_ref") != null){//顶层item不需要展开
            final JSONObject top_item = mData.getJSONObject(index);
            boolean unfold = top_item.getBooleanValue("unfold"),is_sel = top_item.getBooleanValue("isSel");
            if (!unfold){
                top_item.put("unfold",true);
                final JSONArray kids = (JSONArray) top_item.remove("kids");
                if (kids != null){
                    JSONObject ch_item;
                    boolean single = mSingleSel;
                    for (int i = 0,size = kids.size();i < size;i++){
                        ch_item = kids.getJSONObject(i);
                        if (!single && !ch_item.getBooleanValue("isSel"))top_item.put("isSel",is_sel);
                        mData.add(index + i + 1,ch_item);
                        if (isChild(ch_item,item)){
                            unfoldParentItem(ch_item,index + i + 1);
                            index += 1;
                        }
                    }
                }
            }
        }
    }

    private boolean isChild(final JSONObject parent_item,final JSONObject child_ite){
        JSONObject parent = child_ite.getJSONObject("p_ref");
        while (parent != null){
            if (parent_item == parent){
                return true;
            }
            parent = parent.getJSONObject("p_ref");
        }
        return false;
    }

    private JSONObject getTopParent(final JSONObject item){
        JSONObject parent = item.getJSONObject("p_ref"),top_parent = item;
        while (parent != null){
            top_parent = parent;
            parent = parent.getJSONObject("p_ref");
        }
        return top_parent;
    }

    private int getItemIndex(final JSONObject item){
        if (null != mData && item != null){
            JSONObject ch_item;
            for (int i = 0, size = mData.size(); i < size; i++){
                ch_item = mData.getJSONObject(i);
                if (Utils.getNullStringAsEmpty(ch_item,COL_ID).equals(item.getString(COL_ID))){
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public JSONArray getMultipleSelectedContent(){
        final JSONArray objects = new JSONArray();
        JSONObject object;
        for (int i = 0, size = mData.size(); i < size; i++){
            object = mData.getJSONObject(i);
            if (object.getBooleanValue("isSel")){
                object = Utils.JsondeepCopy(object);
                object.remove("kids");
                objects.add(object);
            }
        }
        return objects;
    }

    @Override
    public JSONObject getSingleSelectedContent(){
        JSONObject object;
        for (int i = 0, size = mData.size(); i < size; i++){
            object = mData.getJSONObject(i);
            if (object.getBooleanValue("isSel")){
                object = Utils.JsondeepCopy(object);
                object.remove("kids");
                object.remove("p_ref");
                return object;
            }
        }
        return new JSONObject();
    }
}

