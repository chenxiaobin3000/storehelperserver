package com.cxb.storehelperserver.util;

/**
 * desc:
 * auth: cxb
 * date: 2023/1/11
 */
public class TypeDefine {
    // 上传附件类型
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
}
