package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 业务类型
    public enum BusinessType {
        BUSINESS_PURCHASE(1),   // 采购
        BUSINESS_STORAGE(2),    // 仓储
        BUSINESS_PRODUCT(3),    // 生产
        BUSINESS_AGREEMENT(4),  // 履约
        BUSINESS_SALE(5);       // 销售

        private int value = 0;

        private BusinessType(int v) {
            this.value = v;
        }

        public static BusinessType valueOf(int v) {
            switch (v) {
                case 1:
                    return BUSINESS_PURCHASE;
                case 2:
                    return BUSINESS_STORAGE;
                case 3:
                    return BUSINESS_PRODUCT;
                case 4:
                    return BUSINESS_AGREEMENT;
            }
            return BUSINESS_SALE;
        }

        public int getValue() {
            return value;
        }
    }

    // 订单类型
    public enum OrderType {
        PURCHASE_PURCHASE_ORDER(1),     // 采购进货订单
        PURCHASE_RETURN_ORDER(2),       // 采购退货订单
        PRODUCT_COLLECT_ORDER(3),       // 生产订单
        STORAGE_PURCHASE_ORDER(10),     // 仓储采购入库订单
        STORAGE_RETURN_ORDER(11),       // 仓储采购退货订单
        STORAGE_DISPATCH_ORDER(12),     // 仓储调度订单
        STORAGE_LOSS_ORDER(13),         // 仓储损耗订单
        STORAGE_OFFLINE_ORDER(14),      // 线下销售订单
        STORAGE_BACK_ORDER(15),         // 线下销售退货订单
        AGREEMENT_SHIPPED_ORDER(20),    // 履约发货订单
        AGREEMENT_RETURN_ORDER(21),     // 履约退货订单
        SALE_SALE_ORDER(30),            // 线上销售订单
        SALE_AFTER_ORDER(31),           // 线上售后订单
        SALE_LOSS_ORDER(32);            // 线上损耗订单

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
                    return PRODUCT_COLLECT_ORDER;
                case 10:
                    return STORAGE_PURCHASE_ORDER;
                case 11:
                    return STORAGE_RETURN_ORDER;
                case 12:
                    return STORAGE_DISPATCH_ORDER;
                case 13:
                    return STORAGE_LOSS_ORDER;
                case 14:
                    return STORAGE_OFFLINE_ORDER;
                case 15:
                    return STORAGE_BACK_ORDER;
                case 20:
                    return AGREEMENT_SHIPPED_ORDER;
                case 21:
                    return AGREEMENT_RETURN_ORDER;
                case 30:
                    return SALE_SALE_ORDER;
                case 31:
                    return SALE_AFTER_ORDER;
            }
            return SALE_LOSS_ORDER;
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
        FINANCE_STORAGE_OFFLINE(12),    // 线下销售
        FINANCE_STORAGE_FARE2(13),      // 线下运费
        FINANCE_STORAGE_BACK(14),       // 线下销售退货
        FINANCE_STORAGE_FARE3(15),      // 线下退货运费

        FINANCE_PRODUCT_PAY(20),        // 生产成本

        FINANCE_AGREEMENT_FARE(30),     // 履约发货物流
        FINANCE_AGREEMENT_FARE2(31),    // 履约退款物流

        FINANCE_MARKET_SALE(40),        // 线上销售

        FINANCE_GROUP_OTHER(50);        // 经营费用

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
                    return FINANCE_STORAGE_OFFLINE;
                case 13:
                    return FINANCE_STORAGE_FARE2;
                case 14:
                    return FINANCE_STORAGE_BACK;
                case 15:
                    return FINANCE_STORAGE_FARE3;

                case 20:
                    return FINANCE_PRODUCT_PAY;

                case 30:
                    return FINANCE_AGREEMENT_FARE;
                case 31:
                    return FINANCE_AGREEMENT_FARE2;

                case 40:
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

    // 生产类型
    public enum ProductType {
        PRODUCT_OUT(1),     // 出库
        PRODUCT_IN(2),      // 入库
        PRODUCT_LOSS(3);    // 损耗

        private int value = 0;

        private ProductType(int v) {
            this.value = v;
        }

        public static ProductType valueOf(int v) {
            switch (v) {
                case 1:
                    return PRODUCT_OUT;
                case 2:
                    return PRODUCT_IN;
            }
            return PRODUCT_LOSS;
        }

        public int getValue() {
            return value;
        }
    }
}
