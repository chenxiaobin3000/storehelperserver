package com.cxb.storehelperserver.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

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
     * desc: 是否成功
     */
    public static boolean isOk(RestResult ret) {
        return 0 == ret.code;
    }

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
     * desc: 成功
     */
    public static RestResult ok(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = mapper.convertValue(data, HashMap.class);
        RestResult ret = new RestResult();
        ret.data = map;
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
    public static RestResult fail(int code, String msg) {
        RestResult ret = new RestResult();
        ret.code = code;
        ret.msg = msg;
        ret.data = null;
        return ret;
    }
}
