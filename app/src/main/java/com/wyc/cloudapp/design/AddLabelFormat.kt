package com.wyc.cloudapp.design

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import butterknife.ButterKnife
import butterknife.OnClick
import com.wyc.cloudapp.R
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext
import java.lang.NumberFormatException


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      AddLabelFormat
 * @Description:    新增标签设计格式对话框
 * @Author:         wyc
 * @CreateDate:     2022/4/6 13:40
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/4/6 13:40
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class AddLabelFormat(context: Context):AbstractDialogContext(context,context.getString(R.string.add_label)) {
    private val mLabelTemplate = LabelTemplate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        initLabelSize()
    }

    override fun getWidthRatio(): Double {
        return 0.95
    }

    override fun getContentLayoutId(): Int {
        return R.layout.add_label_format
    }
    fun getContent():LabelTemplate{
        var name = findViewById<EditText>(R.id.label_name).text.toString()
        try {
            val width = findViewById<EditText>(R.id.label_width).text.toString().toInt()
            val height = findViewById<EditText>(R.id.label_height).text.toString().toInt()

            mLabelTemplate.width = width
            mLabelTemplate.height = height
            if (name.isEmpty()){
                name = String.format("未命名_%d_%d", width, height)
            }
            mLabelTemplate.templateName = name
        }catch (e:NumberFormatException){
            MyDialog.toastMessage(e.message)
        }
        return mLabelTemplate
    }
    private fun initLabelSize(){
        findViewById<Spinner>(R.id.default_size)?.apply {
            val adapter = ArrayAdapter<String>(mContext, R.layout.drop_down_style)
            adapter.setDropDownViewResource(R.layout.drop_down_style)
            LabelTemplate.getDefaultSize().forEach {
                adapter.add(it.description)
            }
            setAdapter(adapter)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    LabelTemplate.getDefaultSize().forEach {
                        if (it.description == adapter.getItem(position)){
                            this@AddLabelFormat.findViewById<EditText>(R.id.label_width)?.setText(it.rW.toString())
                            this@AddLabelFormat.findViewById<EditText>(R.id.label_height)?.setText(it.rH.toString())
                            return
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }

    @OnClick(R.id.ok)
    fun ok(){
        setCodeAndExit(1)
    }
    @OnClick(R.id.cancel)
    fun c(){
        closeWindow()
    }
}