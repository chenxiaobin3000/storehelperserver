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
        BUSINESS_SALE(5),       // 线上销售
        BUSINESS_OFFLINE(6);    // 线下销售

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
                case 5:
                    return BUSINESS_SALE;
            }
            return BUSINESS_OFFLINE;
        }

        public int getValue() {
            return value;
        }
    }

    // 订单类型
    public enum OrderType {
        PURCHASE_PURCHASE_ORDER(1),         // 采购进货订单
        PURCHASE_RETURN_ORDER(2),           // 采购退货订单
        STORAGE_PURCHASE_IN_ORDER(10),      // 仓储采购入库订单
        STORAGE_PURCHASE_OUT_ORDER(11),     // 仓储采购退货订单
        STORAGE_PRODUCT_IN_ORDER(12),       // 生产入库订单
        STORAGE_PRODUCT_OUT_ORDER(13),      // 生产出库订单
        STORAGE_AGREEMENT_IN_ORDER(14),     // 生产履约入库订单
        STORAGE_AGREEMENT_OUT_ORDER(15),    // 生产履约出库订单
        STORAGE_OFFLINE_IN_ORDER(16),       // 生产线下入库订单
        STORAGE_OFFLINE_OUT_ORDER(17),      // 生产线下出库订单
        STORAGE_DISPATCH_IN_ORDER(18),      // 仓储调度入库订单
        STORAGE_DISPATCH_OUT_ORDER(19),     // 仓储调度出库订单
        STORAGE_LOSS_ORDER(20),             // 仓储损耗订单
        PRODUCT_PROCESS_ORDER(30),          // 生产开始订单
        PRODUCT_COMPLETE_ORDER(31),         // 生产完成订单
        PRODUCT_LOSS_ORDER(32),             // 生产损耗订单
        AGREEMENT_SHIPPED_ORDER(40),        // 履约发货订单
        AGREEMENT_RETURN_ORDER(41),         // 履约退货订单
        SALE_SALE_ORDER(50),                // 线上销售订单
        SALE_LOSS_ORDER(51),                // 线上损耗订单
        OFFLINE_OFFLINE_ORDER(60),          // 线下销售订单
        OFFLINE_RETURN_ORDER(61);           // 线下销售退货订单

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
                case 10:
                    return STORAGE_PURCHASE_IN_ORDER;
                case 11:
                    return STORAGE_PURCHASE_OUT_ORDER;
                case 12:
                    return STORAGE_PRODUCT_IN_ORDER;
                case 13:
                    return STORAGE_PRODUCT_OUT_ORDER;
                case 14:
                    return STORAGE_AGREEMENT_IN_ORDER;
                case 15:
                    return STORAGE_AGREEMENT_OUT_ORDER;
                case 16:
                    return STORAGE_OFFLINE_IN_ORDER;
                case 17:
                    return STORAGE_OFFLINE_OUT_ORDER;
                case 18:
                    return STORAGE_DISPATCH_IN_ORDER;
                case 19:
                    return STORAGE_DISPATCH_OUT_ORDER;
                case 20:
                    return STORAGE_LOSS_ORDER;
                case 30:
                    return PRODUCT_PROCESS_ORDER;
                case 31:
                    return PRODUCT_COMPLETE_ORDER;
                case 32:
                    return PRODUCT_LOSS_ORDER;
                case 40:
                    return AGREEMENT_SHIPPED_ORDER;
                case 41:
                    return AGREEMENT_RETURN_ORDER;
                case 50:
                    return SALE_SALE_ORDER;
                case 51:
                    return SALE_LOSS_ORDER;
                case 60:
                    return OFFLINE_OFFLINE_ORDER;
            }
            return OFFLINE_RETURN_ORDER;
        }

        public int getValue() {
            return value;
        }
    }

    // 财务记录类型
    public enum FinanceAction {
        FINANCE_PURCHASE_PAY(1),        // 采购进货
        FINANCE_PURCHASE_RET(2),        // 采购退货
        FINANCE_SALE(3),                // 线上销售
        FINANCE_OFFLINE_PAY(4),         // 线下销售
        FINANCE_OFFLINE_RET(5),         // 线下退货
        FINANCE_PURCHASE_FARE(10),      // 采购物流费用
        FINANCE_OFFLINE_FARE(11),       // 线下物流费用
        FINANCE_AGREEMENT_FARE(12),     // 履约物流费用
        FINANCE_PRODUCT_FARE(13),       // 生产物流费用
        FINANCE_STORAGE_FARE(14),       // 仓储物流费用
        FINANCE_GROUP(20);              // 经营费用

        private int value = 0;

        private FinanceAction(int v) {
            this.value = v;
        }

        public static FinanceAction valueOf(int v) {
            switch (v) {
                case 1:
                    return FINANCE_PURCHASE_PAY;
                case 2:
                    return FINANCE_PURCHASE_RET;
                case 3:
                    return FINANCE_SALE;
                case 4:
                    return FINANCE_OFFLINE_PAY;
                case 5:
                    return FINANCE_OFFLINE_RET;
                case 10:
                    return FINANCE_PURCHASE_FARE;
                case 11:
                    return FINANCE_OFFLINE_FARE;
                case 12:
                    return FINANCE_AGREEMENT_FARE;
                case 13:
                    return FINANCE_PRODUCT_FARE;
                case 14:
                    return FINANCE_STORAGE_FARE;
            }
            return FINANCE_GROUP;
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
        COMPLETE_HAS(1),  // 已完成
        COMPLETE_NOT(2);  // 未完成

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

    // 销售售后类型
    public enum SaleType {
        SALE_CONST(0),  // 不变
        SALE_ADD(1),    // 增加
        SALE_SUB(2);    // 减少

        private int value = 0;

        private SaleType(int v) {
            this.value = v;
        }

        public static SaleType valueOf(int v) {
            switch (v) {
                case 0:
                    return SALE_CONST;
                case 1:
                    return SALE_ADD;
            }
            return SALE_SUB;
        }

        public int getValue() {
            return value;
        }
    }

    // 物流类型
    public enum FareType {
        FARE_WAIT(0),       // 待发货
        FARE_SHIP(1),       // 运输中
        FARE_COMPLETE(2);   // 完成

        private int value = 0;

        private FareType(int v) {
            this.value = v;
        }

        public static FareType valueOf(int v) {
            switch (v) {
                case 0:
                    return FARE_WAIT;
                case 1:
                    return FARE_SHIP;
            }
            return FARE_COMPLETE;
        }

        public int getValue() {
            return value;
        }
    }
}
