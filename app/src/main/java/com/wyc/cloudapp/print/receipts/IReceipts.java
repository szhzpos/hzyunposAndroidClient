package com.wyc.cloudapp.print.receipts;

import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.bean.PrintFormatInfo;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: IReceipts
 * @Description: 单据接口
 * @Author: wyc
 * @CreateDate: 2021-12-29 12:59
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-29 12:59
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public interface IReceipts {
    PrintFormatInfo getPrintFormat();
    List<PrintItem> getPrintItem();
}
