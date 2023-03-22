package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 订单类型
    public enum OrderType {
        PURCHASE_PURCHASE_ORDER(1),     // 采购仓储进货订单
        PURCHASE_RETURN_ORDER(2),       // 采购仓储退货订单
        PURCHASE_PURCHASE2_ORDER(3),    // 采购云仓进货订单
        PURCHASE_RETURN2_ORDER(4),      // 采购云仓退货订单
        STORAGE_PURCHASE_ORDER(10),     // 仓储采购入库订单
        STORAGE_DISPATCH_ORDER(11),     // 仓储调度出库订单
        STORAGE_PURCHASE2_ORDER(12),    // 仓储调度入库订单
        STORAGE_LOSS_ORDER(13),         // 仓储损耗订单
        STORAGE_RETURN_ORDER(14),       // 仓储采购退货订单
        STORAGE_AGREEMENT_ORDER(15),    // 仓储履约入库订单
        PRODUCT_PROCESS_ORDER(20),      // 生产开始订单
        PRODUCT_COMPLETE_ORDER(21),     // 生产完成订单
        PRODUCT_LOSS_ORDER(22),         // 生产损耗订单
        AGREEMENT_SHIPPED_ORDER(30),    // 履约发货订单
        AGREEMENT_RETURN_ORDER(31),     // 履约退货订单
        CLOUD_PURCHASE_ORDER(40),       // 云仓采购入库订单
        CLOUD_RETURN_ORDER(41),         // 云仓采购退货订单
        CLOUD_LOSS_ORDER(42),           // 云仓损耗订单
        CLOUD_BACK_ORDER(43),           // 云仓履约退货订单
        CLOUD_AGREEMENT_ORDER(44),      // 云仓履约入库订单
        // 调度
        // 线下销售
        SALE_RETURN_ORDER(50);          // 销售退货订单

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
                    return PURCHASE_PURCHASE2_ORDER;
                case 4:
                    return PURCHASE_RETURN2_ORDER;
                case 10:
                    return STORAGE_PURCHASE_ORDER;
                case 11:
                    return STORAGE_DISPATCH_ORDER;
                case 12:
                    return STORAGE_PURCHASE2_ORDER;
                case 13:
                    return STORAGE_LOSS_ORDER;
                case 14:
                    return STORAGE_RETURN_ORDER;
                case 15:
                    return STORAGE_AGREEMENT_ORDER;
                case 20:
                    return PRODUCT_PROCESS_ORDER;
                case 21:
                    return PRODUCT_COMPLETE_ORDER;
                case 22:
                    return PRODUCT_LOSS_ORDER;
                case 30:
                    return AGREEMENT_SHIPPED_ORDER;
                case 31:
                    return AGREEMENT_RETURN_ORDER;
                case 40:
                    return CLOUD_PURCHASE_ORDER;
                case 41:
                    return CLOUD_RETURN_ORDER;
                case 42:
                    return CLOUD_LOSS_ORDER;
                case 43:
                    return CLOUD_BACK_ORDER;
                case 44:
                    return CLOUD_AGREEMENT_ORDER;
            }
            return SALE_RETURN_ORDER;
        }

        public int getValue() {
            return value;
        }
    }

    // 财务记录类型
    public enum FinanceAction {
        FINANCE_PURCHASE_PAY(1),        // 采购仓储进货
        FINANCE_PURCHASE_FARE(2),       // 采购仓储进货运费
        FINANCE_PURCHASE_RET(3),        // 采购仓储退货
        FINANCE_PURCHASE_FARE2(4),      // 采购仓储退货运费
        FINANCE_PURCHASE2_PAY(5),       // 采购云仓进货
        FINANCE_PURCHASE2_FARE(6),      // 采购云仓进货运费
        FINANCE_PURCHASE2_RET(7),       // 采购云仓退货
        FINANCE_PURCHASE2_FARE2(8),     // 采购云仓退货运费

        FINANCE_STORAGE_MGR(10),        // 仓储管理费
        FINANCE_STORAGE_FARE(11),       // 仓储调度运费
        FINANCE_STORAGE_RET(12),        // 采购退货
        FINANCE_STORAGE_FARE2(13),      // 采购退货运费

        FINANCE_PRODUCT_WRAP(20),       // 包装费
        FINANCE_PRODUCT_MAN(21),        // 人工费用
        FINANCE_PRODUCT_OUT(22),        // 外厂费用

        FINANCE_AGREEMENT_FARE(30),     // 履约发货物流
        FINANCE_AGREEMENT_FARE2(31),    // 履约退款物流

        FINANCE_CLOUD_RET(40),          // 云仓退采购
        FINANCE_CLOUD_FARE(41),         // 云仓退采购运费
        FINANCE_CLOUD_BACK(42),         // 云仓退仓库
        FINANCE_CLOUD_FARE2(43),        // 云仓退仓库运费

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
                case 5:
                    return FINANCE_PURCHASE2_PAY;
                case 6:
                    return FINANCE_PURCHASE2_FARE;
                case 7:
                    return FINANCE_PURCHASE2_RET;
                case 8:
                    return FINANCE_PURCHASE2_FARE2;

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
                case 42:
                    return FINANCE_CLOUD_BACK;
                case 43:
                    return FINANCE_CLOUD_FARE2;

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

    // 完成类型
    public enum CompleteType {
        COMPLETE_ALL(0),  // 全部
        COMPLETE_HAS(1),  // 未完成
        COMPLETE_NOT(2);  // 已完成

        private int value = 0;

        private CompleteType(int v) {
            this.value = v;
        }

        public static CompleteType valueOf(int v) {
            switch (v) {
                case 0:
                    return COMPLETE_ALL;
                case 1:
                    return COMPLETE_HAS;
            }
            return COMPLETE_NOT;
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
