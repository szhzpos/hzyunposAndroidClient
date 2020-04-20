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

                Logger.d(mBarCodeScaleAdapter.toString());
            });
            addBarCodeScaleDialog.show();
        });
        findViewById(R.id.del_scale).setOnClickListener(v -> mBarCodeScaleAdapter.deleteScale()
        );
        findViewById(R.id.download_to_scale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d_json(mBarCodeScaleAdapter.getCurrentScalseInfos().toString());
                CustomApplication.execute(()->{
                    try {
                        download();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
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

    private void download() throws IOException {

        byte[] jj = new byte[]{0x0D,0x0A,0x03};




        byte[] bytes = new byte[1024];
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("192.168.0.150",4001),3000);
        socket.setSoTimeout(5000);
        Logger.d(socket.getInetAddress().toString());
        OutputStream outputStream = socket.getOutputStream();
        List<Byte> bys = new ArrayList<>();

        byte[] bs = "撒打发斯蒂芬阿斯顿发放".getBytes("GB2312");
        String t,s = "";
        for (int i = 0; i < bs.length; i++) {
            int a = Integer.parseInt(Utils.byteToHex(new byte[]{bs[i]}), 16);
            t = (a - 0x80 - 0x20) + "";
            if(t.length() == 1){
                t = 0 + t;
            }
            s += t;
        }
        Logger.d("区位码：%s",s);


        //2281080
        String sz = "!0V0003A0015001002000000000000803000000000000000000000000000000000000000000000000B" + s +"C186642525028D186642525028E" + new String(jj,"GB2312");

        byte[] down = sz.getBytes("GB2312");
        Logger.d(Utils.byteToHex(down));

        outputStream.write(down);

        InputStream inputStream = socket.getInputStream();
        inputStream.read(bytes);
        Logger.d("服务器返回数据：" + new String(bytes,"GB2312"));
    }
}
