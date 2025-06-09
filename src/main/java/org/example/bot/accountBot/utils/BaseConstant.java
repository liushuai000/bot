package org.example.bot.accountBot.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//此类记载所有常量
public class BaseConstant {
    // 命令映射：中文 -> 英文
    static Map<String, String> constantMap = ConstantMap.COMMAND_MAP_ENGLISH;
    // 英文命令常量数组示例
    public final static String[] arrayEnglish = {
            constantMap.get("通知"),
            constantMap.get("设置日切"),
            constantMap.get("开启日切"),
            constantMap.get("关闭日切"),
            constantMap.get("设置费率"),
            constantMap.get("通知所有人"),

            constantMap.get("设置汇率"),
            constantMap.get("设置入款单笔手续费"),
            constantMap.get("取消"),

            constantMap.get("删除操作员"),
            constantMap.get("删除操作人"),
            constantMap.get("设置操作员"),
            constantMap.get("设置操作人"),

            constantMap.get("关闭回复人显示"),
            constantMap.get("隐藏回复人显示"),

            constantMap.get("设置入款单笔手续费"),
            constantMap.get("设置入款手续费"),
            constantMap.get("设置下发手续费"),
            constantMap.get("设置下发单笔手续费"),
            constantMap.get("设置单笔下发手续费"),
            constantMap.get("设置单笔入款手续费")
    };

    public final static String[] showArrayEnglish = {
            constantMap.get("设置手续费"),
            constantMap.get("下发"),
            constantMap.get("入款"),
            constantMap.get("取消"),

            constantMap.get("显示明细"),
            constantMap.get("隐藏明细"),
            constantMap.get("显示操作人名称"),
            constantMap.get("将操作员显示"),
            constantMap.get("显示操作人名字"),
            constantMap.get("隐藏操作人名称"),
            constantMap.get("隐藏操作人名字"),
            constantMap.get("显示操作人"),
            constantMap.get("显示操作员"),

            constantMap.get("隐藏名字"),
            constantMap.get("隐藏名称"),
            constantMap.get("关闭显示"),
            constantMap.get("将回复人显示"),
            constantMap.get("显示回复人名称"),
            constantMap.get("显示余额"),
            constantMap.get("显示金额"),
            constantMap.get("显示USDT"),
            constantMap.get("显示usdt"),
            constantMap.get("显示全部"),

            constantMap.get("显示1条"),
            constantMap.get("显示3条"),
            constantMap.get("显示5条"),
            constantMap.get("+0"),
            constantMap.get("-0"),
            constantMap.get("+0u"),
            constantMap.get("-0u"),
            constantMap.get("+0U"),
            constantMap.get("-0U"),
            constantMap.get("显示分类"),
            constantMap.get("隐藏分类"),

            constantMap.get("清理今天数据"),
            constantMap.get("删除今天数据"),
            constantMap.get("清理今天账单"),
            constantMap.get("清理今日账单"),
            constantMap.get("删除今日账单"),
            constantMap.get("清理今天帐单"),
            constantMap.get("删除今天账单"),

            constantMap.get("删除账单"),
            constantMap.get("删除今天帐单"),
            constantMap.get("删除帐单"),
            constantMap.get("清除账单"),
            constantMap.get("清除帐单"),
            constantMap.get("删除全部账单"),
            constantMap.get("删除全部帐单"),
            constantMap.get("删除全部账单"),
            constantMap.get("清除全部账单"),

            constantMap.get("撤销下发"),
            constantMap.get("撤销入款"),
            constantMap.get("显示手续费"),
            constantMap.get("隐藏手续费")
    };
    //SettingOperatorPerson也需要加 同步的
    public final static String[] array={
            "通知","设置日切","开启日切","关闭日切","设置费率","通知所有人",
            "设置汇率","设置入款单笔手续费","取消",
            "删除操作员", "删除操作人","设置操作员", "设置操作人",
            "设置入款单笔手续费","设置入款手续费","设置下发手续费", "设置下发单笔手续费",
            "设置单笔下发手续费","设置单笔入款手续费","设置下发费率","设置下发汇率",
            "切换英文", "切换中文","设置下发地址","修改下发地址","查看下发地址",
    };
    //这个是需要显示账单的第一行都是contains 其余行都是equals 因为第一行有  下发-30.equals(下发)
    public final static String[] showArray={
            "设置手续费","下发",
            "显示明细",  "隐藏明细",  "显示操作人名称","将操作员显示","显示操作人名字","隐藏操作人名称","隐藏操作人名字","显示操作人", "显示操作员",
            "隐藏名字","隐藏名称","关闭显示","将回复人显示","显示回复人名称","显示余额","显示金额","显示USDT","显示usdt","显示全部",
            "显示1条","显示3条","显示5条", "+0","-0","+0u","-0u","+0U","-0U","显示分类","隐藏分类","取消","账单","显示账单",
            "清理今天数据", "删除今天数据","清理今天账单","清理今日账单","删除今日账单","清理今天帐单","删除今天账单",
            "删除账单", "删除今天帐单","删除帐单","清除账单","清除帐单", "删除全部账单","删除全部帐单", "删除全部账单","清除全部账单",
            "撤销下发","撤销入款","显示手续费","隐藏手续费","关闭回复人显示","隐藏回复人显示","隐藏回复人名称","D","T","issue","P"
    };
    public final static String[] arrayEnglish2 = {
            "notify", "Set daily cut-off", "Enable Daily Switch", "Disable Daily Switch", "Set Rate", "Notify All",
            "Set Exchange Rate", "Set Single Deposit Fee", "Cancel",
            "Delete Operator", "Delete Operator", "Set Operator", "Set Operator Person",
            "Set Single Deposit Fee","Set Deposit Fee","Set Withdrawal Fee","Set Single Withdrawal Fee",
            "Set Single Withdrawal Fee","Set Single Deposit Fee","Set the exchange rate", "Set the delivery rate",
            "Switch to Chinese","Switch to English", "Set the delivery address","Modify the delivery address","View the sending address","Sending address",
    };

