package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 订单类型
    public enum OrderType {
        AGREEMENT_ORDER(1), // 履约订单
        PRODUCT_ORDER(2), // 生产订单
        STORAGE_ORDER(3); // 进货订单

        private int value = 0;

        private OrderType(int v) {
            this.value = v;
        }

        public static OrderType valueOf(int v) {
            switch (v) {
                case 1:
                    return AGREEMENT_ORDER;
                case 2:
                    return PRODUCT_ORDER;
            }
            return STORAGE_ORDER;
        }
    }

    // 上传附件类型
    public enum AttachType {
        ATTACHMENT_COMM_IN_ORDER(1), // 商品入库订单
        ATTACHMENT_COMM_OUT_ORDER(2), // 商品出库订单
        ATTACHMENT_ORI_IN_ORDER(3), // 原料入库订单
        ATTACHMENT_ORI_OUT_ORDER(4); // 原料出库订单

        private int value = 0;

        private AttachType(int v) {
            this.value = v;
        }

        public static AttachType valueOf(int v) {
            switch (v) {
                case 1:
                    return ATTACHMENT_COMM_IN_ORDER;
                case 2:
                    return ATTACHMENT_COMM_OUT_ORDER;
                case 3:
                    return ATTACHMENT_ORI_IN_ORDER;
            }
            return ATTACHMENT_ORI_OUT_ORDER;
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
    }
}
