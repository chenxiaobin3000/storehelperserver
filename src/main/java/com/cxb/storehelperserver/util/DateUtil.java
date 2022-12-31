package com.cxb.storehelperserver.util;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * desc: 日期工具
 * auth: cxb
 * date: 2017/5/4
 */
@Component
public class DateUtil {
    final private SimpleDateFormat dateFormat;
    final private SimpleDateFormat simpleDateFormat;

    public DateUtil() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public int getSecondTimestamp(Date date) {
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return Integer.parseInt(timestamp.substring(0, length - 3));
        } else {
            return 0;
        }
    }
}
