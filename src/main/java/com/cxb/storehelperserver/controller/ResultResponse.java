package com.cxb.storehelperserver.controller;

import java.util.HashMap;

/**
 *
 */
public class ResultResponse {
    /**
     * 成功
     */
    public static Result ok() {
        Result ret = new Result();
        ret.data = new HashMap<>();
        return ret;
    }

    /**
     * 成功
     * @param data
     */
    public static Result ok(HashMap<String, Object> data) {
        Result ret = new Result();
        ret.data = data;
        return ret;
    }

    /**
     * 失败
     * @param code
     */
    public static Result fail(int code) {
        Result ret = new Result();
        ret.code = code;
        // msg
        ret.data = new HashMap<>();
        return ret;
    }

    /**
     * 失败
     *
     * @param code
     * @param data
     */
    public static Result fail(int code, HashMap<String, Object> data) {
        Result ret = new Result();
        ret.code = code;
        // msg
        ret.data = data;
        return ret;
    }
}
