package com.wyc.cloudapp.bean;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: QueryCondition
 * @Description: 查询条件
 * @Author: wyc
 * @CreateDate: 2021-07-19 15:36
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-19 15:36
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class QueryCondition  {
    private static final int ORDER = 1;
    private static final int VIP_CODE= 2;
    private static final int VIP_MOBILE= 3;

    private final long start;
    private final long end;
    private final int query_type;
    private final String condition;

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getQuery_type() {
        return query_type;
    }

    public String getCondition() {
        return condition;
    }

    public QueryCondition(long start, long end, int query_type, String condition) {
        this.start = start;
        this.end = end;
        this.query_type = query_type;
        this.condition = condition;
    }
    public boolean isOrder(){
        return query_type == ORDER;
    }
    public boolean isVipCode(){
        return query_type == VIP_CODE;
    }
}
