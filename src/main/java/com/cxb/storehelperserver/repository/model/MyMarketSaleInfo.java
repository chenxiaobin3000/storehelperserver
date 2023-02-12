package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MyMarketSaleInfo {
    private Integer cid;

    private Integer value;

    private BigDecimal total;

    private Date cdate;
}