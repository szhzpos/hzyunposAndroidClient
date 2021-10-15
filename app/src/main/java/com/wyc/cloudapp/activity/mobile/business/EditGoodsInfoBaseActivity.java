package com.wyc.cloudapp.activity.mobile.business;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.client.android.CaptureActivity;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.AuxiliaryBarcodeAdapter;
import com.wyc.cloudapp.adapter.business.MultiUnitAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.AuxiliaryBarcode;
import com.wyc.cloudapp.bean.BarcodeOnlyCodeInfo;
import com.wyc.cloudapp.bean.GoodsCategory;
import com.wyc.cloudapp.bean.MultiUnitInfo;
import com.wyc.cloudapp.bean.Supplier;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.viewModel.BarcodeOnlyCodeViewModel;
import com.wyc.cloudapp.data.viewModel.SupplierViewModel;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TakePhotoPopWin;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.msg.ReloadMsg;
import com.wyc.cloudapp.utils.FileUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.wyc.cloudapp.constants.ScanCallbackCode.CODE_REQUEST_CODE;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.activity.mobile.business
 * @ClassName: EditGoodInfoBaseActivity
 * @Description: 商品编辑基类
 * @Author: wyc
 * @CreateDate: 2021-09-23 10:30
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-09-23 10:30
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
abstract public class EditGoodsInfoBaseActivity extends AbstractEditArchiveActivity {
    private static final String DEFAULT_SUPPLIER_CODE = "0000";
    private static final int REQUEST_CAPTURE_IMG = 100;
    private static final int REQ_CROP = 108;
    private static final int CHOOSE_PHOTO = 110;
    protected static final String BARCODEID_KEY = "BCK";
    protected static final String CATEGORY_KEY = "CK";


    private String mBarcodeId;
    private String mBarcode;
    private EditText mBarcodeEt,mGoodsAttrEt,mMeteringEt;
    private TextView mBrandTv,mSupplierTv, mCategoryTv,mUnitTv;
    private JSONArray mUnitList,mCategoryList,mBrandList,mAttrList,meteringList;
    private List<Supplier> mSupplierList;

    @BindView(R.id.a_name_et)
    EditText mNameEt;
    @BindView(R.id.a_retail_price_et)
    EditText mRetailPriceEt;
    @BindView(R.id.a_item_no_et)
    EditText mItemIdEt;
    @BindView(R.id.a_vip_price_et)
    EditText mVipPriceEt;
    @BindView(R.id.a_pur_price_et)
    EditText mPurPriceEt;
    @BindView(R.id.pf_price_et)
    EditText pf_price_et;
    @BindView(R.id.place_et)
    EditText place_et;
    @BindView(R.id.spec_tv)
    TextView spec_tv;
    @BindView(R.id.hz_method_tv)
    TextView hz_method_tv;
    @BindView(R.id.ly_ratio_tv)
    TextView ly_ratio_tv;

    @BindView(R.id.goods_img)
    ImageView goods_img;
    @BindView(R.id._tab_layout)
    TabLayout _tab_layout;

    @BindView(R.id.basic)
    ViewGroup mBasicLayout;
    @BindView(R.id.auxiliary)
    ViewGroup mAuxiliaryLayout;
    @BindView(R.id.unit_price_layout)
    ViewGroup mUnitPriceLayout;

    private Uri mImageUri;

    private boolean isModify;
    private JSONObject mGoodsObj;

    private AuxiliaryBarcodeAdapter mAuxiliaryBarcodeAdapter;
    private MultiUnitAdapter mMultiUnitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarcodeId = getIntent().getStringExtra(BARCODEID_KEY);
        setMiddleText(getString((isModify = Utils.isNotEmpty(mBarcodeId)) ? R.string.modify_goods : R.string.add_goods));
        Logger.d("mBarcodeId:%s",mBarcodeId);

        initUnit();
        initCategory();
        initBrand();
        initBarcode();
        initSupplier();
        initGoodsAttrAndMetering();
        initTabLayout();

        //查询商品辅助档案
        getGoodsBase();
        getGoodsInfoByBarcode();
        getSupplier();
        getOnlycodeAndBarcode();

