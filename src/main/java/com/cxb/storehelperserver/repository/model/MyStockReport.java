package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.util.Date;

@Data
public class MyStockReport {
    private Integer id;

    private Integer total;

    private Date cdate;
}