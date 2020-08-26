package com.wyc.cloudapp.dialog.goods;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.GoodsCategoryViewAdapter;
import com.wyc.cloudapp.adapter.TreeListAdapter;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.dialog.barcodeScales.AbstractBarcodeScaleImp;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import static android.content.Context.WINDOW_SERVICE;

public class GoodsManageDialog extends AbstractDialogBaseOnMainActivityImp {
    public GoodsManageDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.manage_goods_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initList();
        initWindowSize();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.goods_manage_dialog_layout;
    }

    private void initList(){
        final RecyclerView item_list = findViewById(R.id.goods_category_list);
        final TreeListAdapter listAdapter = new TreeListAdapter(mContext,true);
        listAdapter.setDatas(GoodsCategoryViewAdapter.getCategoryAsTreeListData(mContext),null);
        item_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(listAdapter);
        listAdapter.setItemListener(new TreeListAdapter.OnItemClick() {
            @Override
            public void OnClick(JSONObject object) {
                Logger.d_json(object.toString());
            }
        });
    }

    private void initWindowSize(){//初始化窗口尺寸
        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.98 * point.y);
                dialogWindow.setAttributes(lp);
            }
        }
    }
}
