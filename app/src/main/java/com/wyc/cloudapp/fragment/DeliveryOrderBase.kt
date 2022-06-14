package com.wyc.cloudapp.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.lifecycle.ViewModelProvider
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.DeliveryOrderNum
import com.wyc.cloudapp.data.viewModel.DeliveryOrderModel
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import java.text.SimpleDateFormat
import java.util.*


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      DeliveryOrderBase
 * @Description:    全部配送订单
 * @Author:         wyc
 * @CreateDate:     2022/6/13 17:38
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 17:38
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

open class DeliveryOrderBase : AbstractBaseFragment(){
    private var mStartDateEt: TextView? = null
    private var mStartTimeEt:TextView? = null
    private var mEndDateEt:TextView? = null
    private var mEndTimeEt:TextView? = null
    private var mVipPosition = -1;
    private var mDeliverymanPosition = -1;

    private val mParamObj = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelProvider(this).get(DeliveryOrderModel::class.java).getCurrentModel().
        observe(this) {
            Logger.d(it.toTypedArray().contentToString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.delivery_order, container, false)
    }

    override fun viewCreated() {
        initStartDateAndTime()
        initEndDateAndTime()
        initVipCondition()
        initDeliverymanCondition()
        initOrderCode()
        initQuery()
    }

    private fun initQuery(){
        findViewById<Button>(R.id.query_btn)?.apply {
            setOnClickListener {
                query()
            }
        }
    }

    private fun query(){
        val startDate = String.format("%s %s",mStartDateEt!!.text,mStartTimeEt!!.text)
        val endDate = String.format("%s %s",mEndDateEt!!.text,mEndTimeEt!!.text)
        mParamObj["start_time"] = startDate
        mParamObj["end_time"] = endDate

        findViewById<EditText>(R.id.vip_condition_et)?.apply {
            if (text.isNotEmpty()){
                when(mVipPosition){
                    0 ->{
                        mParamObj["member_card"] = text
                    }
                    1 ->{
                        mParamObj["member_name"] = text
                    }
                    2 ->{
                        mParamObj["member_mobile"] = text
                    }
                }
            }
        }

        this.findViewById<EditText>(R.id.deliveryman_cond_et)?.apply {
            if (text.isNotEmpty()){
                when(mDeliverymanPosition){
                    0 ->{
                        mParamObj["distributor_code"] = text
                    }
                    1 ->{
                        mParamObj["distributor_name"] = text
                    }
                    2 ->{
                        mParamObj["distributor_mobile"] = text
                    }
                }
            }
        }

        ViewModelProvider(this).get(DeliveryOrderModel::class.java).query(mParamObj)
    }

    private fun initOrderCode(){
        findViewById<EditText>(R.id.order_code)?.apply {
            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable) {
                    if (s.isNotEmpty()){
                        mParamObj["order_code"] = s.toString()
                    }else mParamObj.remove("order_code")

                }
            })
        }
    }

    private fun initEndDateAndTime() {
        mEndDateEt = findViewById<TextView>(R.id.end_date)?.apply {
            onFocusChangeListener = etFocusChangeListener
            text = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())
            setOnClickListener(View.OnClickListener { v: View? ->
                Utils.showDatePickerDialog(
                    context,
                    v as TextView?,
                    Calendar.getInstance()
                )
            })
        }
        mEndTimeEt = findViewById<TextView>(R.id.end_time)?.apply {
            onFocusChangeListener = etFocusChangeListener
            setOnClickListener { v: View? ->
                Utils.showTimePickerDialog(
                    context,
                    v as TextView?,
                    Calendar.getInstance()
                )
            }
        }
    }

    private fun initStartDateAndTime() {
        mStartDateEt = findViewById<TextView>(R.id.start_date)?.apply {
            onFocusChangeListener = etFocusChangeListener
            text = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())
            setOnClickListener { v: View? ->
                Utils.showDatePickerDialog(
                    context,
                    v as TextView?,
                    Calendar.getInstance()
                )
            }
        }
        mStartTimeEt = findViewById<TextView>(R.id.start_time)?.apply {
            onFocusChangeListener = etFocusChangeListener
            setOnClickListener { v: View? ->
                Utils.showTimePickerDialog(
                    context,
                    v as TextView?,
                    Calendar.getInstance()
                )
            }
        }
    }

    private val etFocusChangeListener = OnFocusChangeListener { v: View, b: Boolean ->
        if (b) v.callOnClick()
    }

    private fun initVipCondition() {
        findViewById<Spinner>(R.id.vip_condition_spinner)?.apply {
            val a = ArrayAdapter<String>(requireContext(), R.layout.drop_down_style)
            a.add(getString(R.string.vip_no_sz))
            a.add(getString(R.string.vip_name_sz))
            a.add(getString(R.string.contact))
            adapter = a
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mVipPosition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Logger.d("onNothingSelected")
                }
            }
        }
    }

    private fun initDeliverymanCondition() {
        findViewById<Spinner>(R.id.deliveryman_cond_spinner)?.apply {
            val a = ArrayAdapter<String>(requireContext(), R.layout.drop_down_style)
            a.add(getString(R.string.deliveryman_id))
            a.add(getString(R.string.deliveryman_name))
            a.add(getString(R.string.deliveryman_call))
            adapter = a
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mDeliverymanPosition = position
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Logger.d("onNothingSelected")
                }
            }
        }
    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.all_order)
    }

    companion object{
        private val orderNum = DeliveryOrderNum()
        @JvmStatic
        fun setOrderNum(num: DeliveryOrderNum){
            orderNum.allOrder = num.allOrder
            orderNum.newOrder = num.newOrder
            orderNum.dispatchingOrder = num.dispatchingOrder
            orderNum.completeOrder = num.completeOrder
            orderNum.refundOrder = num.refundOrder
        }
    }
    protected fun getOrderNum():DeliveryOrderNum{
        return orderNum
    }

     open fun getNumber():Int{
        return -1
    }
}