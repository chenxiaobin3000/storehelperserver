package com.cxb.storehelperserver.model;

/**
 * desc: 每个model必须提供主键id
 * auth: cxb
 * date: 2022/11/29
 */
public class BaseModel {
    /**
     * desc:
     */
    private Integer id;

   /**
    * desc:
    */
    public Integer getId() {
        return id;
    }

    /**
     * desc:
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
