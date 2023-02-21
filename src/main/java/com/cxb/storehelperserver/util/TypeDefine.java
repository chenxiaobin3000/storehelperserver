package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 销售平台
    public enum MarketType {
        MARKET_PDD(1),      // 拼多多
        MARKET_MEITUAN(2),  // 美团
        MARKET_KUAILV(3);   // 快驴

        private int value = 0;

        private MarketType(int v) {
            this.value = v;
        }

        public static MarketType valueOf(int v) {
            switch (v) {
                case 1:
                    return MARKET_PDD;
                case 2:
                    return MARKET_MEITUAN;
            }
            return MARKET_KUAILV;
        }

        public int getValue() {
            return value;
        }
    }

    // 商品类型
    public enum CommodityType {
        COMMODITY(1),   // 商品
        HALFGOOD(2),    // 半成品
        ORIGINAL(3),    // 原料
        STANDARD(4);    // 标品

        private int value = 0;

        private CommodityType(int v) {
            this.value = v;
        }

        public static CommodityType valueOf(int v) {
            switch (v) {
                case 1:
                    return COMMODITY;
                case 2:
                    return HALFGOOD;
                case 3:
                    return ORIGINAL;
            }
            return STANDARD;
        }

        public int getValue() {
            return value;
        }
    }

    // 订单类型
    public enum OrderType {
        PURCHASE_IN_ORDER(1),   // 采购进货订单
        PURCHASE_OUT_ORDER(2),  // 采购退货订单
        STORAGE_IN_ORDER(3),    // 进货入库订单
        STORAGE_OUT_ORDER(4),   // 进货出库订单
        PRODUCT_IN_ORDER(5),    // 生产入库订单
        PRODUCT_OUT_ORDER(6),   // 生产出库订单
        AGREEMENT_IN_ORDER(7),  // 履约入库订单
        AGREEMENT_OUT_ORDER(8), // 履约出库订单
        CLOUD_IN_ORDER(9),      // 云仓入库订单
        CLOUD_OUT_ORDER(10),    // 云仓退货订单
        LOSS_LOCAL_ORDER(11),   // 本地损耗订单
        LOSS_CLOUD_ORDER(12),   // 云仓损耗订单
        LOSS_PRODUCT_ORDER(13); // 生产损耗订单

        private int value = 0;

        private OrderType(int v) {
            this.value = v;
        }

        public static OrderType valueOf(int v) {
            switch (v) {
                case 1:
                    return PURCHASE_IN_ORDER;
                case 2:
                    return PURCHASE_OUT_ORDER;
                case 3:
                    return STORAGE_IN_ORDER;
                case 4:
                    return STORAGE_OUT_ORDER;
                case 5:
                    return PRODUCT_IN_ORDER;
                case 6:
                    return PRODUCT_OUT_ORDER;
                case 7:
                    return AGREEMENT_IN_ORDER;
                case 8:
                    return AGREEMENT_OUT_ORDER;
                case 9:
                    return LOSS_LOCAL_ORDER;
            }
            return LOSS_CLOUD_ORDER;
        }

        public int getValue() {
            return value;
        }
    }

    // 订单类型
    public enum OrderInOutType {
        IN_ORDER(false), // 进货入库订单
        OUT_ORDER(true); // 进货出库订单

        private boolean value = false;

        private OrderInOutType(boolean v) {
            this.value = v;
        }

        public static OrderInOutType valueOf(boolean v) {
            return v ? OUT_ORDER : IN_ORDER;
        }

        public boolean getValue() {
            return value;
        }
    }

    // 报表周期类型
    public enum ReportCycleType {
        REPORT_DAILY(1),    // 每日报表
        REPORT_WEEKLY(2),   // 每周报表
        REPORT_MONTH(3),    // 月报表
        REPORT_YEARLY(4);   // 年度报表

        private int value = 0;

        private ReportCycleType(int v) {
            this.value = v;
        }

        public static ReportCycleType valueOf(int v) {
            switch (v) {
                case 1:
                    return REPORT_DAILY;
                case 2:
                    return REPORT_WEEKLY;
                case 3:
                    return REPORT_MONTH;
            }
            return REPORT_YEARLY;
        }

        public int getValue() {
            return value;
        }
    }

    // 财务记录类型
    public enum FinanceAction {
        FINANCE_PURCHASE_PAY(1),        // 采购进货
        FINANCE_PURCHASE_FARE(2),       // 采购进货物流
        FINANCE_PURCHASE_RET(3),        // 采购退款
        FINANCE_PURCHASE_FARE2(4),      // 采购退款物流

        FINANCE_STORAGE_LOCAL(10),      // 本地仓储
        FINANCE_STORAGE_CLOUD(11),      // 云仓仓储

        FINANCE_PRODUCT_MAN(20),        // 人工费用
        FINANCE_PRODUCT_OUT(21),        // 外厂人工费用
        FINANCE_PRODUCT_WRAP(22),       // 包装费

        FINANCE_AGREEMENT_FARE(30),     // 履约发货物流
        FINANCE_AGREEMENT_FARE2(31),    // 履约退款物流

        FINANCE_MARKET_PAY(40),         // 销售平台打款

        FINANCE_MANAGER_OTHER(50);      // 经营费用

        private int value = 0;

        private FinanceAction(int v) {
            this.value = v;
        }

        public static FinanceAction valueOf(int v) {
            switch (v) {
                case 1:
                    return FINANCE_PURCHASE_PAY;
                case 2:
                    return FINANCE_PURCHASE_FARE;
                case 3:
                    return FINANCE_PURCHASE_RET;
                case 4:
                    return FINANCE_PURCHASE_FARE2;

                case 10:
                    return FINANCE_STORAGE_LOCAL;
                case 11:
                    return FINANCE_STORAGE_CLOUD;

                case 20:
                    return FINANCE_PRODUCT_MAN;
                case 21:
                    return FINANCE_PRODUCT_OUT;
                case 22:
                    return FINANCE_PRODUCT_WRAP;

                case 30:
                    return FINANCE_AGREEMENT_FARE;
                case 31:
                    return FINANCE_AGREEMENT_FARE2;

                case 40:
                    return FINANCE_MARKET_PAY;
            }
            return FINANCE_MANAGER_OTHER;
        }

        public int getValue() {
            return value;
        }
    }
}
