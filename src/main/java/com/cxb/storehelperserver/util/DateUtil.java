package com.cxb.storehelperserver.util;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /**
     * desc: 当前日期加N天
     */
    public Date addOneDay(Date date, int day) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * desc: 获取当天00：00：00的时间戳
     */
    public Date getStartTime(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * desc: 获取当天23：59：59的时间戳
     */
    public Date getEndTime(Date date) {
        if (null == date) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
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
