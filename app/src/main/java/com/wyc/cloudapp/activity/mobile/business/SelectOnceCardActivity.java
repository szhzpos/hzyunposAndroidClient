package com.wyc.cloudapp.activity.mobile.business;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.bean.OnceCardData;
import com.wyc.cloudapp.bean.OnceCardInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SelectOnceCardActivity extends AbstractMobileActivity {
    public static final int SELECT_ONCE_CARD = 0x000000cc;
    private static final String ITEM_KEY = "I";
    private OnceCardAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        setMiddleText(getString(R.string.select_once_card));

        initOnceCardInfo();


        loadOnceCard();
    }

    private void initOnceCardInfo(){
        final RecyclerView once_card_list = findViewById(R.id.once_card_list);
        mAdapter = new OnceCardAdapter(this);
        once_card_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        once_card_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),3));
        mAdapter.setSelectListener(cardInfo -> {
            final Intent intent = new Intent();
            intent.putExtra(ITEM_KEY,cardInfo);
            setResult(RESULT_OK,intent);
            finish();
        });
        once_card_list.setAdapter(mAdapter);
    }
    public static OnceCardInfo getOnceCardInfo(@NonNull Intent intent){
        return intent.getParcelableExtra(ITEM_KEY);
    }

    private void loadOnceCard(){
        final JSONObject object = new JSONObject();
        object.put("appid",getAppId());
        object.put("channel",1);
        HttpUtils.sendAsyncPost(getUrl() + InterfaceURL.ONCE_CARD,HttpRequest.generate_request_parm(object,getAppSecret()))
        .enqueue(new ObjectCallback<OnceCardData>(OnceCardData.class,true) {
            @Override
            protected void onError(String msg) {
                MyDialog.toastMessage(msg);
            }

            @Override
            protected void onSuccessForResult(OnceCardData d, String hint) {
                List<OnceCardInfo> list = d.getCard();
                mAdapter.setDataForList(list);
            }
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_select_once_card;
    }
    public static void start(Activity context){
        context.startActivityForResult(new Intent(context,SelectOnceCardActivity.class),SELECT_ONCE_CARD);
    }

    static class OnceCardAdapter extends AbstractDataAdapterForList<OnceCardInfo,OnceCardAdapter.MyViewHolder> implements View.OnClickListener{
        private OnSelectFinishListener mListener;
        private final Context mContext;
        public OnceCardAdapter(Context context){
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null){
                int id = Utils.getViewTagValue(v,-1);
                OnceCardInfo cardInfo = getItem(id);
                if (cardInfo != null)mListener.onFinish(cardInfo);
            }
        }

        static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
            @BindView(R.id.img)
            ImageView img;
            @BindView(R.id.id_tv)
            TextView id_tv;
            @BindView(R.id.name_tv)
            TextView name_tv;
            @BindView(R.id.available_times_tv)
            TextView available_times_tv;
            @BindView(R.id.price_tv)
            TextView price_tv;
            public MyViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this,itemView);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = View.inflate(mContext,R.layout.mobile_once_card_info,null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.once_card_item_height)));
            view.setOnClickListener(this);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final OnceCardInfo cardInfo = mData.get(position);
            final String img_url = cardInfo.getImg();
            if (Utils.isNotEmpty(img_url)){
                HttpUtils.sendAsyncGet(img_url).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call,@NonNull IOException e) {
                        MyDialog.ToastMessageInMainThread(e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call,@NonNull Response response) throws IOException {
                        try (InputStream inputStream = response.body().byteStream()){
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            holder.img.post(()-> holder.img.setImageBitmap(bitmap));
                        }
                    }
                });
            }else
                holder.img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));

            holder.id_tv.setText(String.format(Locale.CHINA,"%d、",position + 1));
            holder.name_tv.setText(cardInfo.getTitle());
            if (cardInfo.getAvailable_limit() == 1)
                holder.available_times_tv.setText(String.format(Locale.CHINA,"%s次",cardInfo.getAvailable_limits()));
            else
                holder.available_times_tv.setText(String.format(Locale.CHINA,"%d次",cardInfo.getAvailable()));
            holder.price_tv.setText(String.format(Locale.CHINA,"￥%.2f",cardInfo.getPrice()));
            holder.itemView.setTag(position);
        }

        public interface OnSelectFinishListener{
            void onFinish(OnceCardInfo cardInfo);
        }

        public void setSelectListener(OnSelectFinishListener mListener) {
            this.mListener = mListener;
        }
    }
}