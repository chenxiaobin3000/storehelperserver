package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/27
 */
public class Permission {
    // 管理后台
    static public final int dashboard_report = 100;
    static public final int dashboard_admin = 101;
    static public final int dashboard_userinfo = 102;

    static public final int commodity = 900;
    static public final int commodity_category = 901;

    static public final int system = 1000;
    static public final int system_resetpwd = 1001;
    static public final int system_department = 1002;
    static public final int system_rolelist = 1003;
    static public final int system_userlist = 1004;
    static public final int system_setpassword = 1005;
    static public final int system_storage = 1106;
    static public final int system_supplier = 1107;
}
