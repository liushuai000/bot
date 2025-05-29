package org.example.bot.accountBot.utils;

import java.util.*;

public class ConstantMap {

    // 命令映射：中文 -> 英文
    public static final Map<String, String> COMMAND_MAP = new LinkedHashMap<>();

    static {
        // 初始化映射关系
        put("通知", "Notice");
        put("设置日切", "Set Daily Switch");
        put("开启日切", "Enable Daily Switch");
        put("关闭日切", "Disable Daily Switch");
        put("设置费率", "Set Rate");
        put("通知所有人", "Notify All");

        put("设置汇率", "Set Exchange Rate");
        put("设置入款单笔手续费", "Set Deposit Fee per Transaction");
        put("取消", "Cancel");

        put("删除操作员", "Delete Operator");
        put("删除操作人", "Delete Operator");
        put("设置操作员", "Set Operator");
        put("设置操作人", "Set Operator");

        put("关闭回复人显示", "Disable Replier Display");
        put("隐藏回复人显示", "Hide Replier Display");

        put("设置入款手续费", "Set Deposit Fee");
        put("设置下发手续费", "Set Withdrawal Fee");
        put("设置下发单笔手续费", "Set Withdrawal Fee per Transaction");
        put("设置单笔下发手续费", "Set Withdrawal Fee per Transaction");
        put("设置单笔入款手续费", "Set Deposit Fee per Transaction");

        // showArray 相关
        put("设置手续费", "Set Fee");
        put("下发", "Withdrawal");
        put("显示明细", "Show Details");
        put("隐藏明细", "Hide Details");
        put("显示操作人名称", "Show Operator Name");
        put("将操作员显示", "Show Operator");
        put("显示操作人名字", "Show Operator Name");
        put("隐藏操作人名称", "Hide Operator Name");
        put("隐藏操作人名字", "Hide Operator Name");
        put("显示操作人", "Show Operator");
        put("显示操作员", "Show Operator");

        put("隐藏名字", "Hide Names");
        put("隐藏名称", "Hide Titles");
        put("关闭显示", "Turn Off Display");
        put("将回复人显示", "Show Replier");
        put("显示回复人名称", "Show Replier Name");
        put("显示余额", "Show Balance");
        put("显示金额", "Show Amount");
        put("显示USDT", "Show USDT");
        put("显示usdt", "Show USDT");
        put("显示全部", "Show All");

        put("显示1条", "Show 1 Entry");
        put("显示3条", "Show 3 Entries");
        put("显示5条", "Show 5 Entries");
        put("+0", "+0");
        put("-0", "-0");
        put("+0u", "+0u");
        put("-0u", "-0u");
        put("+0U", "+0U");
        put("-0U", "-0U");
        put("显示分类", "Show Category");
        put("隐藏分类", "Hide Category");

        put("清理今天数据", "Clear Today's Data");
        put("删除今天数据", "Delete Today's Data");
        put("清理今天账单", "Clear Today's Bill");
        put("清理今日账单", "Clear Today's Bill");
        put("删除今日账单", "Delete Today's Bill");
        put("清理今天帐单", "Clear Today's Record");
        put("删除今天账单", "Delete Today's Bill");

        put("删除账单", "Delete Bill");
        put("删除今天帐单", "Delete Today's Record");
        put("删除帐单", "Delete Record");
        put("清除账单", "Clear Bill");
        put("清除帐单", "Clear Record");
        put("删除全部账单", "Delete All Bills");
        put("删除全部帐单", "Delete All Records");
        put("清除全部账单", "Clear All Bills");

        put("撤销下发", "Revoke Withdrawal");
        put("撤销入款", "Revoke Deposit");
        put("显示手续费", "Show Fee");
        put("隐藏手续费", "Hide Fee");
    }

    private static void put(String zh, String en) {
        COMMAND_MAP.put(zh, en);
    }
    public String get(String zh) {
        return COMMAND_MAP.get(zh);
    }
    /**
     * 获取所有命令的详细信息，格式为：
     * 中文命令==英文命令，每行一个
     */
    public String getAllCommandsDetail() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : COMMAND_MAP.entrySet()) {
            sb.append(entry.getKey()).append("==").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }


    /**
     * 获取所有中文命令
     */
    public static Set<String> getChineseCommands() {
        return COMMAND_MAP.keySet();
    }

    /**
     * 获取所有英文命令
     */
    public static Collection<String> getEnglishCommands() {
        return COMMAND_MAP.values();
    }

    /**
     * 根据中文获取英文
     */
    public static String getEnglish(String chinese) {
        return COMMAND_MAP.get(chinese);
    }

    /**
     * 判断输入是否匹配任何命令（中英文）
     */
    public static boolean isMatchCommand(String input) {
        return COMMAND_MAP.containsKey(input) || COMMAND_MAP.containsValue(input);
    }

    /**
     * 判断输入是否是中文命令
     */
    public static boolean isChineseCommand(String input) {
        return COMMAND_MAP.containsKey(input);
    }

    /**
     * 判断输入是否是英文命令
     */
    public static boolean isEnglishCommand(String input) {
        return COMMAND_MAP.containsValue(input);
    }

    /**
     * 获取对应的命令（自动识别中英文）
     */
    public static String getCorrespondingCommand(String input) {
        if (COMMAND_MAP.containsKey(input)) {
            return COMMAND_MAP.get(input);
        } else if (COMMAND_MAP.containsValue(input)) {
            for (Map.Entry<String, String> entry : COMMAND_MAP.entrySet()) {
                if (entry.getValue().equals(input)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }





}
