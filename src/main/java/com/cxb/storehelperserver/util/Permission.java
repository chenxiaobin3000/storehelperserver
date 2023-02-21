package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/27
 */
public class Permission {
    // 小程序
    static public final int mp_report = 10;

    static public final int mp_purchase_in_apply = 11;
    static public final int mp_purchase_out_apply = 12;
    static public final int mp_storage_in_apply = 13;
    static public final int mp_storage_out_apply = 14;
    static public final int mp_product_out_apply = 15;
    static public final int mp_product_in_apply = 16;
    static public final int mp_agreement_out_apply = 17;
    static public final int mp_agreement_in_apply = 18;
    static public final int mp_loss_local_apply = 19;
    static public final int mp_loss_cloud_apply = 20;

    static public final int mp_purchase_in_review = 21;
    static public final int mp_purchase_out_review = 22;
    static public final int mp_storage_in_review = 23;
    static public final int mp_storage_out_review = 24;
    static public final int mp_product_out_review = 25;
    static public final int mp_product_in_review = 26;
    static public final int mp_agreement_out_review = 27;
    static public final int mp_agreement_in_review = 28;
    static public final int mp_loss_local_review = 29;
    static public final int mp_loss_cloud_review = 30;

    // 管理后台
    static public final int dashboard_report = 100;
    static public final int dashboard_admin = 101;
    static public final int dashboard_userinfo = 102;

    static public final int agreement = 200;
    static public final int agreement_report = 201;
    static public final int agreement_shipped = 202;
    static public final int agreement_return = 203;

    static public final int purchase = 250;
    static public final int purchase_report = 251;
    static public final int purchase_purchase = 252;
    static public final int purchase_return = 253;

    static public final int commodity = 300;
    static public final int commodity_report = 301;
    static public final int commodity_commoditylist = 302;
    static public final int commodity_halfgoodlist = 303;
    static public final int commodity_originallist = 304;
    static public final int commodity_destroylist = 305;
    static public final int commodity_setcategory = 306;
    static public final int commodity_attributelist = 307;

    static public final int finance = 400;
    static public final int finance_report = 401;
    static public final int finance_getlist = 402;

    static public final int market = 500;
    static public final int market_report = 501;
    static public final int market_getlist = 502;
    static public final int market_commodity = 503;
    static public final int market_input = 504;
    static public final int market_offline = 505;

    static public final int product = 600;
    static public final int product_report = 601;
    static public final int product_process = 602;
    static public final int product_complete = 603;

    static public final int loss = 650;
    static public final int loss_report = 651;
    static public final int loss_getlist = 652;

    static public final int report = 700;
    static public final int report_market = 701;
    static public final int report_purchase = 702;
    static public final int report_agreement = 703;
    static public final int report_product = 704;
    static public final int report_storage = 705;
    static public final int report_stock = 706;
    static public final int report_cloud = 707;
    static public final int report_loss = 708;

    static public final int cloud = 750;
    static public final int cloud_report = 751;
    static public final int cloud_getlist = 752;
    static public final int cloud_input = 753;
    static public final int cloud_address = 754;

    static public final int storage = 800;
    static public final int storage_report = 801;
    static public final int storage_purchase = 802;
    static public final int storage_return = 803;
    static public final int storage_address = 804;

    static public final int stock = 850;
    static public final int stock_report = 851;
    static public final int stock_getlist = 852;

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
    static public final int system_rolelist = 1103;
    static public final int system_mprolelist = 1104;

    static public final int admin = 8888;
    static public final int admin_changegroup = 8889;
    static public final int admin_grouplist = 8890;
}
