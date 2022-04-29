package com.wyc.cloudapp.print.parameter;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.fragment.PrintFormat;

import java.io.Serializable;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.bean
 * @ClassName: PrintFormatInfo
 * @Description: 打印格式参数
 * @Author: wyc
 * @CreateDate: 2021-12-28 16:33
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-28 16:33
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SalePrintParameter implements IParameter {
    @JSONField(name = "s_n")
    private String aliasStoresName;
    @JSONField(name = "f_s")
    private Integer footerSpace;
    @JSONField(name = "f_c")
    private String footerContent;//单据尾部内容
    @JSONField(name = "f")
    private Integer formatId;
    @JSONField(name = "p_c")
    private Integer printCount;
    @JSONField(name = "f_z")
    private Integer formatSize;//58 76 80

    public String getAliasStoresName() {
        return aliasStoresName;
    }

    public void setAliasStoresName(String aliasStoresName) {
        this.aliasStoresName = aliasStoresName;
    }

    @Override
    public int getFooterSpace() {
        return footerSpace == null ? 5 : footerSpace;
    }

    public void setFooterSpace(Integer footerSpace) {
        this.footerSpace = footerSpace;
    }

    public String getFooterContent() {
        return footerContent;
    }

    public void setFooterContent(String footerContent) {
        this.footerContent = footerContent;
    }

    public Integer getFormatId() {
        return formatId;
    }

    public void setFormatId(Integer formatId) {
        this.formatId = formatId;
    }

    @Override
    public int getPrintCount() {
        return printCount == null ? 1 : printCount;
    }

    public void setPrintCount(Integer printCount) {
        this.printCount = printCount;
    }

    public Integer getFormatSize() {
        return formatSize == null ? PrintFormat.PAPER_SPEC_58_ID : formatSize;
    }

    public void setFormatSize(Integer formatSize) {
        this.formatSize = formatSize;
    }

    @Override
    public String toString() {
        return "SalePrintParameter{" +
                "aliasStoresName='" + aliasStoresName + '\'' +
                ", footerSpace=" + footerSpace +
                ", footerContent='" + footerContent + '\'' +
                ", formatId=" + formatId +
                ", printCount=" + printCount +
                ", formatSize=" + formatSize +
                '}';
    }
}
