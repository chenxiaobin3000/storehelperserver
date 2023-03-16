package com.cxb.storehelperserver.service.model;

import com.cxb.storehelperserver.model.TProductCommodity;
import lombok.Data;

/**
 * desc: 原料索引信息
 * auth: cxb
 * date: 2023/3/16
 */
@Data
public class OriginalIndex {
    private TProductCommodity original;

    private TProductCommodity halfgood;

    private TProductCommodity original2;

    private TProductCommodity halfgood2;

    public OriginalIndex(TProductCommodity original) {
        this.original = original;
        this.halfgood = null;
        this.original2 = null;
        this.halfgood2 = null;
    }
}
