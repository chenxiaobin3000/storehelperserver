package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyStockDetail {
    private Integer id;

    private Integer gid;

    private Integer sid;

    private BigDecimal price;

    private Integer weight;

    private Integer value;

    private Integer total;

    private Integer cid;

    private String code;

    private String name;

    private Integer ctid;

    private String remark;
}