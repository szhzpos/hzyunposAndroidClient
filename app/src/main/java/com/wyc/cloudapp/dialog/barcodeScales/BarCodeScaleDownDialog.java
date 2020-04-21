package com.wyc.cloudapp.dialog.barcodeScales;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.BarCodeScaleAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.apache.log4j.net.SocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;

public class BarCodeScaleDownDialog extends Dialog {
    private Context mContext;
    private BarCodeScaleAdapter mBarCodeScaleAdapter;
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
        findViewById(R.id._close).setOnClickListener(v -> this.dismiss());
        findViewById(R.id.add_scale).setOnClickListener(v -> {
            AddBarCodeScaleDialog addBarCodeScaleDialog = new AddBarCodeScaleDialog(mContext);
            addBarCodeScaleDialog.setGetContent(object -> {

                mBarCodeScaleAdapter.addScale(object);

            });
            addBarCodeScaleDialog.show();
        });
        findViewById(R.id.del_scale).setOnClickListener(v -> mBarCodeScaleAdapter.deleteScale()
        );
        findViewById(R.id.download_to_scale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d_json(mBarCodeScaleAdapter.getCurrentScalseInfos().toString());
                JSONArray scale_infos = mBarCodeScaleAdapter.getCurrentScalseInfos();
                StringBuilder err = new StringBuilder();
                for (int i = 0,size = scale_infos.length();i < size;i++){
                    final JSONObject object = scale_infos.optJSONObject(i);
                    if (null != object){
                        CustomApplication.execute(()->{
                            try{
                                if (!AbstractBarcodeScale.scaleDownLoad(object,err)){
                                    Logger.d("下载错误：%s",err);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                Logger.i("条码秤下载最外层异常捕获：%s" + e.getMessage());
                            }
                        });
                    }
                }
             }
        });
        findViewById(R.id.modfy_scale).setOnClickListener(v -> {
            JSONArray array = mBarCodeScaleAdapter.getCurrentScalseInfos();
            if (array != null && array.length() != 0){
                AddBarCodeScaleDialog addBarCodeScaleDialog = new AddBarCodeScaleDialog(mContext,array.optJSONObject(0));
                addBarCodeScaleDialog.setGetContent(object -> mBarCodeScaleAdapter.addScale(object));
                addBarCodeScaleDialog.show();
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
}
