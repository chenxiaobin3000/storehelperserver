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
    static public final int mp_purchase_purchase2_apply = 14;
    static public final int mp_purchase_return2_apply = 47;
    static public final int mp_storage_apply = 15;
    static public final int mp_storage_purchase_apply = 16;
    static public final int mp_storage_dispatch_apply = 17;
    static public final int mp_storage_purchase2_apply = 18;
    static public final int mp_storage_loss_apply = 19;
    static public final int mp_storage_return_apply = 20;
    static public final int mp_storage_agreement_apply = 21;
    static public final int mp_product_apply = 25;
    static public final int mp_product_process_apply = 26;
    static public final int mp_product_complete_apply = 27;
    static public final int mp_product_loss_apply = 28;
    static public final int mp_agreement_apply = 30;
    static public final int mp_agreement_shipped_apply = 31;
    static public final int mp_agreement_return_apply = 32;
    static public final int mp_cloud_apply = 35;
    static public final int mp_cloud_purchase_apply = 36;
    static public final int mp_cloud_return_apply = 37;
    static public final int mp_cloud_loss_apply = 38;
    static public final int mp_cloud_back_apply = 39;
    static public final int mp_cloud_agreement_apply = 40;
    static public final int mp_sale_apply = 45;
    static public final int mp_sale_return_apply = 50;

    static public final int mp_purchase_review = 51;
    static public final int mp_purchase_purchase_review = 52;
    static public final int mp_purchase_return_review = 53;
    static public final int mp_purchase_purchase2_review = 54;
    static public final int mp_purchase_return2_review = 87;
    static public final int mp_storage_review = 55;
    static public final int mp_storage_purchase_review = 56;
    static public final int mp_storage_dispatch_review = 57;
    static public final int mp_storage_purchase2_review = 58;
    static public final int mp_storage_loss_review = 59;
    static public final int mp_storage_return_review = 60;
    static public final int mp_storage_agreement_review = 61;
    static public final int mp_product_review = 65;
    static public final int mp_product_process_review = 66;
    static public final int mp_product_complete_review = 67;
    static public final int mp_product_loss_review = 68;
    static public final int mp_agreement_review = 70;
    static public final int mp_agreement_shipped_review = 71;
    static public final int mp_agreement_return_review = 72;
    static public final int mp_cloud_review = 75;
    static public final int mp_cloud_purchase_review = 76;
    static public final int mp_cloud_return_review = 77;
    static public final int mp_cloud_loss_review = 78;
    static public final int mp_cloud_back_review = 79;
    static public final int mp_cloud_agreement_review = 80;
    static public final int mp_sale_review = 85;
    static public final int mp_sale_return_review = 90;

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
    static public final int purchase_purchase2 = 254;
    static public final int purchase_return2 = 255;

    static public final int commodity = 300;
    static public final int commodity_report = 301;
    static public final int commodity_commoditylist = 302;
    static public final int commodity_halfgoodlist = 303;
    static public final int commodity_originallist = 304;
    static public final int commodity_destroylist = 305;
    static public final int commodity_category = 306;
    static public final int commodity_attributelist = 307;

    static public final int finance = 400;
    static public final int finance_report = 401;
    static public final int finance_getlist = 402;

    static public final int dock = 450;
    static public final int dock_account = 451;
    static public final int dock_cloud = 452;
    static public final int dock_commodity = 453;

    static public final int market = 500;
    static public final int market_report = 501;
    static public final int market_getlist = 502;
    static public final int market_input = 504;
    static public final int market_salereturn = 505;

    static public final int product = 600;
    static public final int product_report = 601;
    static public final int product_process = 602;
    static public final int product_complete = 603;
    static public final int product_loss = 604;

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
    static public final int cloud_purchase = 752;
    static public final int cloud_agreement = 753;
    static public final int cloud_loss = 754;
    static public final int cloud_return = 755;
    static public final int cloud_back = 756;

    static public final int storage = 800;
    static public final int storage_report = 801;
    static public final int storage_purchase = 802;
    static public final int storage_dispatch = 803;
    static public final int storage_purchase2 = 804;
    static public final int storage_loss = 805;
    static public final int storage_return = 806;
    static public final int storage_agreement = 807;

    static public final int stock = 850;
    static public final int stock_storage = 851;
    static public final int stock_storagelist = 852;
    static public final int stock_storageoldlist = 853;
    static public final int stock_cloud = 854;
    static public final int stock_cloudlist = 855;
    static public final int stock_cloudoldlist = 856;

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
    static public final int system_storageaddress = 1103;
    static public final int system_cloudaddress = 1104;
    static public final int system_rolelist = 1105;
    static public final int system_mprolelist = 1106;

    static public final int admin = 8888;
    static public final int admin_changegroup = 8889;
    static public final int admin_grouplist = 8890;
}
