package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.bean.TreeListItem;
import com.wyc.cloudapp.application.CustomApplication;
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
public abstract class TreeListBaseAdapterWithList<T extends TreeListBaseAdapterWithList.MyViewHolder> extends TreeListBaseAdapter<List<TreeListItem>,TreeListItem,T> {
    protected final Context mContext;
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
        setData(new ArrayList<>(),null);
    }

    public TreeListBaseAdapterWithList(Context context,boolean single){
        this(context);
        mSingleSel = single;
    }

    @Override
    public TreeListBaseAdapterWithList<T> setData(final List<TreeListItem> array,final List<TreeListItem> selected){
        mData = array;
        if (Looper.myLooper() != Looper.getMainLooper()){
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }else
            notifyDataSetChanged();

        return this;
    }

    @Override
    public void setData(final List<TreeListItem> data,boolean singleSel){
        mSingleSel = singleSel;
        setData(data,null);
    }

    protected final View getView(){
        final View itemView = View.inflate(mContext, R.layout.swipe_tree_item_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_50)));
        return itemView;
    }

    abstract int getContentLayoutId();
    abstract void bindContent(@NonNull T holder, final TreeListItem object);
    protected int clickItemIndex(){
        return 0;
    }

    @Override
    public final void onBindViewHolder(@NonNull T holder, int position) {
        final int old_pos = position;
        final TreeListItem item = mData.get(position);
        holder.row_id.setText(String.valueOf(position));

        boolean is_unfold = item.isUnfold();
        boolean is_sel = item.isSel();

        final ImageView ico = holder.icon;
        if ( (position += 1) == mData.size())position -= 1;
        if (is_unfold || mData.get(position).getP_ref() == item || !item.getKids().isEmpty()){
            if (ico.getVisibility() == View.INVISIBLE)ico.setVisibility(View.VISIBLE);
            ico.setOnClickListener(unfoldIcoListener);
            if (is_unfold){
                ico.setImageDrawable(mUnfoldDb);
            }else {
                ico.setImageDrawable(mFoldDb);
            }
        }else {
            if (ico.getVisibility() == View.VISIBLE)ico.setVisibility(View.INVISIBLE);
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

    @Override
    public int getItemCount() {
        return mData.size();
    }
    @Override
    public void onViewRecycled (@NonNull MyViewHolder holder){
        if (mItemClick != null && mCurrentItemView == holder.itemView)setViewBackgroundColor(mCurrentItemView,false);
    }

    public void setData(final JSONArray datas, final JSONArray items){
        if (datas != null)mData = JSON.parseObject(datas.toString(),new TypeReference<List<TreeListItem>>(TreeListItem.class) {}.getType());

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
            if (row_id >= 0 && row_id < mData.size()){
                final TreeListItem object = mData.get(row_id);
                if (mSingleSel){
                    clearSelected();
                    object.setSel(isChecked);
                }else
                    selectItem(object,row_id + 1,isChecked);
            }
        }
        CustomApplication.runInMainThread(this::notifyDataSetChanged);
    };

    private void clearSelected(){
        if (mData != null){
            for (TreeListItem o :mData){
                if (o.isSel())o.setSel(false);
            }
        }
    }
    private void selectItem(final TreeListItem object, int index, boolean isChecked){
        if (null != object){
            object.setSel(isChecked);
            TreeListItem child;
            for (int j = index,size = mData.size();0 <= j && j < size;j++){
                child = mData.get(j);
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
        if (row_id >= 0 && row_id < mData.size()){
            final TreeListItem object = mData.get(row_id);
            boolean unfold = object.isUnfold(),is_sel = object.isSel();
            object.setUnfold(!unfold);
            if (!unfold){
                final List<TreeListItem> kids =  object.getKids();
                object.setKids(null);
                boolean single = mSingleSel;
                TreeListItem item;
                for (int i = 0,size = kids.size();i < size;i++){
                    item = kids.get(i);
                    if (!single && !item.isSel())item.setSel(is_sel);
                    mData.add(row_id + i + 1,item);
                }
            }else {
                foldChildren(object,row_id + 1);
            }
            CustomApplication.runInMainThread(this::notifyDataSetChanged);
        }
    };
    private void foldChildren(final TreeListItem parent, int index){
        if (parent != null){
            final List<TreeListItem> kids = new ArrayList<>();
            TreeListItem child;
            for (int j = index;0 <= j && j < mData.size();j++){
                child = mData.get(index);
                if (child.getP_ref() == parent){
                    if (child.isUnfold()){
                        child.setUnfold(false);
                        foldChildren(child,index + 1);
                    }
                    kids.add(mData.remove(index));
                    j--;
                }else {
                    break;
                }
            }
            parent.setKids(kids);
        }
    }

    private final View.OnClickListener itemListener = (v)->{
        if (mItemClick != null){
            TreeListItem obj;
            if (mCurrentItemView != v){
                if (null != mCurrentItemView){
                    obj = TreeListItem.getViewTagValue(mCurrentItemView);
                    if (!obj.isEmpty())obj.setSel(false);
                }
                setViewBackgroundColor(mCurrentItemView,false);
                mCurrentItemView = v;
                setViewBackgroundColor(v,true);
            }
            obj = TreeListItem.getViewTagValue(v);
            if (!obj.isEmpty()){
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
            final View content = view.findViewById(getContentLayoutId());
            if (content instanceof ViewGroup){
                final ViewGroup group = (ViewGroup)content;
                View ch;
                for (int i = 0,count = group.getChildCount();i < count;i++){
                    ch = group.getChildAt(i);
                    if (ch instanceof TextView){
                        ((TextView) ch).setTextColor(text_color);
                    }
                }
            }
        }
    }

    private TreeListItem getCurrentItem(){
        int row_id = getCurrentIndex();
        if (row_id >= 0 && row_id < mData.size()){
            final TreeListItem obj = mData.get(row_id);
            obj.setSel(true);

            final TreeListItem object = obj.clone();
            object.setKids(null);
            object.setP_ref(null);
            return object;
        }
        return new TreeListItem();
    }
    protected int getCurrentIndex(){
        if (null != mCurrentItemView){
            final TextView row_id_tv = mCurrentItemView.findViewById(R.id.row_id);
            if (row_id_tv != null) {
                return Integer.parseInt(row_id_tv.getText().toString());
            }
        }
        return -1;
    }

    private void addItem(int index,final TreeListItem item){
        if (index >= 0 && index < mData.size()){
            mData.add(index,item);
        }
    }

    protected void addItemChildOfCurrent(final TreeListItem item){
        int level = item.getLevel();
        if (0 == level){//顶层元素
            mData.add(item);
            notifyDataChange();
        }else {
            int index = getCurrentIndex(),size = mData.size();
            if (index >= 0 && index < size){
                final TreeListItem cur = mData.get(index);
                final List<TreeListItem> listItems = cur.getKids();

                item.setP_ref(cur);
                cur.setSel(false);

                if (!cur.isUnfold()){
                    listItems.add(item);

                    cur.setUnfold(true);
                    cur.setKids(null);
                    boolean is_sel = cur.isSel();
                    TreeListItem t;
                    for (int i = 0,list_size = listItems.size();i < list_size;i++){
                        t = listItems.get(i);
                        if (!mSingleSel && !t.isSel())t.setSel(is_sel);
                        mData.add(index + i + 1,t);
                    }
                }else {
                    index ++;
                    if (index == size)
                        mData.add(item);
                    else
                        mData.add(index,item);
                }
                notifyDataChange();
            }
        }
    }

    public void updateCurrentItem(final String name){
        final TreeListItem item = TreeListItem.getViewTagValue(mCurrentItemView);
        if (!item.isEmpty()){
            item.setItem_name(name);
            notifyDataChange();
        }
    }

    private void notifyDataChange(){
        if (Looper.myLooper() == Looper.getMainLooper())
            notifyDataSetChanged();
        else CustomApplication.runInMainThread(this::notifyDataSetChanged);
    }

    public interface OnItemClick{
        void OnClick(final TreeListItem object);
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
            TreeListItem item;
            for (int i = 0,size = mData.size();i < size;i++){
                item = mData.get(i);
                if (setSelectedItem(item,object,i)){
                    break;
                }
            }
        }
    }

    private boolean setSelectedItem(final TreeListItem item, final JSONObject object, int first_index){
        if (Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID).equals(item.getItem_id())){
            item.setSel(true);
            unfoldParentItem(item,first_index);
            return true;
        }else {
            final List<TreeListItem> kids = item.getKids();
            for (int j = 0,j_size = kids.size();j < j_size;j++){
                if (setSelectedItem(kids.get(j),object,first_index)){
                    return true;
                }
            }
        }
        return false;
    }

    private void unfoldParentItem(final TreeListItem item , int index){
        if (item != null && item.getP_ref() != null){//顶层item不需要展开
            final TreeListItem top_item = mData.get(index);
            boolean unfold = top_item.isUnfold(),is_sel = top_item.isSel();
            if (!unfold){
                top_item.setUnfold(true);
                final List<TreeListItem> kids = top_item.getKids();
                top_item.setKids(null);
                if (kids != null){
                    TreeListItem ch_item;
                    boolean single = mSingleSel;
                    for (int i = 0,size = kids.size();i < size;i++){
                        ch_item = kids.get(i);
                        if (!single && !ch_item.isSel())top_item.setSel(is_sel);
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

    private boolean isChild(final TreeListItem parent_item, final TreeListItem child_ite){
        TreeListItem parent = child_ite.getP_ref();
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

    private int getItemIndex(final TreeListItem item){
        if (null != mData && item != null){
            TreeListItem ch_item;
            for (int i = 0,size = mData.size();i < size;i++){
                ch_item = mData.get(i);
                if (ch_item.getItem_id().equals(item.getItem_id())){
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public List<TreeListItem> getMultipleSelectedContent(){
        final List<TreeListItem> objects = new ArrayList<>();
        TreeListItem object;
        for (int i = 0,size = mData.size();i < size;i++){
            object = mData.get(i);
            if (object.isSel()){
                object = object.clone();
                object.setKids(null);
                objects.add(object);
            }
        }
        return objects;
    }

    @Override
    public TreeListItem getSingleSelectedContent(){
        TreeListItem object;
        for (int i = 0,size = mData.size();i < size;i++){
            object = mData.get(i);
            if (object.isSel()){
                object = object.clone();
                object.setKids(null);
                object.setP_ref(null);
                return object;
            }
        }
        return new TreeListItem();
    }
}
