package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyStockStandard {
    private Integer id;

    private Integer unit;

    private Integer value;

    private Integer total;

    private BigDecimal price;

    private Integer cid;

    private String code;

    private String name;

    private Integer ctid;

    private String remark;
}