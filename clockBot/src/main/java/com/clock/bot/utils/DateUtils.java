package com.clock.bot.utils;
import java.time.Duration;



public class DateUtils {

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

    }
