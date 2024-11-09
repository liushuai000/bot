package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 表示账户信息的实体类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TronAccountDTO {

    /**
     * 账户的出账交易数量
     */
    private int transactions_out;

    /**
     * 委托冻结的带宽
     */
    private int acquiredDelegateFrozenForBandWidth;

    /**
     * 奖励数量
     */
    private int rewardNum;

    /**
     * 灰色标签
     */
    private String greyTag;

    /**
     * 账户所有者权限
     */
    private OwnerPermission ownerPermission;

    /**
     * 红色标签
     */
    private String redTag;

    /**
     * 公共标签
     */
    private String publicTag;

    /**
     * 带价格的代币列表
     */
    private List<WithPriceToken> withPriceTokens;

    /**
     * 委托冻结的能量
     */
    private int delegateFrozenForEnergy;

    /**
     * 账户余额
     */
    private long balance;

    /**
     * 反馈风险标志
     */
    private boolean feedbackRisk;

    /**
     * 投票总数
     */
    private int voteTotal;

    /**
     * 总冻结量
     */
    private int totalFrozen;
    private int totalFrozenV2;
    private int frozenForEnergyV2;

    /**
     * 委托信息
     */
    private Delegated delegated;

    /**
     * 账户的入账交易数量
     */
    private int transactions_in;

    /**
     * 最新操作时间
     */
    private long latest_operation_time;

    /**
     * 总交易数量
     */
    private int totalTransactionCount;

    /**
     * 代表信息
     */
    private Representative representative;

    /**
     * 冻结的带宽
     */
    private int frozenForBandWidth;

    /**
     * 公告
     */
    private String announcement;

    /**
     * 奖励状态
     */
    private int reward;

    /**
     * 地址标签Logo
     */
    private String addressTagLogo;

    /**
     * 允许交换的列表
     */
    private List<String> allowExchange;

    /**
     * 账户地址
     */
    private String address;

//    /**
//     * 冻结供应列表
//     */
//    private List<FrozenSupply> frozen_supply;

    /**
     * 创建日期
     */
    private long date_created;

    /**
     * 账户类型
     */
    private int accountType;

    /**
     * 交易所列表
     */
    private List<Exchange> exchanges;

    /**
     * 冻结信息
     */
    private Frozen frozen;

    /**
     * 账户资源信息
     */
    private AccountResource accountResource;

    /**
     * 账户的总交易数量
     */
    private int transactions;

    /**
     * 蓝色标签
     */
    private String blueTag;

    /**
     * 见证人数量
     */
    private int witness;

    /**
     * 委托冻结的带宽（重复字段）
     */
    private int delegateFrozenForBandWidth2;

    /**
     * 账户名称
     */
    private String name;

    /**
     * 冻结的能量
     */
    private int frozenForEnergy;

    /**
     * 是否激活
     */
    private boolean activated;

    /**
     * 委托冻结的能量（重复字段）
     */
    private int acquiredDelegateFrozenForEnergy;

    /**
     * 活动权限列表
     */
    private List<ActivePermission> activePermissions;
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivePermission {
        /**
         * 权限键列表
         */
        private List<Key> keys;

        /**
         * 权限阈值
         */
        private int threshold;

        /**
         * 权限名称
         */
        private String permission_name;
    }

    /**
     * 所有者权限类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OwnerPermission {
        /**
         * 权限键列表
         */
        private List<Key> keys;

        /**
         * 权限阈值
         */
        private int threshold;

        /**
         * 权限名称
         */
        private String permission_name;

        // Getters and Setters
    }

    /**
     * 权限键类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {
        /**
         * 地址
         */
        private String address;

        /**
         * 权重
         */
        private int weight;

        // Getters and Setters
    }

    /**
     * 带价格的代币类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WithPriceToken {
        /**
         * 金额
         */
        private String amount;

        /**
         * 代币在TRX中的价格
         */
        private int tokenPriceInTrx;

        /**
         * 代币ID
         */
        private String tokenId;

        /**
         * 余额
         */
        private String balance;

        /**
         * 代币名称
         */
        private String tokenName;

        /**
         * 代币小数位
         */
        private int tokenDecimal;

        /**
         * 代币简称
         */
        private String tokenAbbr;

        /**
         * 代币是否可显示
         */
        private int tokenCanShow;

        /**
         * 代币类型
         */
        private String tokenType;

        /**
         * VIP标志
         */
        private boolean vip;

        /**
         * 代币Logo
         */
        private String tokenLogo;

        // Getters and Setters
    }

    /**
     * 委托类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delegated {
        // Add fields if needed
    }

    /**
     * 代表类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Representative {
        /**
         * 上次提现时间
         */
        private long lastWithDrawTime;

        /**
         * 允许额度
         */
        private int allowance;

        /**
         * 是否启用
         */
        private boolean enabled;

        /**
         * URL
         */
        private String url;

        // Getters and Setters
    }

    /**
     * 交易所类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Exchange {
        // Add fields if needed
    }

    /**
     * 冻结类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Frozen {
        /**
         * 总冻结量
         */
        private int total;

        /**
         * 冻结余额列表
         */
        private List<Balance> balances;

        // Getters and Setters
    }

    /**
     * 冻结余额类
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Balance {
        // Add fields if needed
    }

    /**
     * 账户资源类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountResource {
        /**
         * 冻结的能量余额
         */
        private FrozenBalanceForEnergy frozen_balance_for_energy;

        // Getters and Setters
    }

    /**
     * 冻结的能量余额类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FrozenBalanceForEnergy {
        // Add fields if needed
    }



}