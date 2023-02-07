package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/27
 */
public class Permission {
    // 小程序
    static public final int mp_report = 10;
    static public final int mp_storage_in_apply = 11;
    static public final int mp_storage_out_apply = 12;
    static public final int mp_product_out_apply = 13;
    static public final int mp_product_in_apply = 14;
    static public final int mp_agreement_out_apply = 15;
    static public final int mp_agreement_in_apply = 16;
    static public final int mp_storage_in_review = 17;
    static public final int mp_storage_out_review = 18;
    static public final int mp_product_out_review = 19;
    static public final int mp_product_in_review = 20;
    static public final int mp_agreement_out_review = 21;
    static public final int mp_agreement_in_review = 22;

    // 管理后台
    static public final int dashboard_report = 100;
    static public final int dashboard_admin = 101;
    static public final int dashboard_userinfo = 102;

    static public final int agreement = 200;
    static public final int agreement_report = 201;
    static public final int agreement_getlist = 202;
    static public final int agreement_alarm = 203;

    static public final int commodity = 300;
    static public final int commodity_report = 301;
    static public final int commodity_commoditylist = 302;
    static public final int commodity_halfgoodlist = 303;
    static public final int commodity_originallist = 304;
    static public final int commodity_standardlist = 305;
    static public final int commodity_destroylist = 306;
    static public final int commodity_setcategory = 307;
    static public final int commodity_attributelist = 308;

    static public final int finance = 400;
    static public final int finance_report = 401;
    static public final int finance_getlist = 402;

    static public final int market = 500;
    static public final int market_report = 501;
    static public final int market_offline = 502;

    static public final int market_pdd = 510;
    static public final int market_pdd_report = 511;
    static public final int market_pdd_getlist = 512;
    static public final int market_pdd_commodity = 513;
    static public final int market_pdd_alarm = 514;

    static public final int market_meituan = 520;
    static public final int market_meituan_report = 521;
    static public final int market_meituan_getlist = 522;
    static public final int market_meituan_commodity = 523;
    static public final int market_meituan_alarm = 524;

    static public final int market_kuailv = 530;
    static public final int market_kuailv_report = 531;
    static public final int market_kuailv_getlist = 532;
    static public final int market_kuailv_commodity = 533;
    static public final int market_kuailv_alarm = 534;

    static public final int product = 600;
    static public final int product_report = 601;
    static public final int product_getlist = 602;
    static public final int product_alarm = 603;

    static public final int report = 700;
    static public final int report_market = 701;
    static public final int report_agreement = 702;
    static public final int report_product = 703;
    static public final int report_storage = 704;
    static public final int report_stock = 705;

    static public final int storage = 800;
    static public final int storage_reportstock = 801;
    static public final int storage_stock = 802;
    static public final int storage_reportstorage = 803;
    static public final int storage_getlist = 804;
    static public final int storage_alarm = 805;

    static public final int supplier = 900;
    static public final int supplier_getlist = 901;
    static public final int supplier_keepbook = 902;

    static public final int user = 1000;
    static public final int user_userlist = 1001;
    static public final int user_resetpwd = 1002;
    static public final int user_setpassword = 1003;

    static public final int system = 1100;
    static public final int system_groupinfo = 1101;
    static public final int system_storagelist = 1102;
    static public final int system_rolelist = 1103;
    static public final int system_mprolelist = 1104;
    static public final int system_charge = 1105;

    static public final int admin = 8888;
    static public final int admin_changegroup = 8889;
    static public final int admin_grouplist = 8890;
}
