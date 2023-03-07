package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 订单类型
    public enum OrderType {
        PURCHASE_PURCHASE_ORDER(1),     // 采购进货订单
        PURCHASE_RETURN_ORDER(2),       // 采购退货订单
        STORAGE_PURCHASE_ORDER(3),      // 仓储采购入库订单
        STORAGE_DISPATCH_ORDER(4),      // 仓储调度出库订单
        STORAGE_PURCHASE2_ORDER(5),     // 仓储调度入库订单
        STORAGE_LOSS_ORDER(6),          // 仓储损耗订单
        STORAGE_RETURN_ORDER(7),        // 仓储退货订单
        PRODUCT_PROCESS_ORDER(8),       // 生产开始订单
        PRODUCT_COMPLETE_ORDER(9),      // 生产完成订单
        PRODUCT_LOSS_ORDER(10),         // 生产损耗订单
        AGREEMENT_SHIPPED_ORDER(11),    // 履约发货订单
        AGREEMENT_RETURN_ORDER(12),     // 履约退货订单
        CLOUD_PURCHASE_ORDER(13),       // 云仓入库订单
        CLOUD_RETURN_ORDER(14),         // 云仓退货订单
        CLOUD_SALE_ORDER(15),           // 云仓销售订单
        CLOUD_LOSS_ORDER(16);           // 云仓损耗订单

        private int value = 0;

        private OrderType(int v) {
            this.value = v;
        }

        public static OrderType valueOf(int v) {
            switch (v) {
                case 1:
                    return PURCHASE_PURCHASE_ORDER;
                case 2:
                    return PURCHASE_RETURN_ORDER;
                case 3:
                    return STORAGE_PURCHASE_ORDER;
                case 4:
                    return STORAGE_DISPATCH_ORDER;
                case 5:
                    return STORAGE_PURCHASE2_ORDER;
                case 6:
                    return STORAGE_LOSS_ORDER;
                case 7:
                    return STORAGE_RETURN_ORDER;
                case 8:
                    return PRODUCT_PROCESS_ORDER;
                case 9:
                    return PRODUCT_COMPLETE_ORDER;
                case 10:
                    return PRODUCT_LOSS_ORDER;
                case 11:
                    return AGREEMENT_SHIPPED_ORDER;
                case 12:
                    return AGREEMENT_RETURN_ORDER;
                case 13:
                    return CLOUD_PURCHASE_ORDER;
                case 14:
                    return CLOUD_RETURN_ORDER;
                case 15:
                    return CLOUD_SALE_ORDER;
            }
            return CLOUD_LOSS_ORDER;
        }

        public int getValue() {
            return value;
        }
    }

    // 财务记录类型
    public enum FinanceAction {
        FINANCE_PURCHASE_PAY(1),        // 采购进货
        FINANCE_PURCHASE_FARE(2),       // 采购进货运费
        FINANCE_PURCHASE_RET(3),        // 采购退货
        FINANCE_PURCHASE_FARE2(4),      // 采购退货运费

        FINANCE_STORAGE_MGR(10),        // 仓储管理费
        FINANCE_STORAGE_FARE(11),       // 仓储调度运费
        FINANCE_STORAGE_RET(12),        // 采购退货
        FINANCE_STORAGE_FARE2(13),      // 采购退货运费

        FINANCE_PRODUCT_WRAP(20),       // 包装费
        FINANCE_PRODUCT_MAN(21),        // 人工费用
        FINANCE_PRODUCT_OUT(22),        // 外厂费用

        FINANCE_AGREEMENT_FARE(30),     // 履约发货物流
        FINANCE_AGREEMENT_FARE2(31),    // 履约退款物流

        FINANCE_CLOUD_RET(40),          // 云仓退货
        FINANCE_CLOUD_FARE(41),         // 云仓退货运费

        FINANCE_MARKET_SALE(50),        // 销售平台

        FINANCE_GROUP_OTHER(60);        // 经营费用

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
                    return FINANCE_STORAGE_MGR;
                case 11:
                    return FINANCE_STORAGE_FARE;
                case 12:
                    return FINANCE_STORAGE_RET;
                case 13:
                    return FINANCE_STORAGE_FARE2;

                case 20:
                    return FINANCE_PRODUCT_WRAP;
                case 21:
                    return FINANCE_PRODUCT_MAN;
                case 22:
                    return FINANCE_PRODUCT_OUT;

                case 30:
                    return FINANCE_AGREEMENT_FARE;
                case 31:
                    return FINANCE_AGREEMENT_FARE2;

                case 40:
                    return FINANCE_CLOUD_RET;
                case 41:
                    return FINANCE_CLOUD_FARE;

                case 50:
                    return FINANCE_MARKET_SALE;
            }
            return FINANCE_GROUP_OTHER;
        }

        public int getValue() {
            return value;
        }
    }

    // 审核类型
    public enum ReviewType {
        REVIEW_ALL(1),  // 全部
        REVIEW_HAS(2),  // 已审核
        REVIEW_NOT(3);  // 未审核

        private int value = 0;

        private ReviewType(int v) {
            this.value = v;
        }

        public static ReviewType valueOf(int v) {
            switch (v) {
                case 1:
                    return REVIEW_ALL;
                case 2:
                    return REVIEW_HAS;
            }
            return REVIEW_NOT;
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
}
