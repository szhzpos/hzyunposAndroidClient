package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.CustomizationView.SwipeLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.business.MobileEditGoodsCategoryActivity;
import com.wyc.cloudapp.adapter.bean.TreeListItem;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: AddGoodsCategoryAdapter
 * @Description: 商品分类管理适配器
 * @Author: wyc
 * @CreateDate: 2021/5/10 18:21
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/10 18:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class EditGoodsCategoryAdapter extends TreeListBaseAdapterWithList<EditGoodsCategoryAdapter.MyViewHolder>{
    public EditGoodsCategoryAdapter(MobileEditGoodsCategoryActivity context, boolean single) {
        super(context, single);
    }
    @Override
    int getContentLayoutId() {
        return R.id.content_layout;
    }

    static class MyViewHolder extends TreeListBaseAdapterWithList.MyViewHolder {
        TextView content;
        ImageView add,modify;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            content = findViewById(R.id.content);
            add = findViewById(R.id.add);
            modify = findViewById(R.id.modify);
        }
        @Override
        protected int getContentResourceLayout() {
            return R.layout.mobile_add_goods_category_adapter;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = getView();
        if (view instanceof SwipeLayout){
            final SwipeLayout swipeLayout = (SwipeLayout)view;
            swipeLayout.addMenuItem(mContext.getString(R.string.modify_sz), v -> editCategory(),mContext.getResources().getColor(R.color.green,null));
        }
        return new MyViewHolder(view);
    }
    @Override
    void bindContent(@NonNull MyViewHolder holder, TreeListItem object) {
        if (object.getCode().length() < 6) {
            if (!holder.add.hasOnClickListeners())
                holder.add.setOnClickListener(v -> {
                    holder.itemView.callOnClick();
                    ((MobileEditGoodsCategoryActivity) mContext).editCategory(false);
                });
            if (holder.add.getVisibility() == View.INVISIBLE)holder.add.setVisibility(View.VISIBLE);
        }else
            holder.add.setVisibility(View.INVISIBLE);

        if (!holder.modify.hasOnClickListeners())holder.modify.setOnClickListener(v -> {
            holder.itemView.callOnClick();
            editCategory();
        });
        holder.content.setText(String.format(Locale.CHINA,"%s - %s",object.getCode(),object.getItem_name()));
    }

    @Override
    protected int clickItemIndex() {
        return -1;
    }

    public void addCategory(final String id,final String code,final String name,int level){
        final TreeListItem item = new TreeListItem();
        item.setItem_id(id);
        item.setCode(code);
        item.setItem_name(name);
        item.setLevel(level);
        item.setSel(true);
        addItemChildOfCurrent(item);
    }
    private void editCategory(){
        ((MobileEditGoodsCategoryActivity)mContext).editCategory(true);
    }
}
