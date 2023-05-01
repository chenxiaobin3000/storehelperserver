package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MyStockCommodity {
    private Integer id;

    private Integer gid;

    private Integer sid;

    private Integer aid;

    private BigDecimal price;

    private Integer weight;

    private Integer value;

    private Date date;

    private Integer cid;

    private String code;

    private String name;

    private Integer ctid;

    private String remark;
}