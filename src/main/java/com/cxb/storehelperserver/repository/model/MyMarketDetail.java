package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyMarketDetail {
    private Integer id;

    private Integer cid;

    private String mname;

    private Integer value;

    private BigDecimal mprice;

    private String code;

    private String name;

    private BigDecimal price;
}