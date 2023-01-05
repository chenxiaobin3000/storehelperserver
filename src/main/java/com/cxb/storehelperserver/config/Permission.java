package com.cxb.storehelperserver.config;

/**
 * desc:
 * auth: cxb
 * date: 2022/12/27
 */
public class Permission {
    static public final int dashboard_report = 100;
    static public final int dashboard_admin = 101;
    static public final int dashboard_userinfo = 102;

    static public final int agreement = 200;
    static public final int agreement_getlist = 201;
    static public final int agreement_goback = 202;
    static public final int agreement_report = 203;
    static public final int agreement_sendout = 204;

    static public final int commodity = 300;
    static public final int commodity_commoditylist = 301;
    static public final int commodity_setcategory = 302;
    static public final int commodity_attributelist = 303;
    static public final int commodity_salereport = 304;

    static public final int finance = 400;
    static public final int finance_getinfo = 401;
    static public final int finance_report = 402;

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
    static public final int product_alarm = 601;
    static public final int product_getlist = 602;
    static public final int product_productorder = 603;
    static public final int product_raworder = 604;
    static public final int product_report = 605;

    static public final int report = 700;
    static public final int report_day = 701;
    static public final int report_month = 702;
    static public final int report_week = 703;
    static public final int report_year = 704;

    static public final int storage = 800;
    static public final int storage_address = 801;
    static public final int storage_alarm = 802;
    static public final int storage_batchlist = 803;
    static public final int storage_getlist = 804;
    static public final int storage_productorder = 805;
    static public final int storage_raworder = 806;
    static public final int storage_report = 807;

    static public final int supplier = 900;
    static public final int supplier_getlist = 901;
    static public final int supplier_keepbook = 902;

    static public final int user = 1000;
    static public final int user_setpassword = 1001;
    static public final int user_userlist = 1002;
    static public final int user_resetpwd = 1003;

    static public final int system = 1100;
    static public final int system_charge = 1101;
    static public final int system_rolelist = 1102;
    static public final int system_groupinfo = 1103;
    static public final int system_notify = 1104;

    static public final int admin = 8888;
    static public final int admin_changegroup = 88881;
    static public final int admin_grouplist = 88882;
    static public final int admin_userlist = 88883;
}
