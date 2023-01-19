package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 订单类型
    public enum OrderType {
        STORAGE_ORIGINAL_IN_ORDER(1), // 进货原料入库订单
        STORAGE_STANDARD_IN_ORDER(2), // 进货标品入库订单
        PRODUCT_ORIGINAL_OUT_ORDER(3), // 生产原料出库订单
        PRODUCT_ORIGINAL_IN_ORDER(4), // 生产原料入库订单
        PRODUCT_HALFGOOD_IN_ORDER(5), // 生产半成品入库订单
        PRODUCT_COMMODITY_IN_ORDER(6), // 生产商品入库订单
        AGREEMENT_COMMODITY_OUT_ORDER(7), // 履约商品出库订单
        AGREEMENT_STANDARD_OUT_ORDER(8), // 履约标品出库订单
        AGREEMENT_COMMODITY_IN_ORDER(9), // 履约商品入库订单
        AGREEMENT_STANDARD_IN_ORDER(10); // 履约标品入库订单

        private int value = 0;

        private OrderType(int v) {
            this.value = v;
        }

        public static OrderType valueOf(int v) {
            switch (v) {
                case 1:
                    return STORAGE_ORIGINAL_IN_ORDER;
                case 2:
                    return STORAGE_STANDARD_IN_ORDER;
                case 3:
                    return PRODUCT_ORIGINAL_OUT_ORDER;
                case 4:
                    return PRODUCT_ORIGINAL_IN_ORDER;
                case 5:
                    return PRODUCT_HALFGOOD_IN_ORDER;
                case 6:
                    return PRODUCT_COMMODITY_IN_ORDER;
                case 7:
                    return AGREEMENT_COMMODITY_OUT_ORDER;
                case 8:
                    return AGREEMENT_STANDARD_OUT_ORDER;
                case 9:
                    return AGREEMENT_COMMODITY_IN_ORDER;
            }
            return AGREEMENT_STANDARD_IN_ORDER;
        }

        public int getValue() {
            return value;
        }
    }

    // 报表周期类型
    public enum ReportCycleType {
        REPORT_DAILY(1), // 每日报表
        REPORT_WEEKLY(2), // 每周报表
        REPORT_MONTH(3), // 月报表
        REPORT_QUARTER(4), // 季度报表
        REPORT_HALF(5), // 半年报表
        REPORT_YEARLY(6); // 年度报表

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
                case 4:
                    return REPORT_QUARTER;
                case 5:
                    return REPORT_HALF;
            }
            return REPORT_YEARLY;
        }

        public int getValue() {
            return value;
        }
    }
}
