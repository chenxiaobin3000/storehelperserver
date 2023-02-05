package com.cxb.storehelperserver.repository.model;

import lombok.Data;

import java.util.Date;

@Data
public class MyUserOrderComplete {
    private Integer cnum;

    private Integer ctotal;

    private Integer otype;

    private Date cdate;
}