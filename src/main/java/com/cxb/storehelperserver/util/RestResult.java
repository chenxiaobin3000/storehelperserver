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
        return new RestResult();
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
    public static RestResult fail(String msg) {
        RestResult ret = new RestResult();
        ret.code = -1;
        ret.msg = msg;
        ret.data = null;
        return ret;
    }

    /**
     * desc: 失败
     */
    public static RestResult fail(String msg, HashMap<String, Object> data) {
        RestResult ret = new RestResult();
        ret.code = -1;
        ret.msg = msg;
        ret.data = data;
        return ret;
    }

    /**
     * desc: 失败
     */
    public static RestResult fail(int code, String msg, HashMap<String, Object> data) {
        RestResult ret = new RestResult();
        ret.code = code;
        ret.msg = msg;
        ret.data = data;
        return ret;
    }
}
