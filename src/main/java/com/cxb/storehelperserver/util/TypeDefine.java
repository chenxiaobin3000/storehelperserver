package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 商品类型: 1商品，2原料，3半成品，4标品，5废料
    public enum CommodityType {
        COMMODITY(1), // 商品
        HALFGOOD(2), // 半成品
        ORIGINAL(3), // 原料
        STANDARD(4), // 标品
        DESTROY(5); // 废料

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
                case 4:
                    return STANDARD;
            }
            return DESTROY;
        }

        public int getValue() {
            return value;
        }
    }

    // 订单类型
    public enum OrderType {
        STORAGE_IN_ORDER(1), // 进货入库订单
        STORAGE_OUT_ORDER(2), // 进货出库订单
        PRODUCT_IN_ORDER(3), // 生产入库订单
        PRODUCT_OUT_ORDER(4), // 生产出库订单
        AGREEMENT_IN_ORDER(5), // 履约入库订单
        AGREEMENT_OUT_ORDER(6); // 履约出库订单

        private int value = 0;

        private OrderType(int v) {
            this.value = v;
        }

        public static OrderType valueOf(int v) {
            switch (v) {
                case 1:
                    return STORAGE_IN_ORDER;
                case 2:
                    return STORAGE_OUT_ORDER;
                case 3:
                    return PRODUCT_IN_ORDER;
                case 4:
                    return PRODUCT_OUT_ORDER;
                case 5:
                    return AGREEMENT_IN_ORDER;
            }
            return AGREEMENT_OUT_ORDER;
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
