package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MyMarketReport {
    private Integer id;

    private Integer type;

    private Integer cid;

    private Integer value;

    private BigDecimal price;

    private BigDecimal total;

    private Date cdate;
}