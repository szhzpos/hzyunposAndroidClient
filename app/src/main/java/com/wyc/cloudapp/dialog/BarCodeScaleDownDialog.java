package com.wyc.cloudapp.dialog;

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
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONObject;

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
        findViewById(R.id.add_scale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddBarCodeScalseDialog addBarCodeScalseDialog = new AddBarCodeScalseDialog(mContext);
                addBarCodeScalseDialog.setGetContent(new AddBarCodeScalseDialog.OnGetContent() {
                    @Override
                    public void getContent(JSONObject object) {
                        mBarCodeScaleAdapter.addScalse(object);

                        Logger.d(mBarCodeScaleAdapter.toString());
                    }
                });
                addBarCodeScalseDialog.show();
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
    }
}