        if (isModify){
            getGoodsByBarcodeId();
        }
        initAuxiliary();
        initMultiUnit();
    }

    private void initAuxiliary(){
        final RecyclerView auxiliary_list = findViewById(R.id.auxiliary_list);
        if (null != auxiliary_list){
            auxiliary_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            auxiliary_list.setAdapter(mAuxiliaryBarcodeAdapter = new AuxiliaryBarcodeAdapter(auxiliary_list));
            auxiliary_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),1));

            CustomApplication.execute(()-> mAuxiliaryBarcodeAdapter.setDataForList(getAuxiliaryBarcodeById()));
        }
    }
    private List<AuxiliaryBarcode> getAuxiliaryBarcodeById(){
        return SQLiteHelper.getBeans(AuxiliaryBarcode.class,mBarcodeId == null ? "" : mBarcodeId,"1");
    }

    private void initMultiUnit(){
        final RecyclerView unit_price_list = findViewById(R.id.unit_price_list);
        if (null != unit_price_list){
            unit_price_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            unit_price_list.setAdapter(mMultiUnitAdapter = new MultiUnitAdapter(unit_price_list));
            unit_price_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),1));

            CustomApplication.execute(()-> mMultiUnitAdapter.setDataForList(getMultiUnit()));
        }
    }
    private List<MultiUnitInfo> getMultiUnit(){
        List<MultiUnitInfo> multiUnitInfoList;
        if (mBarcodeId != null)
            multiUnitInfoList = SQLiteHelper.getBeans(MultiUnitInfo.class,Utils.getNullStringAsEmpty(mGoodsObj,"only_coding"),"1");
        else {
            multiUnitInfoList = new ArrayList<>();
        }
        return multiUnitInfoList;
    }

    private void initTabLayout(){
        _tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        if (mBasicLayout.getVisibility() == View.GONE){
                            showView(mBasicLayout);

                            hideView(mAuxiliaryLayout);
                            hideView(mUnitPriceLayout);
                        }
                        break;
                    case  1:
                        if (mAuxiliaryLayout.getVisibility() == View.GONE){
                            showView(mAuxiliaryLayout);
                            hideView(mBasicLayout);
                            hideView(mUnitPriceLayout);
                        }
                        break;
                    case 2:
                        if (mUnitPriceLayout.getVisibility() == View.GONE){
                            showMultiUnit();

                            showView(mUnitPriceLayout);
                            hideView(mBasicLayout);
                            hideView(mAuxiliaryLayout);
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        _tab_layout.addTab(_tab_layout.newTab().setText(getString(R.string.basic_info)));
        _tab_layout.addTab(_tab_layout.newTab().setText(getString(R.string.auxiliary_info)));
        _tab_layout.addTab(_tab_layout.newTab().setText(getString(R.string.unit_price_info)));
        _tab_layout.setVisibility(View.VISIBLE);
    }

    private void showMultiUnit(){
        final List<MultiUnitInfo> multiUnitInfoList = mMultiUnitAdapter.getOriginalData();
        MultiUnitInfo multiUnitInfo ;
        if (multiUnitInfoList.size() > 1){
            multiUnitInfo = multiUnitInfoList.get(0);
        }else {
            multiUnitInfo = new MultiUnitInfo();
            multiUnitInfoList.add(0,multiUnitInfo);
        }
        multiUnitInfo.setBarcode(mBarcode);
        multiUnitInfo.setUnit_name(mUnitTv.getText().toString());

        double value = 0.0;
        try {
            value = Double.parseDouble(mRetailPriceEt.getText().toString());
        }catch (NumberFormatException ignored){
        }
        multiUnitInfo.setRetail_price(value);

        try {
            value = Double.parseDouble(pf_price_et.getText().toString());
        }catch (NumberFormatException ignored){
        }
        multiUnitInfo.setTrade_price(value);

        try {
            value = Double.parseDouble(mVipPriceEt.getText().toString());
        }catch (NumberFormatException ignored){
        }
        multiUnitInfo.setYh_price(value);
        mMultiUnitAdapter.notifyDataSetChanged();
    }

    private static class Hide implements Animator.AnimatorListener {
        private final View mAnimView;
        public Hide(@NonNull View view){
            mAnimView = view;
        }
        @Override
        public void onAnimationEnd(Animator animation, boolean isReverse) {

        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimView.setVisibility(View.GONE);
            mAnimView.animate().setListener(null);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mAnimView.setVisibility(View.GONE);
            mAnimView.animate().setListener(null);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
    private void hideView(@NonNull View view){
        if (view.getVisibility() == View.VISIBLE){
            hideInputMethod();
            view.animate().alpha(0).scaleY(0).scaleX(0).setDuration(300).setListener(new Hide(view)).start();
        }
    }
    private void showView(@NonNull View view){
        view.setVisibility(View.VISIBLE);
        view.animate().setListener(null).cancel();
        view.animate().alpha(1f).scaleY(1f).scaleX(1f).setDuration(300).start();
    }

    @OnClick(R.id.clear_img_btn)
    void clearPic(){
        mImageUri = null;
        goods_img.setImageDrawable(getDrawable(R.drawable.nodish));
    }

    @OnClick(R.id.add_img_btn)
    void capturePic(){
        final TakePhotoPopWin takePhotoPopWin = new TakePhotoPopWin(this, v -> {
            int id = v.getId();
            if (id == R.id.btn_take_photo){
                XXPermissions.with(this)
                        .permission(Permission.CAMERA)
                        .request(new OnPermissionCallback() {
                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                takePic();
                            }

                            @Override
                            public void onDenied(List<String> permissions, boolean never) {
                                if (never){
                                    MyDialog.toastMessage(getString(R.string.camera_permission_hint));
                                }
                            }
                        });
            }else if (id == R.id.btn_pick_photo){
                openAlbum();
            }
        });
        takePhotoPopWin.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    private void takePic(){
        mImageUri = FileUtils.createCaptureImageFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageUri);
        try {
            startActivityForResult(intent, REQUEST_CAPTURE_IMG);
        }catch (Exception e){
            e.printStackTrace();
            MyDialog.toastMessage(e.getMessage());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode == RESULT_OK ){
            if (requestCode == CODE_REQUEST_CODE) {
                final String _code = intent.getStringExtra(CaptureActivity.CALLBACK_CODE);
                mBarcodeEt.setText(_code);
                getGoodsInfoByBarcode();
            }else if (requestCode == REQUEST_CAPTURE_IMG) {
                crop();
            }else if (requestCode == REQ_CROP){
                try(InputStream inputStream = getContentResolver().openInputStream(mImageUri)) {
                    goods_img.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                    MyDialog.toastMessage(e.getLocalizedMessage());
                }
            }else if (requestCode == CHOOSE_PHOTO){
                mImageUri = intent.getData();
                crop();
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    private void crop() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(mImageUri, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 1200);
        intent.putExtra("outputY", 1200);
        intent.putExtra("scale", false);
        intent.putExtra("return-data", false);

        Uri imgCropUri = FileUtils.createCropImageFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri);

        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", false);

        mImageUri = imgCropUri;
        startActivityForResult(intent, REQ_CROP);
    }


    private void openAlbum() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.setDataAndType( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        openAlbumIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(openAlbumIntent, CHOOSE_PHOTO);//打开相册
    }

    private void getSupplier(){
        final MutableLiveData<List<Supplier>> liveData = new ViewModelProvider(this).get(SupplierViewModel.class).getCurrentModel();
        if (!liveData.hasActiveObservers())liveData.observe(this, suppliers -> {
            mSupplierList = suppliers;
            setDefaultSupplier();
        });
    }
    private void initSupplier(){
        final TextView supplier_et = findViewById(R.id.a_supplier_et);
        supplier_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setHz_method();
            }
        });
        supplier_et.setOnClickListener(v -> {
            final String sup = getString(R.string.a_supplier_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setData(Utils.JsondeepCopy(parse_supplier_info()),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    supplier_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));//注意调用位置。后续业务会在TextWatcher对象里面获取tag值，所以必须先设置tag
                    supplier_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    mNameEt.requestFocus();
                }
            });
        });
        mSupplierTv = supplier_et;
    }

    private void setHz_method(){
        final Supplier supplier = getSupplierById(Utils.getViewTagValue(mSupplierTv,""));
        if (null != supplier){
            final String hz_method = String.valueOf(supplier.getHz_method());
            hz_method_tv.setTag(hz_method);
            hz_method_tv.setText(supplier.getHz_method_name());
            setLyRatio("1".equals(hz_method) ? View.VISIBLE : View.INVISIBLE);
        }
    }
    private Supplier getDefaultSupplier(){
        if (null == mSupplierList)return null;
        for (Supplier sup : mSupplierList){
            if (DEFAULT_SUPPLIER_CODE.equals(sup.getCs_code())){
                return sup;
            }
        }
        return null;
    }

    private void setDefaultSupplier(){
        if (null != mSupplierTv){
            Supplier object;
            if (isModify){
                object = getSupplierById(Utils.getNullStringAsEmpty(mGoodsObj,"gs_id"));
            }else {
                object = getDefaultSupplier();
            }
            if ( null != object){
                mSupplierTv.setTag(object.getGs_id());
                mSupplierTv.setText(object.getGs_name());
            }
        }
    }

    private JSONArray parse_supplier_info(){
        final JSONArray array  = new JSONArray();
        final List<Supplier> suppliers = mSupplierList;
        if (suppliers != null){
            JSONObject object;
            for (Supplier sup : mSupplierList){
                final int id = Integer.parseInt(sup.getGs_id());
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,sup.getCs_name());
                array.add(object);
            }
        }
        return array;
    }

    private Supplier getSupplierById(final String gs_id){
        if (null == mSupplierList)return null;
        for (Supplier sup : mSupplierList){
            if (sup.getGs_id().equals(gs_id)){
                return sup;
            }
        }
        return null;
    }

    private void getGoodsInfoByBarcode(){
        if (!Utils.isNotEmpty(mBarcode))return;

        final CustomProgressDialog progressDialog = new CustomProgressDialog(this);
        final JEventLoop jEventLoop = new JEventLoop();
        progressDialog.setMessage("正在加载商品信息...").setCancel(false).show();
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("itemcode",mBarcode);
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost("http://adm.hzyunpos.com/api/getgoods/get_goods",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()-> MyDialog.ToastMessage("查询商品信息错误:" + retJson.getString("info"), getWindow()));
                    break;
                case 1:
                    runOnUiThread(()-> showGoodsInfo(JSONObject.parseObject(retJson.getString("info"))));
                    break;
            }
            jEventLoop.done(0);
        });
        jEventLoop.exec();
        progressDialog.dismiss();
    }
    private void showGoodsInfo(final JSONObject info){
        final String unit_sz = Utils.getNullStringAsEmpty(info,"unit");
        if (!unit_sz.isEmpty())mUnitTv.setText(unit_sz);
        mRetailPriceEt.setText(String.format(Locale.CHINA,"%.2f",info.getDoubleValue("price")));
        mBarcodeEt.setText(mBarcode);
        final String name = Utils.getNullStringAsEmpty(info,"name");
        if (name.isEmpty()){
            mNameEt.requestFocus();
        }else {
            mNameEt.setText(name);
            mRetailPriceEt.requestFocus();
        }
    }
    private void initUnit(){
        final TextView unit_et = findViewById(R.id.a_unit_et);
        unit_et.setOnClickListener(v -> showUnit(mUnitTv));
        mUnitTv = unit_et;
        setDefaultUnit();
    }
    public void showUnit(final TextView tv){
        final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,getString(R.string.unit_sz));
        treeListDialog.setData(Utils.JsondeepCopy(mUnitList),null,true);
        CustomApplication.runInMainThread(()->{
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                tv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
            }else {
                if (tv instanceof EditText){
                    tv.postDelayed(()-> Utils.setFocus(this,(EditText)tv),300);
                }
            }
        });
    }
    private void setDefaultUnit(){
        if (null != mUnitTv){
            mUnitTv.setText("未定");
            mUnitTv.setTag("");
        }
    }

    private void initCategory(){
        final TextView category_et = findViewById(R.id.a_category_et);
        category_et.setOnClickListener(v -> {
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,getString(R.string.d_category_sz));
            treeListDialog.setData(Utils.JsondeepCopy(mCategoryList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    category_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    category_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                    getOnlycodeAndBarcode();
                }
            });
        });
        mCategoryTv = category_et;
    }

    private void getOnlycodeAndBarcode(){//修改状态下不刷新条码和货号20210922
        if (!isModify){
            final BarcodeOnlyCodeViewModel model = new ViewModelProvider(this).get(BarcodeOnlyCodeViewModel.class);
            if (!model.hasObserver())model.AddObserver(this, this::setBarcodeAndItemId);
            model.refresh(Utils.getViewTagValue(mCategoryTv,"7223"),Utils.getViewTagValue(mGoodsAttrEt,"1"));
        }
    }

    private void setBarcodeAndItemId(final @NonNull BarcodeOnlyCodeInfo info){
        final String only_coding = info.getOnly_coding();
        if (mItemIdEt != null)mItemIdEt.setText(only_coding);
        if (mBarcodeEt != null){
            if ("2".equals(Utils.getViewTagValue(mGoodsAttrEt,"1"))){
                mBarcodeEt.setText(only_coding);
            }else{
                if(mBarcode == null || mBarcode.isEmpty()){
                    mBarcodeEt.setText(info.getBarcode());
                    mBarcodeEt.requestFocus();
                    mBarcodeEt.selectAll();
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initBarcode(){
        final EditText barcode_et = findViewById(R.id.a_barcode_et);
        if (barcode_et != null){
            barcode_et.setText(mBarcode);
            barcode_et.requestFocus();
            barcode_et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mBarcode = s.toString();
                }
            });
            barcode_et.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    final float dx = motionEvent.getX();
                    final int w = barcode_et.getWidth();
                    if (dx > (w - barcode_et.getCompoundPaddingRight())) {
                        barcode_et.requestFocus();
                        final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                        startActivityForResult(intent, CODE_REQUEST_CODE);
                    }
                }
                return false;
            });
            mBarcodeEt = barcode_et;
        }
    }

    private void setLyRatio(int visibility){
        //设置联营扣率
        final TextView ly_ratio_label = findViewById(R.id.ly_ratio_label);
        ly_ratio_label.setVisibility(visibility);
        ly_ratio_tv.setVisibility(visibility);
        if (visibility == View.VISIBLE){
            ly_ratio_tv.setText(Utils.getNullStringAsEmpty(mGoodsObj,"cash_flow_ratio"));
        }else ly_ratio_tv.setText("");
    }

    private void initBrand(){
        final TextView brand = findViewById(R.id.brand_et);
        brand.setOnClickListener(v -> {
            final String sup = getString(R.string.brand_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setData(Utils.JsondeepCopy(mBrandList),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    brand.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    brand.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                }
            });
        });
        mBrandTv = brand;
    }

    private void getGoodsBase(){
        Observable.create((ObservableOnSubscribe<JSONObject>) emitter -> {
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(getUrl() + "/api/goods_set/get_bases",sz_param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                if (HttpUtils.checkBusinessSuccess(info_obj)){
                    emitter.onNext(info_obj.getJSONObject("data"));
                }else emitter.onError(new Exception(info_obj.getString("info")));
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(data -> {
            mUnitList = parse_unit_info(Utils.getNullObjectAsEmptyJsonArray(data,"units"));
            final JSONArray categorys = new JSONArray();
            parse_category_info(Utils.getNullObjectAsEmptyJsonArray(data,"category"),null,0,categorys);
            mCategoryList = categorys;
            setDefaultCategory();

            mBrandList = parse_brand_info(Utils.getNullObjectAsEmptyJsonArray(data,"brand"));
            setDefaultBrand();
        }, throwable -> MyDialog.ToastMessage("查询商品基本信息错误:" + throwable.getMessage(), getWindow()));
    }

    private JSONObject getDefaultCategory(){
        JSONObject object = null;
        if (mCategoryList != null) {
            final GoodsCategory category = getIntent().getParcelableExtra(CATEGORY_KEY);
            if (category != null){
                object = new JSONObject();
                object.put(TreeListBaseAdapter.COL_ID,category.getCategory_id());
                object.put(TreeListBaseAdapter.COL_NAME,category.getName());
            }else
                for (int i = 0,size = mCategoryList.size();i < size;i ++){
                    object  = mCategoryList.getJSONObject(i);
                    if ("00".equals(object.getString("category_code"))){
                        return object;
                    }
                }
        }
        return object;
    }

    private void setDefaultCategory(){
        if (!isModify ){
            final JSONObject object = getDefaultCategory();
            if (null != object && mCategoryTv != null){
                mCategoryTv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                mCategoryTv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
            }
        }
    }

    private JSONArray parse_brand_info(final JSONArray brands){
        final JSONArray array  = new JSONArray();
        if (brands != null){
            JSONObject object,tmp;
            for (int i = 0,size = brands.size();i < size;i++){
                tmp = brands.getJSONObject(i);

                final String id = Utils.getNullStringAsEmpty(tmp,"gb_id"),name = Utils.getNullStringAsEmpty(tmp,"gb_name");
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,name);
                array.add(object);
            }
        }
        return array;
    }

    private JSONObject getDefaultBrand(){
        if (mBrandList != null && !mBrandList.isEmpty()){
            return mBrandList.getJSONObject(0);
        }
        return null;
    }

    private void setDefaultBrand(){
        final JSONObject object = getDefaultBrand();
        if (!isModify && null != object && mBrandTv != null){
            mBrandTv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
            mBrandTv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
        }
    }

    private JSONArray parse_unit_info(final JSONArray units){
        final JSONArray array  = new JSONArray();
        if (units != null){
            JSONObject object,tmp;
            for (int i = 0,size = units.size();i < size;i++){
                tmp = units.getJSONObject(i);

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,Utils.getNullStringAsEmpty(tmp,"unit_id"));
                object.put(TreeListBaseAdapter.COL_NAME,Utils.getNullStringAsEmpty(tmp,"unit_name"));
                array.add(object);
            }
        }
        return array;
    }

    private void parse_category_info(final JSONArray category_jsons,final JSONObject parent,int level,final JSONArray categorys) {
        JSONObject item,category_json;
        JSONArray kids,childs;
        for (int i = 0, length = category_jsons.size(); i < length; i++) {
            category_json = category_jsons.getJSONObject(i);

            item = new JSONObject();
            item.put("level",level);
            item.put("unfold",false);
            item.put("isSel",false);
            item.put(TreeListBaseAdapter.COL_ID,category_json.getString("category_id"));
            item.put("category_code",category_json.getString("category_code"));
            item.put(TreeListBaseAdapter.COL_NAME,category_json.getString("name"));

            item.put("kids",new JSONArray());

            if (parent != null){
                item.put("p_ref",parent);
                kids = parent.getJSONArray("kids");
                kids.add(item);
            }

            if (category_json.containsKey("childs")) {
                childs = (JSONArray) category_json.remove("childs");
                if (childs != null && childs.size() != 0) {
                    parse_category_info(childs,item,level + 1, null);
                }
            }
            if (categorys != null)categorys.add(item);
        }
    }

    private void initGoodsAttrAndMetering(){
        final EditText goods_attr_et = findViewById(R.id.a_goods_attr_et),metering_et = findViewById(R.id.a_metering_et);
        setAttrList();
        setMeteringList();
        goods_attr_et.setOnClickListener(v -> {
            final String attr = getString(R.string.a_goods_attr_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,attr.substring(0,attr.length() - 1));
            treeListDialog.setData(mAttrList,null,true);

            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    final String id = object.getString(TreeListBaseAdapter.COL_ID);
                    goods_attr_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    goods_attr_et.setTag(id);
                    if ("2".equals(id)){
                        final JSONObject obj = getMeteringById("0");
                        if (null != obj){
                            metering_et.setText(obj.getString(TreeListBaseAdapter.COL_NAME));
                            metering_et.setTag(obj.getString(TreeListBaseAdapter.COL_ID));
                            metering_et.setVisibility(View.VISIBLE);
                        }
                    }else{
                        metering_et.setVisibility(View.GONE);
                    }
                    getOnlycodeAndBarcode();

                    mBarcodeEt.requestFocus();
                }
            });
        });
        goods_attr_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(goods_attr_et);
        });
        final JSONObject attr = getAttrById("1");
        if (null != attr){
            goods_attr_et.setText(attr.getString(TreeListBaseAdapter.COL_NAME));
            goods_attr_et.setTag(attr.getString(TreeListBaseAdapter.COL_ID));
        }
        mGoodsAttrEt = goods_attr_et;

        metering_et.setOnClickListener(v -> {
            final String attr_sz = getString(R.string.a_goods_attr_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,attr_sz.substring(0,attr_sz.length() - 1));
            treeListDialog.setData(meteringList,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    metering_et.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    metering_et.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                    mBarcodeEt.requestFocus();
                }
            });
        });
        metering_et.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)v.callOnClick();
            Utils.hideKeyBoard(metering_et);
        });
        mMeteringEt = metering_et;
    }
    private void setAttrList(){
        mAttrList = new JSONArray();
        final JSONObject obj = new JSONObject();
        obj.put("level",0);
        obj.put("unfold",false);
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"1");
        obj.put(TreeListBaseAdapter.COL_NAME,"普通商品");
        mAttrList.add(Utils.JsondeepCopy(obj));
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"2");
        obj.put(TreeListBaseAdapter.COL_NAME,"称重商品");
        mAttrList.add(obj);
    }
    private void setMeteringList(){
        meteringList = new JSONArray();
        final JSONObject obj = new JSONObject();
        obj.put("level",0);
        obj.put("unfold",false);
        obj.put("isSel",true);
        obj.put(TreeListBaseAdapter.COL_ID,"0");
        obj.put(TreeListBaseAdapter.COL_NAME,"计重");
        meteringList.add(Utils.JsondeepCopy(obj));
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"1");
        obj.put(TreeListBaseAdapter.COL_NAME,"计件");
        meteringList.add(Utils.JsondeepCopy(obj));
        obj.put("isSel",false);
        obj.put(TreeListBaseAdapter.COL_ID,"2");
        obj.put(TreeListBaseAdapter.COL_NAME,"定重");
        meteringList.add(obj);
    }
    private JSONObject getAttrById(final String id){
        if (mAttrList != null){
            for (int i = 0,size = mAttrList.size();i < size;i ++){
                final JSONObject object = mAttrList.getJSONObject(i);
                if (Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID).equals(id)){
                    return object;
                }
            }
        }
        return null;
    }
    private JSONObject getMeteringById(final String id){
        if (meteringList != null){
            for (int i = 0,size = meteringList.size();i < size;i ++){
                final JSONObject object = meteringList.getJSONObject(i);
                if (Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID).equals(id)){
                    return object;
                }
            }
        }
        return null;
    }

    @Override
    protected void sure() {
        addGoods(generateParameter(),false);
    }

    private JSONObject generateParameter(){
        final JSONObject data = new JSONObject();

        if (!mAuxiliaryBarcodeAdapter.isValid() || !mMultiUnitAdapter.isValid())return data;

        final String barcode = mBarcode,name = mNameEt.getText().toString(),category = mCategoryTv.getText().toString(),unit = mUnitTv.getText().toString(),
                only_coding = mItemIdEt.getText().toString(),supplier = mSupplierTv.getText().toString();
        if (barcode == null || barcode.isEmpty()){
            mBarcodeEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.barcode_sz)), getWindow());
            return data;
        }
        if (name.isEmpty()){
            mNameEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.g_name_sz)), getWindow());
            return data;
        }
        if (supplier.isEmpty()){
            mSupplierTv.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.supplier_setting)), getWindow());
            return data;
        }
        if (category.isEmpty()){
            mCategoryTv.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.d_category_sz)), getWindow());
            return data;
        }
        if (unit.isEmpty()){
            mUnitTv.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.unit_sz)), getWindow());
            return data;
        }
        if (only_coding.isEmpty()){
            mItemIdEt.requestFocus();
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.item_no_sz)), getWindow());
            return data;
        }

        data.put("goods_id",Utils.getNullStringAsEmpty(mGoodsObj,"goods_id"));
        data.put("barcode_id",Utils.getNullStringAsEmpty(mGoodsObj,"barcode_id"));
        data.put("barcode",barcode);
        data.put("attr_id",0);
        data.put("type",isModify ? 2 : 1);

        data.put("buying_price",mPurPriceEt.getText().toString());
        data.put("retail_price",mRetailPriceEt.getText().toString());
        data.put("yh_mode",Utils.getNotKeyAsNumberDefault(mGoodsObj,"yh_mode",0));
        data.put("yh_price",mVipPriceEt.getText().toString());
        data.put("cost_price",0);
        data.put("ps_price",0);
        data.put("trade_price",pf_price_et.getText().toString());

        data.put("category_id",Utils.getViewTagValue(mCategoryTv,""));
        data.put("goods_title",name);
        data.put("gs_id",Utils.getViewTagValue(mSupplierTv,""));
        data.put("spec_id",Utils.getViewTagValue(mGoodsAttrEt,"1"));
        data.put("only_coding",only_coding);
        data.put("unit_name",unit);
        data.put("spec_str",spec_tv.getText().toString());
        data.put("current_goods", getCurPriceFlag());
        data.put("gb_id",Utils.getViewTagValue(mBrandTv,""));
        data.put("cash_flow_mode",Utils.getViewTagValue(hz_method_tv,""));
        data.put("cash_flow_ratio",ly_ratio_tv.getText().toString());
        data.put("origin",place_et.getText().toString());
        data.put("pic_id",Utils.getNotKeyAsNumberDefault(mGoodsObj,"goods_img",0));

        data.put("appid",getAppId());

        final List<MultiUnitInfo> d = mMultiUnitAdapter.getSubmitData();
        if (!d.isEmpty())
            data.put("goods",JSONObject.parseArray(JSON.toJSONString(d)));
        data.put("fuzhu",JSONObject.parseArray(JSON.toJSONString(mAuxiliaryBarcodeAdapter.getValidData())));
        return data;
    }

    private int getCurPriceFlag(){
        final RadioButton yes = findViewById(R.id.y);
        return yes.isChecked() ? 1 : 0;
    }
    private void setCurPriceFlag(boolean flag){
        RadioButton rb;
        if (flag){
            rb = findViewById(R.id.y);
        }else {
            rb = findViewById(R.id.n);
        }
        rb.setChecked(true);
    }

    private String getMetering(){
        if ("1".equals(Utils.getViewTagValue(mGoodsAttrEt,"1"))){
            return "0";
        }else
            return Utils.getViewTagValue(mMeteringEt,"1");
    }

    private void addGoods(final JSONObject data,boolean reset){
        if (data.isEmpty())return;

        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,"正在上传商品信息...");
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        CustomApplication.execute(()->{

            HttpRequest httpRequest = new HttpRequest();
            if (null != mImageUri){
                //上传图片
                try {
                    JSONObject ret_json = httpRequest.uploadFileForPost(getUrl() + "/api/imgupload/index",new File(new URI(mImageUri.toString())));
                    if (HttpUtils.checkRequestSuccess(ret_json)){
                        ret_json = JSONObject.parseObject(ret_json.getString("info"));
                        if (HttpUtils.checkBusinessSuccess(ret_json)){
                            ret_json = ret_json.getJSONObject("data");
                            data.put("pic_id",ret_json.getString("imgs_id"));
                        }else throw new Exception(ret_json.getString("info"));
                    }else throw new Exception(ret_json.getString("info"));
                } catch (Exception e) {
                    e.printStackTrace();
                    loop.done(0);
                    err.append(e);
                    return;
                }
            }

            data.put("operation_mode",1);//判断后台操作员权限
            data.put("pt_user_id",getPtUserId());

            Logger.d_json(data);

            final String param = HttpRequest.generate_request_parm(data,getAppSecret());

            final JSONObject retJson = httpRequest.sendPost(getUrl() +"/api/goods_set/goods_sets",param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    loop.done(0);
                    err.append(retJson.getString("info"));
                    break;
                case 1:
                    try {
                        final JSONObject info  = JSON.parseObject(retJson.getString("info"));
                        switch (Utils.getNullStringAsEmpty(info,"status")){
                            case "n":
                                throw new JSONException(info.getString("info"));
                            case "y":
                                if (getNewGoodsAndSave(httpRequest,err)){
                                    loop.done(1);
                                }else
                                    loop.done(0);
                                break;
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        loop.done(0);
                        err.append(e.getMessage());
                    }
                    break;
            }
        });
        int code = loop.exec();
        progressDialog.dismiss();
        if (code != 1){
            MyDialog.displayErrorMessage(this, "上传商品错误:" + err);
        }else {
            if (reset)
                reset();
            else{
                EventBus.getDefault().post(new ReloadMsg());
                finish();
            }
        }
    }

    private boolean getNewGoodsAndSave(final HttpRequest httpRequest, final StringBuilder err){
        boolean code = true;

        final JSONObject data = new JSONObject();
        data.put("appid",getAppId());
        data.put("pos_num",getPosNum());
        data.put("stores_id",getStoreId());
        final String param = HttpRequest.generate_request_parm(data,getAppSecret());
        final JSONObject retJson = httpRequest.sendPost(getUrl() +"/api/goods/get_goods_all",param,true);
        switch (retJson.getIntValue("flag")){
            case 0:
                code = false;
                err.append(retJson.getString("info"));
                break;
            case 1:
                final JSONObject info = JSON.parseObject(Utils.getNullOrEmptyStringAsDefault(retJson,"info","{}"));
                switch (Utils.getNullStringAsEmpty(info,"status")){
                    case "n":
                        code = false;
                        err.append(info.getString("info"));
                        break;
                    case "y":
                        final JSONArray new_goods = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(info,"data","[]"));
                        if (!new_goods.isEmpty())
                            code = SQLiteHelper.execSQLByBatchFromJson(new_goods,"barcode_info" , CustomApplication.getGoodsCols(),err,1);
                        break;
                }
                break;
        }
        return code ;
    }

    @Override
    protected void saveAndAdd() {
        addGoods(generateParameter(),true);
    }

    private void reset(){
        isModify = false;
        mGoodsObj = null;
        mImageUri = null;

        mNameEt.setText(R.string.space_sz);
        resetCurPrice();
        //setDefaultSupplier();
        //setDefaultCategory();
        setDefaultUnit();

        mRetailPriceEt.setText(R.string.zero_p_z_sz);
        mVipPriceEt.setText(R.string.zero_p_z_sz);
        mPurPriceEt.setText(R.string.zero_p_z_sz);
        pf_price_et.setText(R.string.zero_p_z_sz);
        //mUnitTv.setText("");
        spec_tv.setText("");
        mBarcodeEt.setText("");
        if (Utils.isNotEmpty(mBarcode))mBarcode = null;

        getOnlycodeAndBarcode();
    }

    private void resetCurPrice(){
        final RadioButton n = findViewById(R.id.n);
        if (!n.isChecked())n.setChecked(true);
    }

    private void getGoodsByBarcodeId(){
        final StringBuilder sql = new StringBuilder("select ");
        final String[] cols = CustomApplication.getGoodsCols();
        for (int i = 0,size = cols.length;i < size;i ++){
            sql.append(cols[i]);
            if (i != size -1)sql.append(",");
        }
        sql.append(" from barcode_info where barcode_id = ").append(mBarcodeId);

        mGoodsObj = new JSONObject();
        if (SQLiteHelper.execSql(mGoodsObj,sql.toString())){
            showGoods(mGoodsObj);
        }else {
            MyDialog.toastMessage("查询商品信息错误..." + mGoodsObj.getString("info"));
        }
    }
    private void showGoods(final JSONObject goods){

        final String img_url = Utils.getNullStringAsEmpty(goods,"img_url");
        if (!"".equals(img_url)){
            final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
            CustomApplication.execute(()->{
                final Bitmap bitmap = BitmapFactory.decodeFile(CustomApplication.getGoodsImgSavePath() + szImage);
                CustomApplication.runInMainThread(()-> goods_img.setImageBitmap(bitmap));
            });
        }else{
            goods_img.setImageDrawable(getDrawable(R.drawable.nodish));
        }

        final JSONObject attr_obj = getAttrById(goods.getString("type"));
        if (null != attr_obj){
            String id = attr_obj.getString(TreeListBaseAdapter.COL_ID);
            mGoodsAttrEt.setTag(id);
            mGoodsAttrEt.setText(attr_obj.getString(TreeListBaseAdapter.COL_NAME));

            if ("2".equals(id)){
                final JSONObject obj = getMeteringById(goods.getString("metering_id"));
                if (null != obj){
                    mMeteringEt.setText(obj.getString(TreeListBaseAdapter.COL_NAME));
                    mMeteringEt.setTag(obj.getString(TreeListBaseAdapter.COL_ID));
                    mMeteringEt.setVisibility(View.VISIBLE);
                }
            }
        }
        setCurPriceFlag(goods.getIntValue("current_goods") == 1);

        mBarcodeEt.setText(goods.getString("barcode"));
        mNameEt.setText(goods.getString("goods_title"));

        mSupplierTv.setTag(goods.getString("gs_id"));
        mSupplierTv.setText(goods.getString("gs_name"));

        mItemIdEt.setText(goods.getString("only_coding"));

        mCategoryTv.setTag(goods.getString("category_id"));
        mCategoryTv.setText(goods.getString("category_name"));

        mBrandTv.setTag(goods.getString("brand_id"));
        mBrandTv.setText(goods.getString("brand"));

        mUnitTv.setTag(goods.getString("unit_id"));
        mUnitTv.setText(goods.getString("unit_name"));
        spec_tv.setText(goods.getString("spec_str"));

        place_et.setText(goods.getString("origin"));


        mPurPriceEt.setText(goods.getString("buying_price"));
        pf_price_et.setText(goods.getString("trade_price"));
        mRetailPriceEt.setText(goods.getString("retail_price"));
        mVipPriceEt.setText(goods.getString("yh_price"));
    }
}
