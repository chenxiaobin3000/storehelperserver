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
    static public final int commodity_salereport = 301;
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
    static public final int market_offline = 501;
    static public final int market_report = 502;

    static public final int market_pdd = 510;
    static public final int market_pdd_alarm = 511;
    static public final int market_pdd_comein = 512;
    static public final int market_pdd_price = 513;
    static public final int market_pdd_sendout = 514;
    static public final int market_pdd_setting = 515;
    static public final int market_pdd_surplus = 516;

    static public final int market_meituan = 520;
    static public final int market_meituan_alarm = 521;
    static public final int market_meituan_comein = 522;
    static public final int market_meituan_price = 523;
    static public final int market_meituan_sendout = 524;
    static public final int market_meituan_setting = 525;
    static public final int market_meituan_surplus = 526;

    static public final int market_kuailv = 530;
    static public final int market_kuailv_alarm = 531;
    static public final int market_kuailv_comein = 532;
    static public final int market_kuailv_price = 533;
    static public final int market_kuailv_sendout = 534;
    static public final int market_kuailv_setting = 535;
    static public final int market_kuailv_surplus = 536;

    static public final int product = 600;
    static public final int product_report = 601;
    static public final int product_getlist = 602;
    static public final int product_alarm = 603;

    static public final int report = 700;
    static public final int report_day = 701;
    static public final int report_month = 702;
    static public final int report_week = 703;
    static public final int report_year = 704;

    static public final int storage = 800;
    static public final int storage_report = 801;
    static public final int storage_stock = 802;
    static public final int storage_getlist = 803;
    static public final int storage_alarm = 804;

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
