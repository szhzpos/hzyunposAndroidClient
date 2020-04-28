package com.wyc.cloudapp.dialog.barcodeScales;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.BarCodeScaleAdapter;
import com.wyc.cloudapp.dialog.MyDialog;


import java.util.List;
import java.util.concurrent.Future;

import static android.content.Context.WINDOW_SERVICE;

public class BarCodeScaleDownDialog extends Dialog {
    private Context mContext;
    private BarCodeScaleAdapter mBarCodeScaleAdapter;
    private List<Future<Boolean>> mFutureList;
    public BarCodeScaleDownDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.barcode_scale_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //
        initView();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v -> {
            if (isDownloadFinished()){
                BarCodeScaleDownDialog.this.dismiss();
            }
        });
        findViewById(R.id.add_scale).setOnClickListener(v -> {
            AddBarCodeScaleDialog addBarCodeScaleDialog = new AddBarCodeScaleDialog(mContext);
            addBarCodeScaleDialog.setGetContent(object -> {
                mBarCodeScaleAdapter.addScale(object);
            });
            addBarCodeScaleDialog.show();
        });
        findViewById(R.id.del_scale).setOnClickListener(v -> {
            MyDialog.displayAskMessage(null, "是否删除条码秤信息？", mContext, myDialog -> {
                if (isDownloadFinished()){
                    mBarCodeScaleAdapter.deleteScale();
                    myDialog.dismiss();
                }
            },Dialog::dismiss);
        });
        findViewById(R.id.download_to_scale).setOnClickListener(v -> {
            if (isDownloadFinished()){
                mFutureList = AbstractBarcodeScaleImp.scaleDownLoad(mBarCodeScaleAdapter.getCurrentScalseInfos(),mBarCodeScaleAdapter.getCurrentItemIndexMap());
            }
         });
        findViewById(R.id.modfy_scale).setOnClickListener(v -> {
            if (isDownloadFinished()){
                JSONArray array = mBarCodeScaleAdapter.getCurrentScalseInfos();
                if (array != null && array.size() != 0){
                    AddBarCodeScaleDialog addBarCodeScaleDialog = new AddBarCodeScaleDialog(mContext,array.getJSONObject(0));
                    addBarCodeScaleDialog.setGetContent(object -> mBarCodeScaleAdapter.addScale(object));
                    addBarCodeScaleDialog.show();
                }
            }
        });

        //初始化窗口尺寸
        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.8 * point.y); // 宽度
                dialogWindow.setAttributes(lp);
            }
        }
    }

    private void initView(){
        mBarCodeScaleAdapter = new BarCodeScaleAdapter(mContext);
        RecyclerView recyclerView = findViewById(R.id.b_scalse_r_v);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mBarCodeScaleAdapter);
        //加载信息
        mBarCodeScaleAdapter.setDatas();
    }

    private boolean isDownloadFinished(){
        if (mFutureList != null){
            for (Future<Boolean> future : mFutureList){
                if (!MyDialog.ToastMessage(null,"正在下载,请稍后操作!",mContext,getWindow(),future.isDone())){
                    return false;
                }
            }
        }
        return true;
    }
}
