package org.example.bot.accountBot.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;


public class DateUtils {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String parseDate(Date date){
        String dateDay = dateFormat.format(date);
        return dateDay;
    }
    /**
     * 将秒数转换为时间字符串，根据不同的时间单位进行格式化。
     *
     * @param seconds 秒数
     * @return 格式化后的时间字符串
     */
    public static String formatDuration(long seconds) {
        if (seconds >= 3600) { // 超过1小时
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            return hours + "小时 " + minutes + "分钟 " + remainingSeconds + "秒";
        } else if (seconds >= 60) { // 超过1分钟
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分钟 " + remainingSeconds + "秒";
        } else {
            return seconds + "秒";
        }
    }
    public static Date calculateRenewalDate(Date validTime, long daysToAdd, ZoneId zoneId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseTime;

        if (validTime == null || validTime.before(Date.from(now.atZone(zoneId).toInstant()))) {
            baseTime = now;
        } else {
            baseTime = validTime.toInstant().atZone(zoneId).toLocalDateTime();
        }

        LocalDateTime renewalTime = baseTime.plusDays(daysToAdd);
        return Date.from(renewalTime.atZone(zoneId).toInstant());
    }

    /**
     * 将Duration对象转换为时间字符串，根据不同的时间单位进行格式化。
     *
     * @param duration Duration对象
     * @return 格式化后的时间字符串
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        return formatDuration(seconds);
    }
    private static int sequence = 0;
    /**
     * 生成订单号
     * @return
     */
    public synchronized String generateOrderNumber() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String datePart = dateFormat.format(new Date());
        sequence = (sequence + 1) % 1000; // 限制序列号在0-999之间
        return String.format("%s%03d", datePart, sequence);
    }

    /**
     * 获取Duration对象的小时部分。
     *
     * @param duration Duration对象
     * @return 小时部分
     */
    public static long getHours(Duration duration) {
        return duration.toHours();
    }

    /**
     * 获取Duration对象的分钟部分（不包括小时部分）。
     *
     * @param duration Duration对象
     * @return 分钟部分
     */
    public static long getMinutesPart(Duration duration) {
        return duration.toMinutes();
    }

    /**
     * 获取Duration对象的秒部分（不包括分钟和小时部分）。
     *
     * @param duration Duration对象
     * @return 秒部分
     */
    public static long getSecondsPart(Duration duration) {
        return duration.getSeconds();//%60
    }

    public Date setHours(Date now, Integer min) {
        // 获取当前时间并加上指定的小时数
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, min);
        return calendar.getTime();
    }
}
