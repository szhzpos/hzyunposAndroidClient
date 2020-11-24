package com.wyc.cloudapp.dialog.barcodeScales;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.BarCodeScaleAdapter;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext;


import java.util.List;
import java.util.concurrent.Future;

import static android.content.Context.WINDOW_SERVICE;

public class BarCodeScaleDownDialog extends AbstractDialogContext {
    private BarCodeScaleAdapter mBarCodeScaleAdapter;
    private List<Future<Boolean>> mFutureList;
    public BarCodeScaleDownDialog(@NonNull Context context) {
        super(context,context.getString(R.string.b_code_s_d_sz));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRecyclerView();
        initAddScaleBtn();
        initDelBtn();
        initDownloadToScaleBtn();
        initModifyBtn();
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.barcode_scale_dialog_layout;
    }

    @Override
    protected void closeWindow(){
        if (isDownloadFinished()){
            this.dismiss();
        }
    }

    private void initAddScaleBtn(){
        final Button add_scale_btn = findViewById(R.id.add_scale);
        if (null != add_scale_btn)
            add_scale_btn.setOnClickListener(v -> {
                AddBarcodeScaleDialog addBarCodeScaleDialog = new AddBarcodeScaleDialog(mContext,mContext.getString(R.string.add_scale_sz));
                addBarCodeScaleDialog.setGetContent(object -> {
                    mBarCodeScaleAdapter.addScale(object);
                });
                addBarCodeScaleDialog.show();
            });
    }
    private void initDelBtn(){
        final Button del_scale_btn = findViewById(R.id.del_scale);
        if (null != del_scale_btn)
            del_scale_btn.setOnClickListener(v -> {
                if (!mBarCodeScaleAdapter.getCurrentScalseInfos().isEmpty())
                    if (isDownloadFinished()){
                        MyDialog.displayAskMessage(null, "是否删除条码秤信息？", mContext, myDialog -> {
                            mBarCodeScaleAdapter.deleteScale();
                            myDialog.dismiss();
                        },Dialog::dismiss);
                    }
            });
    }
    private void initDownloadToScaleBtn(){
        final Button download_to_scale_btn = findViewById(R.id.download_to_scale);
        if (null != download_to_scale_btn)
            download_to_scale_btn.setOnClickListener(v -> {
                if (isDownloadFinished()){
                    mFutureList = AbstractBarcodeScaleImp.scaleDownLoad(mBarCodeScaleAdapter.getCurrentScalseInfos(),mBarCodeScaleAdapter.getCurrentItemIndexMap());
                }
            });
    }
    private void initModifyBtn(){
        final Button modify_btn = findViewById(R.id.modfy_scale);
        if (null != modify_btn)
            modify_btn.setOnClickListener(v -> {
                if (isDownloadFinished()){
                    final JSONArray array = mBarCodeScaleAdapter.getCurrentScalseInfos();
                    if (array != null && !array.isEmpty()){
                        AddBarcodeScaleDialog addBarCodeScaleDialog = new AddBarcodeScaleDialog(mContext,array.getJSONObject(0));
                        addBarCodeScaleDialog.setGetContent(object -> mBarCodeScaleAdapter.addScale(object));
                        addBarCodeScaleDialog.show();
                    }
                }
            });
    }

    @Override
    protected void initWindowSize(){
        final WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.8 * point.y);
                dialogWindow.setAttributes(lp);
            }
        }
    }

    private void initRecyclerView(){
        mBarCodeScaleAdapter = new BarCodeScaleAdapter(mContext);
        final RecyclerView recyclerView = findViewById(R.id.b_scalse_r_v);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mBarCodeScaleAdapter);
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
