package com.wyc.cloudapp.activity.mobile.business

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.TreeListBaseAdapter
import com.wyc.cloudapp.adapter.business.*
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting
import com.wyc.cloudapp.bean.EnquiryOrderPrintContent
import com.wyc.cloudapp.bean.OrderPrintContentBase
import com.wyc.cloudapp.bean.OrderPrintContentBase.Goods
import com.wyc.cloudapp.bean.TransferOutInOrder
import com.wyc.cloudapp.constants.InterfaceURL.ENQUIRY_ORDER_DETAIL
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson
import com.wyc.cloudapp.utils.FormatDateTimeUtils
import com.wyc.cloudapp.utils.Utils
import java.util.*

/*要货申请单*/
class MobileEnquiryOrderActivity : AbstractMobileBusinessOrderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getAdapter(): AbstractBusinessOrderDataAdapter<out AbstractDataAdapter.SuperViewHolder> {
         return MobileEnquiryOrderAdapter(this)
    }

    override fun generateQueryCondition(): JSONObject {
        return JSONObject().fluentPut("api", "/api/api_yaohuo/xlist")
    }

    override fun jumpAddTarget(): Class<*> {
        return MobileAddEnquiryOrderActivity::class.java
    }

    override fun getPermissionId(): String {
        return "44"
    }

    class MobileAddEnquiryOrderActivity:AbstractMobileAddOrderActivity(){
        private var mTargetWhTv:TextView? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initTransferInWarehouse()
        }
        override fun getContentLayoutId(): Int {
            return R.layout.activity_mobile_add_enquiry_order
        }

        private fun initTransferInWarehouse() {
            val transfer_in_wh_tv = findViewById<TextView>(R.id.target_wh_tv)
            val sz = getString(R.string.target_store)
            val array = getTransferInWarehouse(transfer_in_wh_tv)
            transfer_in_wh_tv.setOnClickListener { _: View? ->
                CustomApplication.runInMainThread {
                    val treeListDialog = TreeListDialogForJson(this, sz.substring(0, sz.length - 1))
                    treeListDialog.setData(array, null, true)
                    if (treeListDialog.exec() == 1) {
                        val `object` = treeListDialog.singleContent
                        setTargetWarehouse(`object`.getString(TreeListBaseAdapter.COL_ID), `object`.getString(TreeListBaseAdapter.COL_NAME))
                    }
                }
            }
            mTargetWhTv = transfer_in_wh_tv
        }

        private fun getTransferInWarehouse(tv: TextView): JSONArray {
            val err = StringBuilder()
            val array = SQLiteHelper.getListToJson("SELECT wh_id,stores_name name FROM shop_stores where stores_id <>$storeId", err)
            val data = JSONArray()
            if (null != array) {
                var `object`: JSONObject
                var i = 0
                val size = array.size
                while (i < size) {
                    val tmp = array.getJSONObject(i)
                    val id = Utils.getNullStringAsEmpty(tmp, "wh_id")
                    val name = Utils.getNullStringAsEmpty(tmp, "name")
                    `object` = JSONObject()
                    `object`["level"] = 0
                    `object`["unfold"] = false
                    `object`["isSel"] = false
                    `object`[TreeListBaseAdapter.COL_ID] = id
                    `object`[TreeListBaseAdapter.COL_NAME] = name
                    data.add(`object`)
                    if (i == 0) {
                        tv.tag = id
                        tv.text = name
                    }
                    i++
                }
            } else {
                MyDialog.ToastMessage(err.toString(), null)
            }
            return data
        }

        private fun setTargetWarehouse(id: String, name: String) {
            mTargetWhTv?.text = name
            mTargetWhTv?.tag = id
        }

        private fun getTargetWarehouseId(): String {
            return Utils.getViewTagValue(mTargetWhTv, "")
        }

        override fun showOrder() {
            super.showOrder()
            setTargetWarehouse(Utils.getNullStringAsEmpty(mOrderInfo, "mb_wh_id"), Utils.getNullStringAsEmpty(mOrderInfo, "mb_wh_name"))
            setView(mOrderCodeTv, Utils.getNullStringAsEmpty(mOrderInfo, "yhd_id"), Utils.getNullStringAsEmpty(mOrderInfo, "yhd_code"))
            setView(mDateTv, "", FormatDateTimeUtils.formatTimeWithTimestamp(mOrderInfo.getLongValue("addtime") * 1000))
        }

        override fun generateQueryDetailCondition(): JSONObject {
            return JSONObject().fluentPut("api", ENQUIRY_ORDER_DETAIL)
        }

        override fun getAdapter(): AbstractBusinessOrderDetailsDataAdapter<out AbstractDataAdapter.SuperViewHolder> {
            return MobileEnquiryOrderDetailAdapter(this)
        }

        override fun getSaleOperatorKey(): String {
            return "js_pt_user_id"
        }

        override fun getSaleOperatorNameKey(): String {
            return "js_pt_user_name"
        }

        override fun generateUploadCondition(): JSONObject {
            val uploadObj = super.generateUploadCondition()
            val obj = JSONObject()
            uploadObj.remove("gs_id")
            uploadObj["yhd_code"] = mOrderCodeTv.text.toString()
            uploadObj["yhd_id"] = Utils.getNullStringAsEmpty(mOrderInfo, orderIDKey)
            uploadObj["mb_wh_id"] = getTargetWarehouseId()
            uploadObj["goods_list_json"] = getGoodsList()
            obj["api"] = "/api/api_yaohuo/add"
            obj["upload_obj"] = uploadObj
            return obj
        }

        private fun getGoodsList(): JSONArray {
            val array = orderDetails
            val data = JSONArray()
            var i = 0
            val size = array.size
            while (i < size) {
                val obj = JSONObject()
                val old_obj = array.getJSONObject(i)
                obj["xnum"] = old_obj.getDoubleValue("xnum")
                obj["xnote"] = old_obj.getString("xnote")
                obj["barcode_id"] = old_obj.getString("barcode_id")
                obj["goods_id"] = old_obj.getString("goods_id")
                obj["conversion"] = old_obj.getString("conversion")
                obj["unit_id"] = old_obj.getString("unit_id")
                data.add(obj)
                i++
            }
            return data
        }

        override fun generateOrderCodePrefix(): String {
            return "YH"
        }

        override fun generateAuditCondition(): JSONObject {
            return JSONObject().fluentPut("api", "/api/api_yaohuo/sh")
        }

        override fun getOrderIDKey(): String {
            return "yhd_id"
        }

        override fun getPrintContent(setting: BusinessOrderPrintSetting?): String {
            val Builder = OrderPrintContentBase.Builder(EnquiryOrderPrintContent())
            val details: MutableList<Goods> = ArrayList()
            val name = getString(R.string.applying_order)
            val goods_list: JSONArray
            if (isNewOrder) {
                goods_list = orderDetails
                Builder.company(storeName)
                        .orderName(name)
                        .storeName(mWarehouseTv.text.toString())
                        .outStoreName(mTargetWhTv?.text.toString())
                        .operator(mSaleOperatorTv.text.toString())
                        .orderNo(mOrderCodeTv.text.toString())
                        .operateDate(FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1))
                        .remark(mRemarkEt.text.toString())
            } else {
                goods_list = mOrderInfo.getJSONArray("goods_list")
                Builder.company(storeName)
                        .orderName(name)
                        .storeName(mOrderInfo.getString("wh_name"))
                        .outStoreName(mOrderInfo.getString("mb_wh_name"))
                        .operator(mOrderInfo.getString(saleOperatorNameKey))
                        .orderNo(mOrderInfo.getString("yhd_code"))
                        .operateDate(mOrderInfo.getString("add_datetime"))
                        .remark(mOrderInfo.getString("remark"))
            }
            var i = 0
            val size = goods_list.size
            while (i < size) {
                val `object` = goods_list.getJSONObject(i)
                val goods = Goods.Builder()
                        .barcodeId(`object`.getString("barcode_id"))
                        .barcode(`object`.getString("barcode"))
                        .name(`object`.getString("goods_title"))
                        .unit(`object`.getString("unit_name"))
                        .num(`object`.getDoubleValue("xnum"))
                        .build()
                details.add(goods)
                i++
            }
            return Builder.goodsList(details).build().format58(this, setting!!)
        }

    }
}