package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/27
 */
public class Permission {
    // 小程序
    static public final int mp_report = 10;

    static public final int mp_purchase = 11;
    static public final int mp_purchase_purchase = 12;
    static public final int mp_purchase_return = 13;
    static public final int mp_storage = 20;
    static public final int mp_storage_purchase_in = 21;
    static public final int mp_storage_purchase_out = 22;
    static public final int mp_storage_product_in = 23;
    static public final int mp_storage_product_out = 24;
    static public final int mp_storage_agreement_in = 25;
    static public final int mp_storage_agreement_out = 26;
    static public final int mp_storage_offline_in = 27;
    static public final int mp_storage_offline_out = 28;
    static public final int mp_storage_dispatch_in = 29;
    static public final int mp_storage_dispatch_out = 30;
    static public final int mp_storage_loss = 31;
    static public final int mp_product = 40;
    static public final int mp_product_process = 41;
    static public final int mp_product_complete = 42;
    static public final int mp_product_loss = 43;
    static public final int mp_agreement = 50;
    static public final int mp_agreement_shipped = 51;
    static public final int mp_agreement_return = 52;
    static public final int mp_sale = 60;
    static public final int mp_sale_sale = 61;
    static public final int mp_sale_loss = 62;
    static public final int mp_offline = 70;
    static public final int mp_offline_offline = 71;
    static public final int mp_offline_return = 72;
    static public final int mp_end = 80;

    // 管理后台
    static public final int dashboard_report = 100;
    static public final int dashboard_admin = 101;
    static public final int dashboard_userinfo = 102;

    static public final int user = 1000;
    static public final int user_userlist = 1001;
    static public final int user_resetpwd = 1002;
    static public final int user_setpassword = 1003;

    static public final int system = 1100;
    static public final int system_alarm = 1101;
    static public final int system_groupinfo = 1102;
    static public final int system_department = 1107;
    static public final int system_storage = 1103;
    static public final int system_rolelist = 1104;
    static public final int system_mprolelist = 1105;
    static public final int system_supplier = 1106;
    static public final int system_account = 1108;

    static public final int admin = 8888;
    static public final int admin_changegroup = 8889;
    static public final int admin_grouplist = 8890;
}
