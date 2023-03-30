package com.cxb.storehelperserver.repository.model;

import com.cxb.storehelperserver.model.TMarketAccount;
import com.cxb.storehelperserver.model.TMarketCloud;
import lombok.Data;

@Data
public class MyMarketCloud {
    private Integer id;

    private Integer gid;

    private Integer mid;

    private Integer aid;

    private String account;

    private Integer cid;

    public MyMarketCloud(TMarketCloud cloud, TMarketAccount account) {
        this.id = cloud.getId();
        this.cid = cloud.getCid();
        if (null != account) {
            this.gid = account.getGid();
            this.mid = account.getMid();
            this.aid = cloud.getAid();
            this.account = account.getAccount();
        }
    }
}