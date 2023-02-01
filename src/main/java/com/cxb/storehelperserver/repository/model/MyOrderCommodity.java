package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyOrderCommodity {
    private Integer id;

    private Integer oid;

    private Integer cid;

    private Integer ctype;

    private Integer unit;

    private Integer value;

    private BigDecimal price;

    private Boolean io;
}