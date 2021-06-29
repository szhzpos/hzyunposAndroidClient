package com.wyc.cloudapp.activity.mobile.business;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.OnceCardData;
import com.wyc.cloudapp.bean.OnceCardInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

public class SelectOnceCardActivity extends AbstractMobileActivity {
    public static final int SELECT_ONCE_CARD = 0x000000cc;
    private OnceCardAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getString(R.string.select_once_card));

        initGoodsInfo();


        query();
    }

    private void initGoodsInfo(){
        final RecyclerView once_card_list = findViewById(R.id.once_card_list);
        mAdapter = new OnceCardAdapter();
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, GoodsInfoViewAdapter.MOBILE_SPAN_COUNT);
        once_card_list.setLayoutManager(gridLayoutManager);

        mAdapter.setSelectListener(new OnceCardAdapter.OnSelectFinishListener() {
            @Override
            public void onFinish() {
                final Intent intent = new Intent();

                setResult(RESULT_OK,intent);
                finish();
            }
        });
        once_card_list.setAdapter(mAdapter);
    }

    private void query(){
        final JSONObject object = new JSONObject();
        object.put("appid",getAppId());
        object.put("channel",1);
        HttpUtils.sendAsyncPost(getUrl() + InterfaceURL.ONCE_CARD,HttpRequest.generate_request_parm(object,getAppSecret())).enqueue(new ObjectCallback<OnceCardData>(OnceCardData.class,true) {
            @Override
            protected void onError(String msg) {
                MyDialog.toastMessage(msg);
            }

            @Override
            protected void onSuccessForResult(OnceCardData d, String hint) {
                List<OnceCardInfo> list = d.getCard();
                Logger.d(Arrays.toString(list.toArray()));
            }
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_select_once_card;
    }
    public static void start(Activity context){
        Intent intent = new Intent(context,SelectOnceCardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityForResult(intent,SELECT_ONCE_CARD);
    }

    private static class OnceCardAdapter extends AbstractDataAdapterForList<OnceCardInfo,OnceCardAdapter.MyViewHolder>{
        private OnSelectFinishListener mListener;
        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{

            public MyViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        }

        public interface OnSelectFinishListener{
            void onFinish();
        }

        public void setSelectListener(OnSelectFinishListener mListener) {
            this.mListener = mListener;
        }
    }
}