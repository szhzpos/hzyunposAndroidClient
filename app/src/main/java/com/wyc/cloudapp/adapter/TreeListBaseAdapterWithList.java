package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.bean.TreeList;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: TreeListBaseAdapterWithList
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021/5/10 17:29
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/10 17:29
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class TreeListBaseAdapterWithList<T extends TreeListBaseAdapterWithList.MyViewHolder> extends RecyclerView.Adapter<T>  {
    private List<TreeList> mDatas;
    private final Context mContext;
    private boolean mSingleSel = true;
    private final Drawable mUnfoldDb,mFoldDb;
    private OnItemClick mItemClick;//不为null的时候会隐藏单选、多选按钮，改变mCurrentItemView的背景色来表示选中；此种模式下只支持选中单个项目并触发事件。
    private View mCurrentItemView;
    /*    Item{
            p_ref,level,unfold,isSel,item_id,item_name,kids; <p_ref , kids>存在上下级时必须存在
        }*/
    private TreeListBaseAdapterWithList(Context context){
        this.mContext = context;
        mUnfoldDb = context.getDrawable(R.drawable.unfold);
        mFoldDb = context.getDrawable(R.drawable.fold);
        mDatas = new ArrayList<>();
    }

    public TreeListBaseAdapterWithList(Context context,boolean single){
        this(context);
        mSingleSel = single;
    }

    static abstract class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        CheckBox mul_cb;
        RadioButton single_rb;
        TextView item_id,row_id;
        ViewStub content;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id = findViewById(R.id.row_id);
            icon = findViewById(R.id.item_ico);
            mul_cb = findViewById(R.id.multiple_cb);
            single_rb = findViewById(R.id.single_rb);
            item_id = findViewById(R.id.item_id);
            content = findViewById(R.id.content_view);
            content.setLayoutResource(getContentResourceId());
            content.inflate();
        }

        protected abstract int getContentResourceId();
        protected final <T extends View> T  findViewById(int id){
            return itemView.findViewById(id);
        }
    }

    protected final View getView(){
        View itemView = View.inflate(mContext, R.layout.tree_item_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_50)));
        return itemView;
    }

    abstract void bindContent(@NonNull T holder, final TreeList object);
    protected int clickItemIndex(){
        return 0;
    }

    @Override
    public final void onBindViewHolder(@NonNull T holder, int position) {
        final int old_pos = position;
        final TreeList item = mDatas.get(position);
        if (item != null){

            holder.row_id.setText(String.valueOf(position));

            boolean is_unfold = item.isUnfold();
            boolean is_sel = item.isSel();

            final ImageView ico = holder.icon;
            if ( (position += 1) == mDatas.size())position -= 1;
            if (is_unfold || mDatas.get(position).getP_ref() == item || !item.getKids().isEmpty()){
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
            holder.item_id.setText(item.getItem_id());


            holder.itemView.setPadding( 25 * item.getLevel(),0,0,0);
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
        return mDatas.size();
    }
    @Override
    public void onViewRecycled (@NonNull MyViewHolder holder){
        if (mItemClick != null && mCurrentItemView == holder.itemView)setViewBackgroundColor(mCurrentItemView,false);
    }

    public void setData(final JSONArray datas, final JSONArray items){
        if (datas != null)mDatas = JSON.parseObject(datas.toString(),new TypeReference<List<TreeList>>(TreeList.class) {}.getType());;

        setSelectedItem(items);

        notifyDataSetChanged();
    }

    public void setData(final JSONArray data,boolean singleSel){
        mSingleSel = singleSel;
        setData(data,null);
    }

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
        final View v = (View) buttonView.getParent();
        final TextView row_id_tv = v.findViewById(R.id.row_id);
        if (row_id_tv != null){
            int row_id = Integer.parseInt(row_id_tv.getText().toString());
            if (row_id >= 0 && row_id < mDatas.size()){
                final TreeList object = TreeList.getViewTagValue(v);
                if (mSingleSel){
                    clearSelected();
                    object.setSel(isChecked);
                }else
                    selectItem(object,row_id + 1,isChecked);
            }
        }
        buttonView.post(this::notifyDataSetChanged);
    };

    private void clearSelected(){
        if (mDatas != null){
            for (Object o :mDatas){
                final JSONObject object = (JSONObject)o;
                if (object.getBooleanValue("isSel"))object.put("isSel",false);
            }
        }
    }
    private void selectItem(final TreeList object,int index,boolean isChecked){
        if (null != object){
            object.setSel(isChecked);
            TreeList child;
            for (int j = index,size = mDatas.size();0 <= j && j < size;j++){
                child = mDatas.get(j);
                if (child.getP_ref() == object){
                    if (child.isUnfold()){
                        selectItem(child,j += 1,isChecked);
                    }
                    object.setSel(isChecked);
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
        if (row_id >= 0 && row_id < mDatas.size()){
            final TreeList object = mDatas.get(row_id);
            boolean unfold = object.isUnfold(),is_sel = object.isSel();
            object.setUnfold(!unfold);
            if (!unfold){
                final List<TreeList> kids =  object.getKids();
                object.setKids(null);
                if (kids != null){
                    boolean single = mSingleSel;
                    TreeList item;
                    for (int i = 0,size = kids.size();i < size;i++){
                        item = kids.get(i);
                        if (!single && !item.isSel())item.setSel(is_sel);
                        mDatas.add(row_id + i + 1,item);
                    }
                }
            }else {
                foldChildren(object,row_id + 1);
            }
            row_id_tv.post(this::notifyDataSetChanged);
        }
    };
    private void foldChildren(final TreeList parent, int index){
        if (parent != null){
            final List<TreeList> kids = new ArrayList<>();
            TreeList child;
            for (int j = index;0 <= j && j < mDatas.size();j++){
                child = mDatas.get(index);
                if (child.getP_ref() == parent){
                    if (child.isUnfold()){
                        child.setUnfold(false);
                        foldChildren(child,index + 1);
                    }
                    kids.add(mDatas.remove(index));
                    j--;
                }else {
                    parent.setKids(kids);
                    break;
                }
            }
        }
    }

    private final View.OnClickListener itemListener = (v)->{
        if (mItemClick != null){
            TreeList obj;
            if (mCurrentItemView != v){
                if (null != mCurrentItemView){
                    obj = TreeList.getViewTagValue(mCurrentItemView);
                    if (null != obj)obj.setSel(false);
                }
                setViewBackgroundColor(mCurrentItemView,false);
                mCurrentItemView = v;
                setViewBackgroundColor(v,true);
            }
            obj = TreeList.getViewTagValue(v);
            if (null != obj){
                obj.setSel(true);
                mItemClick.OnClick(obj);
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

    private TreeList getCurrentItem(final @NonNull View view){
        final TextView row_id_tv = view.findViewById(R.id.row_id);
        if (row_id_tv != null){
            int row_id = Integer.parseInt(row_id_tv.getText().toString());
            if (row_id >= 0 && row_id < mDatas.size()){
                final TreeList obj = mDatas.get(row_id);
                obj.setSel(true);

                final TreeList object = obj.clone();
                object.setKids(null);
                object.setP_ref(null);
                return object;
            }
        }
        return new TreeList();
    }

    public interface OnItemClick{
        void OnClick(final TreeList object);
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
            TreeList item;
            for (int i = 0,size = mDatas.size();i < size;i++){
                item = mDatas.get(i);
                if (setSelectedItem(item,object,i)){
                    break;
                }
            }
        }
    }

    private boolean setSelectedItem(final TreeList item,final JSONObject object,int first_index){
        if (Utils.getNullStringAsEmpty(object,"item_id").equals(item.getItem_id())){
            item.setSel(true);
            unfoldParentItem(item,first_index);
            return true;
        }else {
            final List<TreeList> kids = item.getKids();
            for (int j = 0,j_size = kids.size();j < j_size;j++){
                if (setSelectedItem(kids.get(j),object,first_index)){
                    return true;
                }
            }
        }
        return false;
    }

    private void unfoldParentItem(final TreeList item ,int index){
        if (item != null && item.getP_ref() != null){//顶层item不需要展开
            final TreeList top_item = mDatas.get(index);
            boolean unfold = top_item.isUnfold(),is_sel = top_item.isSel();
            if (!unfold){
                top_item.setUnfold(true);
                final List<TreeList> kids = top_item.getKids();
                top_item.setKids(null);
                if (kids != null){
                    TreeList ch_item;
                    boolean single = mSingleSel;
                    for (int i = 0,size = kids.size();i < size;i++){
                        ch_item = kids.get(i);
                        if (!single && !ch_item.isSel())top_item.setSel(is_sel);
                        mDatas.add(index + i + 1,ch_item);
                        if (isChild(ch_item,item)){
                            unfoldParentItem(ch_item,index + i + 1);
                            index += 1;
                        }
                    }
                }
            }
        }
    }

    private boolean isChild(final TreeList parent_item,final TreeList child_ite){
        TreeList parent = child_ite.getP_ref();
        while (parent != null){
            if (parent_item == parent){
                return true;
            }
            parent = parent.getP_ref();
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

    private int getItemIndex(final TreeList item){
        if (null != mDatas && item != null){
            TreeList ch_item;
            for (int i = 0,size = mDatas.size();i < size;i++){
                ch_item = mDatas.get(i);
                if (ch_item.getItem_id().equals(item.getItem_id())){
                    return i;
                }
            }
        }
        return -1;
    }


    public JSONArray getMultipleSelectedContent(){
        final JSONArray objects = new JSONArray();
        TreeList object;
        for (int i = 0,size = mDatas.size();i < size;i++){
            object = mDatas.get(i);
            if (object.isSel()){
                object = object.clone();
                object.setKids(null);
                objects.add(object);
            }
        }
        return objects;
    }

    public TreeList getSingleSelectedContent(){
        TreeList object;
        for (int i = 0,size = mDatas.size();i < size;i++){
            object = mDatas.get(i);
            if (object.isSel()){
                object = object.clone();
                object.setKids(null);
                object.setP_ref(null);
                return object;
            }
        }
        return new TreeList();
    }
}
