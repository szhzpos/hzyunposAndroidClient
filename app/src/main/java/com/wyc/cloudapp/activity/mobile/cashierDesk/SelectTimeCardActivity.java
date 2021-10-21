package com.wyc.cloudapp.activity.mobile.cashierDesk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractSelectAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardData;
import com.wyc.cloudapp.bean.TimeCardInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.data.viewModel.TimeCardViewModel;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
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

public final class SelectTimeCardActivity extends AbstractSelectActivity<TimeCardInfo, SelectTimeCardActivity.TimeCardAdapter> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        setMiddleText(getString(R.string.select_once_card));

    }

    @Override
    protected TimeCardAdapter getAdapter() {
        return new TimeCardAdapter();
    }

    public static void startWithFragment(Fragment context, final ArrayList<TimeCardInfo> result){
        context.startActivityForResult(new Intent(context.getContext(), SelectTimeCardActivity.class).putParcelableArrayListExtra("result",result), SELECT_ITEM);
    }

    public static void startForResult(Activity context, final ArrayList<TimeCardInfo> result){
        final Intent intent = new Intent(context,SelectTimeCardActivity.class);
        intent.putParcelableArrayListExtra("result",result);
        context.startActivityForResult(intent, SELECT_ITEM);
    }

    @Override
    protected void loadData(String c) {
        final MutableLiveData<List<TimeCardInfo>> liveData = new ViewModelProvider(this).get(TimeCardViewModel.class).refresh(this,c);
        if (!liveData.hasActiveObservers()){
            liveData.observe(this, this::setData);
        }
    }

    static class TimeCardAdapter extends AbstractSelectAdapter<TimeCardInfo, TimeCardAdapter.MyViewHolder> implements View.OnClickListener{
        public TimeCardAdapter(){
        }
        @Override
        public void onClick(View v) {
            if (hasListener()){
                int id = Utils.getViewTagValue(v,-1);
                TimeCardInfo cardInfo = getItem(id);
                invoke(cardInfo);
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
            final View view = View.inflate(CustomApplication.self(),R.layout.mobile_once_card_info,null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) CustomApplication.self().getResources().getDimension(R.dimen.once_card_item_height)));
            view.setOnClickListener(this);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final TimeCardInfo cardInfo = mData.get(position);
            final String img_url = cardInfo.getImg();
            if (Utils.isNotEmpty(img_url)){
                Glide.with(holder.img).load(img_url).into(holder.img);
            }else
                Glide.with(holder.img).load(R.drawable.nodish).into(holder.img);

            holder.id_tv.setText(String.format(Locale.CHINA,"%d、",position + 1));
            holder.name_tv.setText(cardInfo.getTitle());
            if (cardInfo.getAvailable_limit() == 1)
                holder.available_times_tv.setText(String.format(Locale.CHINA,"%s次",cardInfo.getAvailable_limits()));
            else
                holder.available_times_tv.setText(String.format(Locale.CHINA,"%d次",cardInfo.getAvailable()));
            holder.price_tv.setText(String.format(Locale.CHINA,"￥%.2f",cardInfo.getPrice()));
            holder.itemView.setTag(position);
        }
    }
}