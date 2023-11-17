package com.cxb.storehelperserver.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * desc:
 * auth: cxb
 * date: 2023/3/16
 */
@Data
public class PageData {
    private int total;

    private ArrayList<HashMap<String, Object>> list;

    public PageData() {
        this.total = 0;
        this.list = null;
    }

    public PageData(int total, ArrayList<HashMap<String, Object>> list) {
        this.total = total;
        this.list = list;
    }
}
