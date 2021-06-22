package com.bird.sso.utils;

import com.bird.sso.api.ex.SSOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/24 14:45
 */
@Slf4j
public final class DateUtils {

    public static final String formatYYYYMM = "yyyy-MM";

    public static final String formatYYYYMMDD = "yyyy-MM-dd";

    public static final String formatYYYYMMDDHHmm = "yyyy-MM-dd HH:mm";

    public static final String formatYYYYMMDDHHmmss = "yyyy-MM-dd HH:mm:ss";







    public static String getDateYYYYMMDDHHmm(Date date) {
        return DateFormatUtils.format(date, formatYYYYMMDDHHmm);
    }


    public static String getDateYYYYMMDD(Date date) {
        return DateFormatUtils.format(date, formatYYYYMMDD);
    }


    public static String getDateYYYYMM(Date date) {
        return DateFormatUtils.format(date, formatYYYYMM);
    }


    /**
     * 时间差 （秒）
     *
     * @param time
     * @return
     */
    public static int interval(Date time) {
        return (int) ((Date.from(Instant.now()).getTime() - time.getTime()) / 1000);
    }



    /**
     * 与当前时间比较
     * true ： 小于当前时间
     *
     * @param time
     * @return
     */
    public static boolean compare(Date time) {
        Long currentTime = System.currentTimeMillis();
        return currentTime >= time.getTime();
    }


    public static boolean compare(String time) {
        Long currentTime = System.currentTimeMillis();
        Date date;
        try {
            date = org.apache.commons.lang3.time.DateUtils.parseDate(time, formatYYYYMMDDHHmm);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            throw new SSOException(e.getMessage(),e);
        }
        return currentTime >= date.getTime();
    }


    public static boolean compareGt(Date aTime ,Date bTime) {
        return aTime.getTime() > bTime.getTime();
    }

    public static boolean compareEGt(Date aTime ,Date bTime) {
        return aTime.getTime() >= bTime.getTime();
    }


    public static boolean compareLt(Date aTime ,Date bTime) {
        return aTime.getTime() < bTime.getTime();
    }




    public static String dateTimeJoinYYYYMMDDHHMM(Date date, String time) {
        String dateYYYYMMDD = getDateYYYYMMDD(date);
        String join = StringUtils.join(dateYYYYMMDD, " ", time);
        return join;
    }


    /**
     * 获取指定日期当月的第一天
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static String getFirstDayOfGivenMonth(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 0);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 获取指定日期下个月的第一天
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static String getFirstDayOfNextMonth(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 1);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 获取当前时间按照指定格式
     *
     * @param format
     * @return String
     */
    public static String getNowDateFormat(String format) {
        if (format == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }


    /**
     * 获取指定时间，指定前面多少小时的时间
     *
     * @param ihour
     * @return
     */
    public static Date getBeforeHourTime(Date dateTime, int ihour) {
        Calendar calendar = Calendar.getInstance();  //Calendar.HOUR_OF_DAY 改一下就是分钟 、天 、月
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - ihour);
        return calendar.getTime();
    }


}
