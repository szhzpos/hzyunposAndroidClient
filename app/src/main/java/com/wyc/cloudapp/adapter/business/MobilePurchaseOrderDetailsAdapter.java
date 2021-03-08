package com.wyc.cloudapp.adapter.business;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobilePurchaseOrderDetailsAdapter
 * @Description: 采购订货单商品明细适配器
 * @Author: wyc
 * @CreateDate: 2021/2/23 15:32
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/23 15:32
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class MobilePurchaseOrderDetailsAdapter extends MobileBaseOrderDetailsAdapter {
    public MobilePurchaseOrderDetailsAdapter(final MainActivity activity){
        super(activity);
    }
}
