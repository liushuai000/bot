package org.example.bot.accountBot.utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//此类记载所有常量
public class BaseConstant {
    //SettingOperatorPerson也需要加 同步的
    public final static String[] array={
            "通知","设置日切","开启日切","关闭日切","显示操作人名字","显示操作人名称", "设置费率",
            "设置汇率","设置入款单笔手续费","取消",
            "删除操作员", "删除操作人","设置操作员", "设置操作人", "显示操作人", "显示操作员",
            "将操作员显示","关闭显示",   "将回复人显示","显示回复人名称", "关闭回复人显示","隐藏回复人显示",
            "设置入款单笔手续费","设置入款手续费","设置下发手续费", "设置下发单笔手续费","显示手续费","隐藏手续费",
            "设置单笔下发手续费","设置单笔入款手续费"
    };
    //这个是需要显示账单的
    public final static String[] showArray={
            "设置手续费","下发",
            "显示明细",  "隐藏明细",  "显示操作人名称","显示操作人名字","隐藏操作人名称","隐藏操作人名字",
            "隐藏名字","隐藏名称","将回复人显示","显示回复人名称","显示余额","显示USDT","显示usdt","显示全部",
            "显示1条","显示3条","显示5条", "+0","-0","+0u","-0u","+0U","-0U","显示分类",
            "清理今天数据", "删除今天数据","清理今天账单","清理今日账单","删除今日账单","清理今天帐单","删除今天账单",
            "删除账单", "删除今天帐单","删除帐单","清除账单","清除帐单", "删除全部账单","删除全部帐单", "删除全部账单","清除全部账单",
            "撤销下发","撤销入款",
    };

    /**
     * 判断群组内消息是否包含机器人识别的消息在回复
     * @param text
     * @return
     */
    public static boolean getMessageContentIsContain(String text) {
        // 合并两个数组
        String[] combinedArray = Arrays.copyOf(array, array.length + showArray.length);
        System.arraycopy(showArray, 0, combinedArray, array.length, showArray.length);
        boolean b = equalsAny(array, text);
        boolean b1 = containsAny(showArray, text);
        boolean temp;
        if (b){
            temp= true;
        }else {
            temp = b1;
        }
        return temp;
    }
    //判断群组内消息 是否直接显示账单
    public static boolean showReplay(String text) {
        return  containsAny(showArray, text);//包含0  equals0
    }
    public static boolean equalsAny(String[] array, String input) {
        for (String str : array) {
            if (input.contains(str)) {
                return true; // 如果input包含array中的某个元素，则返回true
            }
        }
        return false; // 如果没有任何元素匹配，返回false
    }
    public static boolean containsAny(String[] array, String input) {
        for (String str : array) {
            if(input.contains(array[0])||input.contains(array[1])){
                return true;
            }
            if (input.equals(str)) {
                return true; // 如果input包含array中的某个元素，则返回true
            }
        }
        return false; // 如果没有任何元素匹配，返回false
    }
    //如果有下发替换成-
    public static String isXiFa(String text){
        // 定义正则表达式
        Pattern pattern = Pattern.compile("下发(\\d+)(u)?");
        Matcher matcher = pattern.matcher(text);

        // 使用 StringBuffer 来构建替换后的字符串
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String number = matcher.group(1); // 提取数字部分
            String unit = matcher.group(2); // 提取单位部分（可能为空）
            String replacement = "-" + number + (unit != null ? unit : "");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return  sb.toString();
    }
}