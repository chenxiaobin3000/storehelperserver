package com.cxb.storehelperserver.repository.model;

import com.cxb.storehelperserver.model.TMarketAccount;
import com.cxb.storehelperserver.model.TMarketStorage;
import lombok.Data;

@Data
public class MyMarketStorage {
    private Integer id;

    private Integer gid;

    private Integer mid;

    private Integer aid;

    private String account;

    private String remark;

    private Integer cid;

    public MyMarketStorage(TMarketStorage storage, TMarketAccount account) {
        this.id = storage.getId();
        this.cid = storage.getCid();
        if (null != account) {
            this.gid = account.getGid();
            this.mid = account.getMid();
            this.aid = storage.getAid();
            this.account = account.getAccount();
            this.remark = account.getRemark();
        }
    }
}