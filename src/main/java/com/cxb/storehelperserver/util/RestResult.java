package com.cxb.storehelperserver.util;

import java.util.HashMap;

/**
 * desc: restful 接口返回结果
 * auth: cxb
 * date: 2022/11/29
 */
public class RestResult {
    public int code = 0;
    public String msg = "成功";
    public HashMap<String, Object> data = null;

    /**
     * desc: 成功
     */
    public static RestResult ok() {
        RestResult ret = new RestResult();
        ret.data = new HashMap<>();
        return ret;
    }

    /**
     * desc: 成功
     */
    public static RestResult ok(HashMap<String, Object> data) {
        RestResult ret = new RestResult();
        ret.data = data;
        return ret;
    }

    /**
     * desc: 失败
     */
    public static RestResult fail(int code) {
        RestResult ret = new RestResult();
        ret.code = code;
        // msg
        ret.data = new HashMap<>();
        return ret;
    }

    /**
     * desc: 失败
     */
    public static RestResult fail(int code, HashMap<String, Object> data) {
        RestResult ret = new RestResult();
        ret.code = code;
        // msg
        ret.data = data;
        return ret;
    }
}
