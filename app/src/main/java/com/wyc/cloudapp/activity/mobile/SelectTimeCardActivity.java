package com.wyc.cloudapp.activity.mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.bean.TimeCardData;
import com.wyc.cloudapp.bean.TimeCardInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SelectTimeCardActivity extends AbstractMobileActivity {
    public static final int SELECT_ONCE_CARD = 0x000000cc;
    private static final String ITEM_KEY = "I";
    private TimeCardAdapter mAdapter;
    private EditText mSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        setMiddleText(getString(R.string.select_once_card));

        initTimeCardInfo();

        initSearchContent();

        showActivity();
    }

    private void initTimeCardInfo(){
        final RecyclerView once_card_list = findViewById(R.id.once_card_list);
        mAdapter = new TimeCardAdapter(this);
        once_card_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        once_card_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),3));
        mAdapter.setSelectListener(this::setResult);
        once_card_list.setAdapter(mAdapter);
    }
    private void setResult(TimeCardInfo cardInfo){
        final Intent intent = new Intent();
        intent.putExtra(ITEM_KEY,cardInfo);
        setResult(RESULT_OK,intent);
        finish();
    }
    public static TimeCardInfo getTimeCardInfo(@NonNull Intent intent){
        return intent.getParcelableExtra(ITEM_KEY);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.search_once_card);
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                loadTimeCard();
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    loadTimeCard();
                }
            }
            return false;
        });
        mSearch = search;
    }

    private void loadTimeCard(){
        final JSONObject object = new JSONObject();
        object.put("appid",getAppId());
        object.put("channel",1);
        final String name = mSearch.getText().toString();
        if (Utils.isNotEmpty(name)){
            object.put("title",name);
        }
        HttpUtils.sendAsyncPost(getUrl() + InterfaceURL.ONCE_CARD,HttpRequest.generate_request_parm(object,getAppSecret()))
        .enqueue(new ObjectCallback<TimeCardData>(TimeCardData.class,true) {
            @Override
            protected void onError(String msg) {
                MyDialog.toastMessage(msg);
            }

            @Override
            protected void onSuccessForResult(TimeCardData d, String hint) {
                mAdapter.setDataForList(d.getCard());
            }
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_select_once_card;
    }
    public static void start(Fragment context){
        context.startActivityForResult(new Intent(context.getContext(), SelectTimeCardActivity.class),SELECT_ONCE_CARD);
    }
    public static void startForResult(Activity context,final ArrayList<TimeCardInfo> result){
        final Intent intent = new Intent(context, SelectTimeCardActivity.class);
        intent.putParcelableArrayListExtra("result",result);
        context.startActivityForResult(intent,SELECT_ONCE_CARD);
    }

    private void showActivity(){
        final Intent intent = getIntent();
        if (null != intent){
            List<TimeCardInfo> data = intent.getParcelableArrayListExtra("result");
            if (null !=data){
                mAdapter.setDataForList(data);
            }else loadTimeCard();
        }else loadTimeCard();
    }

    static class TimeCardAdapter extends AbstractDataAdapterForList<TimeCardInfo, TimeCardAdapter.MyViewHolder> implements View.OnClickListener{
        private OnSelectFinishListener mListener;
        private final SelectTimeCardActivity mContext;
        public TimeCardAdapter(SelectTimeCardActivity context){
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null){
                int id = Utils.getViewTagValue(v,-1);
                TimeCardInfo cardInfo = getItem(id);
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
            final TimeCardInfo cardInfo = mData.get(position);
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
            void onFinish(TimeCardInfo cardInfo);
        }

        public void setSelectListener(OnSelectFinishListener mListener) {
            this.mListener = mListener;
        }
    }
}