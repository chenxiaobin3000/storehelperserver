package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MyMarketCommodity {
    private Integer id;

    private Integer sid;

    private Integer mid;

    private Integer cid;

    private String code;

    private String name;

    private String remark;

    private BigDecimal alarm;

    private BigDecimal price;

    private Integer value;

    private Date cdate;
}