    public final static String[] showArrayEnglish2 = {
            "Setup fee", "Withdraw",
            "Show Details", "Hide Details", "Show Operator Name", "Show Operator Names", "Show Operator", "Hide Operator Name",
            "Hide Name","Hide Name", "Hide Titles","Hide Display","Show Replyer","Show Replyer Name",
            "Hide All", "Show Replier", "Show Replier Name", "Show Balance", "Display amount", "Show USDT", "Show All",
            "Show 1 item", "Show 3 item", "Show 5 item", "+0", "-0", "+0u", "-0u", "+0U", "-0U", "Show categories", "Hide categories","Cancel","Bill","Show Bill","View Bill",
            "Clear Today Data","Delete Today Data","Clear Today Bill","Delete Today Bill",
            "Delete Bill","Clear Bill","Delete All Bills","Clear All Bills","Undo delivery","Cancel deposit",
            "Show handling fee","Hidden fees","Bill","View Bill","Show Bill","Hide Replyer Display", "Hide Replyer Info","Hide reply name",
            "D","T","issue","P"
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
        boolean b1 = startsWithAny(showArray, text);
        boolean temp;
        if (b){
            temp= true;
        }else {
            temp = b1;
        }
        if (text.startsWith("设置手续费")||text.toLowerCase().contains("setup fee")){
            return true;
        }
        if (text.startsWith("下发")||text.toLowerCase().contains("withdraw")){
            return true;
        }
        if (text.startsWith("入款")||text.toLowerCase().contains("deposit")){
            return true;
        }
        return temp;
    }
    public static boolean getMessageContentIsContainEnglish2(String text) {
        text=text.toLowerCase();
        // 合并两个数组
        String[] combinedArray = Arrays.copyOf(arrayEnglish2, arrayEnglish2.length + showArrayEnglish2.length);
        System.arraycopy(showArrayEnglish2, 0, combinedArray, arrayEnglish2.length, showArrayEnglish2.length);
        boolean b = equalsAny(arrayEnglish2, text);
        boolean b1 = startsWithAny(showArrayEnglish2, text);
        boolean temp;
        if (b){
            temp= true;
        }else {
            temp = b1;
        }
        if (text.startsWith("设置手续费")||text.toLowerCase().contains("setup fee")){
            return true;
        }
        if (text.startsWith("下发")||text.toLowerCase().contains("withdraw")){
            return true;
        }
        if (text.startsWith("入款")||text.toLowerCase().contains("deposit")){
            return true;
        }
        return temp;
    }
    //判断群组内消息 是否直接显示账单
    public static boolean showReplay(String text) {
        return  startsWithAny(showArray, text);//包含0  equals0
    }
    public static boolean showReplayEnglish(String text) {
        return  startsWithAny(showArrayEnglish, text);//包含0  equals0
    }
    public static boolean showReplayEnglish2(String text) {
        return  startsWithAny(showArrayEnglish2, text);//包含0  equals0
    }
    public static boolean equalsAny(String[] array, String input) {
        for (String str : array) {
            if (input.contains(str.toLowerCase())) {
                return true; // 如果input包含array中的某个元素，则返回true
            }
        }
        return false; // 如果没有任何元素匹配，返回false
    }
    public static boolean startsWithAny(String[] array, String input) {
        for (String str : array) {
            str=str.toLowerCase();
            if (array[0]==null){
                System.err.println(input+"-"+array[0]+" - "+array[1]);
            }
            if (input.startsWith("设置下发费率")||input.startsWith("设置下发汇率")
                    ||input.toLowerCase().contains("set the exchange rate")||input.toLowerCase().contains("set the delivery rate")){
                return false;
            }
//            if (input.startsWith("t-")||input.startsWith("d-")||input.startsWith("issue")){
//                return true;
//            }
            if(input.startsWith(str)){
                return true;
            }
            if (input.equals(str)) {
                return true; // 如果input包含array中的某个元素，则返回true
            }
        }
        return false; // 如果没有任何元素匹配，返回false
    }
//    public static String isXiFa(String text) {
//        if (text == null || text.isEmpty()) return text;
//        // 判断是否包含 “下发” 或 “withdraw”
//        if (text.toLowerCase().contains("下发") || text.toLowerCase().contains("withdraw")) {
//            // 提取数字和单位
//            Pattern pattern = Pattern.compile("([+\\-]?\\d+)(u)?", Pattern.CASE_INSENSITIVE);
//            Matcher matcher = pattern.matcher(text);
//            if (matcher.find()) {
//                String number = matcher.group(1);
//                String unit = matcher.group(2);
//                return "-" + number + (unit != null ? unit : "");
//            }
//        }
//        return text; // 不匹配则原样返回
//    }
public static void main(String[] args) {
    System.out.println(isXiFa("下发1789.92"));
}
    public static String isXiFa(String text) {
        if (text == null || text.isEmpty()) return text;
        // 判断是否包含 “下发” 或 “withdraw”
        if (text.toLowerCase().contains("下发") || text.toLowerCase().contains("withdraw")) {
            // 支持小数、负号、单位 u/U
            Pattern pattern = Pattern.compile("([+\\-]?(?:\\d+\\.?\\d*))([uU])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String number = matcher.group(1);   // 比如 "1789.92"
                String unit = matcher.group(2);     // 比如 "u"
                return "-" + number + (unit != null ? unit.toLowerCase() : "");
            }
        }
        return text; // 不匹配则原样返回
    }

    public static String isRuKuan(String text) {
        if (text == null || text.isEmpty()) return text;
        // 判断是否包含 “入款” 或 “deposit”
        if (text.toLowerCase().contains("入款") || text.toLowerCase().contains("deposit")) {
            // 支持小数、负号、单位 u/U
            Pattern pattern = Pattern.compile("([+\\-]?(?:\\d+\\.?\\d*))([uU])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String number = matcher.group(1);   // 比如 "-300.5"
                String unit = matcher.group(2);     // 比如 "U"
                return "+" + number + (unit != null ? unit.toLowerCase() : "");
            }
        }
        return text; // 不匹配则原样返回
    }




}
