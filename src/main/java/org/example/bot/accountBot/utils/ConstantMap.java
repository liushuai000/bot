package org.example.bot.accountBot.utils;

import java.util.*;

public class ConstantMap {

    // 命令映射：中文 -> 英文
    public static final Map<String, String> COMMAND_MAP = new LinkedHashMap<>();
    public static final Map<String, String> COMMAND_MAP_ENGLISH = new LinkedHashMap<>();
    public ConstantMap() {
    }

    static {
        // 初始化映射关系
        put("通知(Notice)", "TZ");
        put("设置日切(Set Daily Switch)", "SZRQ");
        put("开启日切(Enable Daily Switch)", "KQRQ");
        put("关闭日切(Disable Daily Switch)", "GBRQ");
        put("设置费率(Set Rate)", "SZFL");
        put("通知所有人(Notify All)", "TZSYR");

        put("波场下载地址", "BCXZDZ");

        put("设置汇率(Set Exchange Rate)", "SZHL");
        put("取消(Cancel)", "QX");

        put("删除操作员(Delete Operator)", "SCCZY");
        put("删除操作人(Delete Operator Person)", "SCCZR");
        put("设置操作员(Set Operator)", "SZCZY");
        put("设置操作人(Set Operator Person)", "SZCZR");

        put("关闭回复人显示(Hide Replyer Display)", "GBHFRXS");
        put("隐藏回复人显示(Hide Replyer Info)", "YCHFRXS");
        put("隐藏回复人名称(Hide Replyer name)", "YCHFRMC");
        put("查询(Query)", "CX");

        put("设置入款手续费(Set Deposit Fee)", "SZRKSXF");
        put("设置下发手续费(Set Withdrawal Fee)", "SZXFSXF");
        put("设置下发单笔手续费(Set Single Withdrawal Fee)", "SZXFDBSXF");
        put("设置单笔下发手续费(Set Single Withdrawal Fee)", "SZDBXFSXF");
        put("设置入款单笔手续费(Set Single Deposit Fee)", "SZRKDBSXF");
        put("设置单笔入款手续费(Set Single Deposit Fee)", "SZDBRKSXF");

        // showArray 相关
        put("设置手续费(Setup fee 10)0为关闭", "SZSXF");
        put("下发(Withdraw)", "XF");
        put("入款(Deposit)", "RK");
        put("显示明细(Show Details)", "XSMX");
        put("隐藏明细(Hide Details)", "YCMX");
        put("将操作员显示(Show Operator)", "JCZYXS");
        put("显示操作人名称(Show Operator Name)", "XSCZRMC");
        put("显示操作人名字(Show Operator Name)", "XSCZRMZ");
        put("隐藏操作人名称(Hide Operator Name)", "YCCZRMC");
        put("隐藏操作人名字(Hide Operator Name)", "YCCZRMZ");
        put("显示操作人(Show Operator)", "XSCZR");
        put("显示操作员(Show Operator)", "XSCZY");

        put("隐藏名字(Hide Name)", "YCMZ");
        put("隐藏名称(Hide Title)", "YCMC");
        put("关闭显示(Hide Display)", "GBXS");
        put("将回复人显示(Show Replyer)", "JHFRXS");
        put("显示回复人名称(Show Replyer Name)", "XSHFRMC");
        put("显示余额(Show Balance)", "XSYE");
        put("显示金额(Display amount)", "XSJE");
        put("显示USDT(Show USDT)", "XSUSDT");
        put("显示usdt(Show USDT)", "XSusdt");
        put("显示全部(Show All)", "XSQB");

        put("显示1条(Show 1 item)", "XS1T");
        put("显示3条(Show 3 item)", "XS3T");
        put("显示5条(Show 5 item)", "XS5T");
        put("+0(Add Zero)", "+0");
        put("-0(Subtract Zero)", "-0");
        put("+0u(Add Zero USDT)", "+0U");
        put("-0u(Subtract Zero USDT)", "-0U");
        put("+0U(Add Zero USDT)", "+0U");
        put("-0U(Subtract Zero USDT)", "-0U");
        put("显示分类(Show Category)", "XSFL");
        put("隐藏分类(Hide Category)", "YCFL");

        put("清理今天数据(Clear Today Data)", "QLJTSJ");
        put("删除今天数据(Delete Today Data)", "SCJTSJ");
        put("清理今天账单(Clear Today Bill)", "QLJTZD");
        put("清理今日账单(Clear Today Bill)", "QLJRZD");
        put("删除今日账单(Delete Today Bill)", "SCJRZD");
        put("清理今天帐单(Clear Today Bill)", "QLJTZD");
        put("删除今天账单(Delete Today Bill)", "SCJTZD");

        put("删除账单(Delete Bill)", "SCZD");
        put("删除今天帐单(Delete Today Bill)", "SCJTZD");
        put("删除帐单(Delete Bill)", "SCZD");
        put("清除账单(Clear Bill)", "QCZD");
        put("清除帐单(Clear Bill)", "QCZD");
        put("删除全部账单(Delete All Bills)", "SCQBZD");
        put("删除全部帐单(Delete All Bills)", "SCQBZD");
        put("清除全部账单(Clear All Bills)", "QCQBZD");

        put("撤销下发(Undo Withdrawal)", "CXXF");
        put("撤销入款(Undo Deposit)", "CXRK");
        put("显示手续费(Show Fee)", "XSSXF");
        put("隐藏手续费(Hide Fee)", "YCSXF");

        put("账单(Bill)", "ZD");
        put("查看账单(View Bill)", "CKZD");
        put("显示账单(Show Bill)", "XXZD");

        put("权限人(Authorized Person)", "QXR");
        put("管理员(Admin)", "GLY");

        put("设置下发汇率(Set the exchange rate)", "SZXFHL");
        put("设置下发费率(Set the delivery rate)", "SZXFFL");

        put("切换中文(Switch to Chinese)", "QHZW");
        put("切换英文(Switch to English)", "QHYW");

        put("设置下发地址(Set the delivery address)", "SZXFDZ");
        put("修改下发地址(Modify the delivery address)", "XGXFDZ");
        put("查看下发地址(View the sending address)", "CKXFDZ");

    }
    static {
        putEnglish("设置下发地址", "SZXFDZ");
        putEnglish("修改下发地址)", "XGXFDZ");
        putEnglish("查看下发地址", "CKXFDZ");
        putEnglish("切换中文", "QHZW");
        putEnglish("切换英文", "QHYW");
        // 初始化映射关系
        putEnglish("通知", "TZ");
        putEnglish("设置日切", "SZRQ");
        putEnglish("开启日切", "KQRQ");
        putEnglish("关闭日切", "GBRQ");
        putEnglish("设置费率", "set rate ");
        putEnglish("通知所有人", "TZSYR");
        putEnglish("波场下载地址", "BCXZDZ");
        putEnglish("设置汇率", "SZHL");
        putEnglish("设置入款单笔手续费", "SZRKDBSXF");
        putEnglish("取消", "QX");

        putEnglish("删除操作员", "SCCZY");
        putEnglish("删除操作人", "SCCZR");
        putEnglish("设置操作员", "SZCZY");
        putEnglish("设置操作人", "SZCZR");

        putEnglish("关闭回复人显示", "GBHFRXS");
        putEnglish("隐藏回复人显示", "YCHFRXS");
        putEnglish("隐藏回复人名称", "YCHFRMC");
        putEnglish("查询", "CX");

        putEnglish("设置入款手续费", "SZRKSXF");
        putEnglish("设置下发手续费", "SZXFSXF");
        putEnglish("设置下发单笔手续费", "SZXFDBSXF");
        putEnglish("设置单笔下发手续费", "SZDBXFSXF");
        putEnglish("设置单笔入款手续费", "SZDBRKSXF");

        // showArray 相关
        putEnglish("设置手续费", "SZSXF");
        putEnglish("下发", "XF");
        putEnglish("入款", "RK");
        putEnglish("显示明细", "XSMX");
        putEnglish("隐藏明细", "YCMX");
        putEnglish("将操作员显示", "JCZYXS");
        putEnglish("显示操作人名称", "XSCZRMC");
        putEnglish("显示操作人名字", "XSCZRMZ");
        putEnglish("隐藏操作人名称", "YCCZRMC");
        putEnglish("隐藏操作人名字", "YCCZRMZ");
        putEnglish("显示操作人", "XSCZR");
        putEnglish("显示操作员", "XSCZY");

        putEnglish("隐藏名字", "YCMZ");
        putEnglish("隐藏名称", "YCMC");
        putEnglish("关闭显示", "GBXS");
        putEnglish("将回复人显示", "JHFRXS");
        putEnglish("显示回复人名称", "XSHFRMC");
        putEnglish("显示余额", "XSYE");
        putEnglish("显示金额", "XSJE");
        putEnglish("显示USDT", "XSUSDT");
        putEnglish("显示usdt", "XSusdt");
        putEnglish("显示全部", "XSQB");

        putEnglish("显示1条", "XS1T");
        putEnglish("显示3条", "XS3T");
        putEnglish("显示5条", "XS5T");
        putEnglish("+0", "+0");
        putEnglish("-0", "-0");
        putEnglish("+0u", "+0U");
        putEnglish("-0u", "-0U");
        putEnglish("+0U", "+0U");
        putEnglish("-0U", "-0U");
        putEnglish("显示分类", "XSFL");
        putEnglish("隐藏分类", "YCFL");

        putEnglish("清理今天数据", "QLJTSJ");
        putEnglish("删除今天数据", "SCJTSJ");
        putEnglish("清理今天账单", "QLJTZD");
        putEnglish("清理今日账单", "QLJRZD");
        putEnglish("删除今日账单", "SCJRZD");
        putEnglish("清理今天帐单", "QLJTZD");
        putEnglish("删除今天账单", "SCJTZD");

        putEnglish("删除账单", "SCZD");
        putEnglish("删除今天帐单", "SCJTZD");
        putEnglish("删除帐单", "SCZD");
        putEnglish("清除账单", "QCZD");
        putEnglish("清除帐单", "QCZD");
        putEnglish("删除全部账单", "SCQBZD");
        putEnglish("删除全部帐单", "SCQBZD");
        putEnglish("清除全部账单", "QCQBZD");

        putEnglish("撤销下发", "CXXF");
        putEnglish("撤销入款", "CXRK");
        putEnglish("显示手续费", "XSSXF");
        putEnglish("隐藏手续费", "YCSXF");

        putEnglish("权限人", "QXR");
        putEnglish("管理员", "GLY");
        put("设置下发汇率", "SZXFHL");
        put("设置下发费率", "SZXFFL");
    }

    private static void put(String zh, String en) {
        COMMAND_MAP.put(zh, en);
    }
    private static void putEnglish(String zh, String en) {
        COMMAND_MAP_ENGLISH.put(zh, en);
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




}
