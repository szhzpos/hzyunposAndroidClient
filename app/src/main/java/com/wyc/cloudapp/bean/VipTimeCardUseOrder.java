package com.wyc.cloudapp.bean;


import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.wyc.cloudapp.fragment.PrintFormatFragment.TIME_CARD_USE_FORMAT_ID;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipTimeCardUseReuslt
 * @Description: 次卡使用结果
 * @Author: wyc
 * @CreateDate: 2021-07-19 11:43
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-19 11:43
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class VipTimeCardUseOrder{
    @JSONField(name = "number")
    private String number;
    @JSONField(name = "stores_name")
    private String storesName;
    @JSONField(name = "addtime")
    private String addtime;
    @JSONField(name = "title")
    private String title;
    @JSONField(name = "order_code")
    private String orderCode;
    @JSONField(name = "member_name")
    private String memberName;
    @JSONField(name = "goods")
    private List<GoodsDTO> goods;
    @JSONField(name = "use_num")
    private Integer useNum;
    @JSONField(name = "member_mobile")
    private String memberMobile;
    @JSONField(name = "img_big")
    private String imgBig;
    @JSONField(name = "img")
    private String img;
    @JSONField(name = "member_card")
    private String memberCard;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStoresName() {
        return storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public List<GoodsDTO> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsDTO> goods) {
        this.goods = goods;
    }

    public Integer getUseNum() {
        return useNum;
    }

    public void setUseNum(Integer useNum) {
        this.useNum = useNum;
    }

    public String getMemberMobile() {
        return memberMobile;
    }

    public void setMemberMobile(String memberMobile) {
        this.memberMobile = memberMobile;
    }

    public String getImgBig() {
        return imgBig;
    }

    public void setImgBig(String imgBig) {
        this.imgBig = imgBig;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getMemberCard() {
        return memberCard;
    }

    public void setMemberCard(String memberCard) {
        this.memberCard = memberCard;
    }

    public static class GoodsDTO {
        @JSONField(name = "goods_title")
        private String goodsTitle;
        @JSONField(name = "barcode_id")
        private String barcodeId;
        @JSONField(name = "unit")
        private String unit;
        @JSONField(name = "num")
        private Integer num;

        public String getGoodsTitle() {
            return goodsTitle;
        }

        public void setGoodsTitle(String goodsTitle) {
            this.goodsTitle = goodsTitle;
        }

        public String getBarcodeId() {
            return barcodeId;
        }

        public void setBarcodeId(String barcodeId) {
            this.barcodeId = barcodeId;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GoodsDTO goodsDTO = (GoodsDTO) o;
            return Objects.equals(barcodeId, goodsDTO.barcodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(barcodeId);
        }

        @NonNull
        @Override
        public String toString() {
            return "GoodsDTO{" +
                    "goodsTitle='" + goodsTitle + '\'' +
                    ", barcodeId='" + barcodeId + '\'' +
                    ", unit='" + unit + '\'' +
                    ", num=" + num +
                    '}';
        }
    }

    public void print(final MainActivity activity){
        CustomApplication.execute(()-> Printer.print(get_print_content(activity)));
    }

    private String get_print_content(final MainActivity context){
        final JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("t_card_use",print_format_info)){
            if (print_format_info.getIntValue("f") == TIME_CARD_USE_FORMAT_ID){
                switch (print_format_info.getIntValue("f_z")){
                    case R.id.f_58:
                        content = c_format_58(context,print_format_info);
                        break;
                    case R.id.f_76:
                        break;
                    case R.id.f_80:
                        break;
                }
            }else {
                context.runOnUiThread(()-> MyDialog.ToastMessage(context.getString(R.string.f_not_sz), context.getWindow()));
            }
        }else
            context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")), context.getWindow()));

        return content;
    }

    private String c_format_58(final MainActivity context, final JSONObject format_info){
        final StringBuilder info = new StringBuilder(),out = new StringBuilder();

        final String store_name = Utils.getNullStringAsEmpty(format_info,"s_n");
        final String new_line =  "\n";
        final String footer_c = Utils.getNullStringAsEmpty(format_info,"f_c");

        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1);
        int footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);

        final CustomApplication application = CustomApplication.self();

        while (print_count-- > 0) {//打印份数
            if (info.length() > 0){
                info.append(new_line).append(new_line);
                out.append(info);
                continue;
            }
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name.length() == 0 ? application.getStoreName() : store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(context.getString(R.string.order_sz).concat(getOrderCode())).append(new_line);
            info.append(context.getString(R.string.use_order_time).concat(getAddtime())).append(new_line);

            final JSONObject member_parameter = new JSONObject();
            if (!SQLiteHelper.getLocalParameter("MEMBER_PARAMETER",member_parameter)) Logger.d("查询会员参数错误:%s",member_parameter.getString("info"));
            String vip_name = getMemberName(),card_code = getMemberCard(),mobile = getMemberMobile();
            if (Utils.getNotKeyAsNumberDefault(member_parameter,"member_secret_protect",0) == 1){
                if (vip_name.length() > 2)
                    vip_name = vip_name.replace(vip_name.substring(1),Printer.REPLACEMENT);
                else {
                    vip_name = vip_name.concat(Printer.REPLACEMENT);
                }
                int len = card_code.length();
                if (len <= 3){
                    card_code = card_code.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    card_code = card_code.replace(card_code.substring(3,len - 1),Printer.REPLACEMENT);
                }else {
                    card_code = card_code.replace(card_code.substring(3,7),Printer.REPLACEMENT);
                }
                int mobile_len = mobile.length();
                if (mobile_len <= 3){
                    mobile = mobile.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    mobile = mobile.replace(mobile.substring(3,len - 1),Printer.REPLACEMENT);
                }else {
                    mobile = mobile.replace(mobile.substring(3,7),Printer.REPLACEMENT);
                }
            }

            info.append(context.getString(R.string.use_vip_name).concat(vip_name)).append(new_line);
            info.append(context.getString(R.string.use_vip_mobile).concat(mobile)).append(new_line);
            info.append(context.getString(R.string.use_vip_card).concat(card_code)).append(new_line);
            info.append(context.getString(R.string.use_store).concat(getStoresName())).append(new_line);

            String line_58 = application.getString(R.string.line_58),space_sz = " ",name;
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(line_58).append(new_line);
            name = getTitle();
            int space = 8 - name.length();
            if (space > 0){
                final StringBuilder sb = new StringBuilder(name);
                for (int i = 0;i < space;i ++){
                    sb.append(space_sz);
                }
                name = sb.toString();
            }else {
                name = name.substring(0,7);
            }
            stringBuilder.append(String.format(Locale.CHINA,"%s  扣减 %d 次%s",name,getUseNum(),new_line));
            stringBuilder.append(line_58);
            info.append(stringBuilder).append(new_line);

            if (footer_c.isEmpty()){
                info.append(context.getString(R.string.hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(application.getStoreInfo(),"telphone","")).append(new_line);
                info.append(context.getString(R.string.stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(application.getStoreInfo(),"region","")).append(new_line);
            }else {
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c).append(Printer.commandToStr(Printer.ALIGN_LEFT));
            }

            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);
        }
        out.append(info);

        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VipTimeCardUseOrder that = (VipTimeCardUseOrder) o;
        return Objects.equals(orderCode, that.orderCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderCode);
    }

    @NonNull
    @Override
    public String toString() {
        return "VipTimeCardUseOrder{" +
                "number='" + number + '\'' +
                ", storesName='" + storesName + '\'' +
                ", addtime='" + addtime + '\'' +
                ", title='" + title + '\'' +
                ", orderCode='" + orderCode + '\'' +
                ", memberName='" + memberName + '\'' +
                ", goods=" + goods +
                ", useNum=" + useNum +
                ", memberMobile='" + memberMobile + '\'' +
                ", imgBig='" + imgBig + '\'' +
                ", img='" + img + '\'' +
                ", memberCard='" + memberCard + '\'' +
                '}';
    }
}