package com.wyc.cloudapp.adapter.report;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: CategoryNameAdapter
 * @Description: 类别统计报表类别名称适配器
 * @Author: wyc
 * @CreateDate: 2021/2/2 9:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/2 9:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileCategoryNameAdapter extends AbstractDataAdapter<MobileCategoryNameAdapter.MyViewHolder> {
    private View.OnClickListener mItemListener;
    private final Drawable drawable;
    public MobileCategoryNameAdapter(MainActivity context) {
        super(context);
        drawable = context.getDrawable(R.drawable.small_fold);
        if (drawable != null) drawable.setBounds(0, 0, drawable.getIntrinsicWidth() / 2 , drawable.getIntrinsicHeight() / 2 );
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.category_name_adapter_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,58));

        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mDatas != null) {
            final JSONObject object = mDatas.getJSONObject(position);
            final TextView view = holder.category_name_tv;

            view.setText(String.format(Locale.CHINA, "%s%s%s", object.getString("name"), "\n", object.getString("category_code")));

            view.setTag(Utils.getNotKeyAsNumberDefault(object,"category_id",-1));
            if (object.getIntValue("has_child") == 1) {
                if (view.getPaddingRight() != 0) view.setPadding(view.getPaddingLeft(), -1, 0, -1);
                view.setCompoundDrawables(null, null, drawable, null);
                if (mItemListener != null) view.setOnClickListener(mItemListener);
            } else {
                final Rect r = drawable.getBounds();
                if (view.getPaddingRight() == 0)
                    view.setPadding(view.getPaddingLeft(), -1, r.right, -1);
                view.setCompoundDrawables(null, null, null, null);
                view.setOnClickListener(null);
            }
        }
    }

    public void setItemListener(final View.OnClickListener listener){
        mItemListener = listener;
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView category_name_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            category_name_tv = itemView.findViewById(R.id.category_name);
        }
    }
}
