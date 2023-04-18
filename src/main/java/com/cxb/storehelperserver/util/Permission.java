package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/27
 */
public class Permission {
    // 小程序
    static public final int mp_report = 10;

    static public final int mp_purchase_apply = 11;
    static public final int mp_purchase_purchase_apply = 12;
    static public final int mp_purchase_return_apply = 13;
    static public final int mp_product_apply = 15;
    static public final int mp_product_collect_apply = 16;
    static public final int mp_storage_apply = 20;
    static public final int mp_storage_purchase_apply = 21;
    static public final int mp_storage_return_apply = 22;
    static public final int mp_storage_dispatch_apply = 23;
    static public final int mp_storage_loss_apply = 24;
    static public final int mp_storage_offline_apply = 25;
    static public final int mp_storage_back_apply = 26;
    static public final int mp_agreement_apply = 30;
    static public final int mp_agreement_shipped_apply = 31;
    static public final int mp_agreement_return_apply = 32;
    static public final int mp_sale_apply = 40;
    static public final int mp_sale_sale_apply = 41;
    static public final int mp_sale_after_apply = 42;
    static public final int mp_sale_loss_apply = 43;
    static public final int mp_end_apply = 50;

    static public final int mp_purchase_review = 51;
    static public final int mp_purchase_purchase_review = 52;
    static public final int mp_purchase_return_review = 53;
    static public final int mp_product_review = 55;
    static public final int mp_product_collect_review = 56;
    static public final int mp_storage_review = 60;
    static public final int mp_storage_purchase_review = 61;
    static public final int mp_storage_return_review = 62;
    static public final int mp_storage_dispatch_review = 63;
    static public final int mp_storage_loss_review = 64;
    static public final int mp_storage_offline_review = 65;
    static public final int mp_storage_back_review = 66;
    static public final int mp_agreement_review = 70;
    static public final int mp_agreement_shipped_review = 71;
    static public final int mp_agreement_return_review = 72;
    static public final int mp_sale_review = 80;
    static public final int mp_sale_sale_review = 81;
    static public final int mp_sale_after_review = 82;
    static public final int mp_sale_loss_review = 83;
    static public final int mp_end_review = 90;

    // 管理后台
    static public final int dashboard_report = 100;
    static public final int dashboard_admin = 101;
    static public final int dashboard_userinfo = 102;

    static public final int agreement = 200;
    static public final int agreement_report = 201;
    static public final int agreement_getlist = 202;
    static public final int agreement_shipped = 203;
    static public final int agreement_return = 204;

    static public final int purchase = 250;
    static public final int purchase_report = 251;
    static public final int purchase_getlist = 252;
    static public final int purchase_purchase = 253;
    static public final int purchase_return = 254;

    static public final int commodity = 300;
    static public final int commodity_report = 301;
    static public final int commodity_commodity = 302;
    static public final int commodity_halfgood = 303;
    static public final int commodity_original = 304;
    static public final int commodity_category = 306;
    static public final int commodity_attribute = 307;

    static public final int finance = 400;
    static public final int finance_report = 401;
    static public final int finance_getlist = 402;
    static public final int finance_detail = 403;
    static public final int finance_label = 404;

    static public final int dock = 450;
    static public final int dock_account = 451;
    static public final int dock_many = 452;
    static public final int dock_storage = 453;
    static public final int dock_commodity = 454;

    static public final int market = 500;
    static public final int market_report = 501;
    static public final int market_getlist = 502;
    static public final int market_sale = 503;
    static public final int market_after = 504;
    static public final int market_loss = 505;

    static public final int product = 600;
    static public final int product_report = 601;
    static public final int product_getlist = 602;
    static public final int product_collect = 603;

    static public final int report = 700;
    static public final int report_market = 701;
    static public final int report_purchase = 702;
    static public final int report_agreement = 703;
    static public final int report_product = 704;
    static public final int report_storage = 705;
    static public final int report_stock = 706;

    static public final int storage = 800;
    static public final int storage_stockreport = 801;
    static public final int storage_stocklist = 802;
    static public final int storage_report = 803;
    static public final int storage_getlist = 804;
    static public final int storage_purchase = 805;
    static public final int storage_return = 806;
    static public final int storage_dispatch = 807;
    static public final int storage_loss = 808;
    static public final int storage_offline = 809;
    static public final int storage_back = 810;

    static public final int supplier = 900;
    static public final int supplier_getlist = 901;
    static public final int supplier_ledger = 902;

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

    static public final int admin = 8888;
    static public final int admin_changegroup = 8889;
    static public final int admin_grouplist = 8890;
}
