package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.bean.TreeListItem;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: TreeListBaseAdapter
 * @Description: 树形结构适配器基类
 * @Author: wyc
 * @CreateDate: 2021-06-22 11:17
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-22 11:17
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class TreeListBaseAdapter<D,S,T extends AbstractDataAdapter.SuperViewHolder> extends AbstractDataAdapter<D,T> {
    public static final String COL_ID = "item_id",COL_NAME = "item_name";
    public abstract TreeListBaseAdapter<D,S,T> setData(final D data,final D selectedData);
    public abstract void setData(final D data,boolean b);
    public abstract D getMultipleSelectedContent();
    public abstract S getSingleSelectedContent();

    static abstract class MyViewHolder extends AbstractDataAdapter.SuperViewHolder {
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
}
