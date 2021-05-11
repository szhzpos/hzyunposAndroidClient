package com.wyc.cloudapp.dialog.goods;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.BuyFullGiveXGoodsAdapter;
import com.wyc.cloudapp.adapter.BuyFullGiveXRuleAdapter;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.goods
 * @ClassName: BuyFullGiveXSelectDialog
 * @Description: 买满赠方案选择对话框
 * @Author: wyc
 * @CreateDate: 2021/3/22 16:47
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/22 16:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BuyFullGiveXSelectDialog extends AbstractDialogSaleActivity {
    private TextView mRuleDescriptionTv;
    private BuyFullGiveXGoodsAdapter mGoodsAdapter;
    private final JSONArray mRules;
    private int mGiveWay,mDiscountItem = -1;//当mGiveWay为1时，mDiscountItem为赠送任选项数量
    public BuyFullGiveXSelectDialog(@NonNull SaleActivity context,final JSONArray data) {
        super(context, "买满赠送促销选择");
        mRules = data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRuleDescriptionTv = findViewById(R.id.rule_description_tv);
        initRuleList();
        initGoodsList();
        initBtn();
    }

    @Override
    protected int getContentLayoutId() {
        if (Utils.lessThan7Inches(mContext)){
            return R.layout.mobile_buyfull_give_x_select_dialog_layout;
        }
        return R.layout.buyfull_give_x_select_dialog_layout;
    }

    protected double getWidthRatio(){
        return 0.98;
    }

    private void initRuleList(){
        final RecyclerView rule_list = findViewById(R.id.rule_list);
        final BuyFullGiveXRuleAdapter listAdapter = new BuyFullGiveXRuleAdapter(mContext,true);
        listAdapter.setData(formatRules(),null);
        listAdapter.setItemListener(object -> {
            for (int i = 0, size = mRules.size(); i < size; i ++){
                final JSONObject rule = mRules.getJSONObject(i);
                if (Utils.getNullStringAsEmpty(rule,"tlpb_id").equals(object.getString("item_id"))){
                    mRuleDescriptionTv.setText(generateDes(rule));
                    final JSONArray givex_goods_info = JSONArray.parseArray(Utils.getNullOrEmptyStringAsDefault(rule,"givex_goods_info","[]"));
                    mGoodsAdapter.setData(formatPresentGoods(givex_goods_info),false);
                    break;
                }
            }
        });
        rule_list.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        rule_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        rule_list.setAdapter(listAdapter);
    }
    private JSONArray formatPresentGoods(final JSONArray array){
        final JSONArray data = new JSONArray();
        if (null != array){
            JSONObject object;
            for (int i = 0,size = array.size();i < size;i++){
                final JSONObject tmp = array.getJSONObject(i);
                final String id = Utils.getNullStringAsEmpty(tmp,"barcode_id");

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",mGiveWay == 0);
                object.put("item_id",id);
                object.put("content",tmp);
                data.add(object);
            }
        }
        return data;
    }

    private String generateDes(final JSONObject rule){
        int promotion_type = Utils.getNotKeyAsNumberDefault(rule,"promotion_type",0),fullgive_way = Utils.getNotKeyAsNumberDefault(rule,"fullgive_way",-1);
        String type_name,way_name,give_way_name;
        switch (promotion_type){
            case 1:
                type_name = "按商品";
                break;
            case 2:
                type_name = "按类别";
                break;
            case 3:
                type_name = "按货商";
                break;
            case 4:
                type_name = "按品牌";
                break;
            default:
                type_name = "全场促销";
        }
        if (fullgive_way == 1){
            way_name = mContext.getString(R.string.amt_not_colon_sz);
        }else {
            way_name = mContext.getString(R.string.num_not_colon_sz);
        }

        mGiveWay = Utils.getNotKeyAsNumberDefault(rule,"give_way",0);
        if (mGiveWay == 0){
            give_way_name = "全部";
        }else {
            mDiscountItem = Utils.getNotKeyAsNumberDefault(rule,"item_discount",-1);
            give_way_name = String.format(Locale.CHINA,"任选%d件",mDiscountItem);
        }

        double buyfull_money = Utils.getNotKeyAsNumberDefault(rule,"buyfull_money",0.0);
        return String.format(Locale.CHINA,"【%s】购物满%s【%.2f】,赠送以下%s商品",type_name,way_name,buyfull_money,give_way_name);
    }
    private JSONArray formatRules(){
        final JSONArray array = mRules,data = new JSONArray();
        if (null != array){
            JSONObject object;
            for (int i = 0,size = array.size();i < size;i++){
                final JSONObject tmp = array.getJSONObject(i);
                final String id = Utils.getNullStringAsEmpty(tmp,"tlpb_id"),name = Utils.getNullStringAsEmpty(tmp,"title");

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("item_name",name);
                data.add(object);
            }
        }
        return data;
    }

    private void initGoodsList(){
        final RecyclerView table_body = findViewById(R.id.table_body);
        mGoodsAdapter = new BuyFullGiveXGoodsAdapter(mContext,true);
        table_body.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        table_body.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        table_body.setAdapter(mGoodsAdapter);
    }

    private void initBtn(){
        final Button cancel = findViewById(R.id.t_cancel),ok = findViewById(R.id.t_ok);
        cancel.setOnClickListener(v -> closeWindow());
        ok.setOnClickListener(v -> {
            final JSONArray array = mGoodsAdapter.getMultipleSelectedContent();
            if (mDiscountItem != -1 ){
                if (array.size() > mDiscountItem){
                    MyDialog.displayMessage(mContext,String.format(Locale.CHINA,"当前方案只能任选%d项!",mDiscountItem));
                }else {
                    addPresentGoods(array);
                }
            }else {
                addPresentGoods(array);
            }
        });
    }

    private void addPresentGoods(final JSONArray items){
        for (int i = 0,size = items.size();i < size;i ++){
            final JSONObject item = items.getJSONObject(i),content = item.getJSONObject("content"),goods  = new JSONObject();
            Logger.d_json(content.toString());
            if (mGoodsAdapter.findBuyFullGiveGoodsByBarcodeId(goods,content.getString("barcode_id"))){
                goods.put("xnum",content.getDoubleValue("xnum_give"));
                goods.put("price",content.getDoubleValue("markup_price"));

                GoodsInfoViewAdapter.makeBuyFullGiveX(goods);

                Logger.d_json(goods.toString());

                mContext.addBuyFullGiveGoods(goods);
            }else {
                MyDialog.displayErrorMessage(mContext,goods.getString("info"));
                return;
            }
        }
        closeWindow();
    }
}
