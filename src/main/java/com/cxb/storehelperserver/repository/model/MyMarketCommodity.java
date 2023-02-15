package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class MyMarketCommodity {
    private Integer id;

    private String code;

    private String name;

    private Integer cid;

    private Integer unit;

    private String remark;

    private Integer mcid;

    private String mname;

    private BigDecimal alarm;

    private ArrayList<String> attrs;
}