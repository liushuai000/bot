package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TronAccountDTO1 {
    // TRC20 代币余额
    private List<Map<String, Object>> trc20token_balances;

    // 发出的交易数量
    private int transactions_out;

    // 用于带宽的委托冻结金额
    private int acquiredDelegateFrozenForBandWidth;

    // 奖励数量
    private int rewardNum;

    // 账户权限信息
    private OwnerPermission ownerPermission;

    // 代币余额
    private List<TokenBalance> tokenBalances;

    // 用于能量的委托冻结金额
    private int delegateFrozenForEnergy;

    // 余额信息
    private List<Balance> balances;

    // TRC721 代币余额
    private List<Map<String, Object>> trc721token_balances;

    // 总余额
    private long balance;

    // 投票总数
    private int voteTotal;

    // 总冻结金额
    private int totalFrozen;

    // 代币信息
    private List<TokenBalance> tokens;

    // 委托信息
    private Map<String, Object> delegated;

    // 收到的交易数量
    private int transactions_in;

    // 总交易数量
    private int totalTransactionCount;

    // 代表信息
    private Representative representative;

    // 用于带宽的冻结金额
    private int frozenForBandWidth;

    // 奖励
    private int reward;

    // 地址标签logo
    private String addressTagLogo;

    // 允许交换的列表
    private List<String> allowExchange;

    // 地址
    private String address;

    // 冻结供应
    private List<Map<String, Object>> frozen_supply;

    // 创建日期
    private long date_created;

    // 账户类型
    private int accountType;

    // 交易所信息
    private List<Map<String, Object>> exchanges;

    // 冻结信息
    private Frozen frozen;

    // 账户资源信息
    private AccountResource accountResource;

    // 交易数量
    private int transactions;

    // 见证人
    private int witness;

    // 用于带宽的委托冻结金额
    private int delegateFrozenForBandWidth;

    // 名称
    private String name;

    // 用于能量的冻结金额
    private int frozenForEnergy;

    // 用于能量的委托冻结金额
    private int acquiredDelegateFrozenForEnergy;

    // 活动权限信息
    private List<ActivePermission> activePermissions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OwnerPermission {
        // 密钥列表
        private List<Key> keys;

        // 阈值
        private int threshold;

        // 权限名称
        private String permission_name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {
        // 地址
        private String address;

        // 权重
        private int weight;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class TokenBalance {
        // 金额
        private String amount;

        // 代币价格（以TRX计）
        private int tokenPriceInTrx;

        // 代币ID
        private String tokenId;

        // 余额
        private String balance;

        // 代币名称
        private String tokenName;

        // 代币小数位
        private int tokenDecimal;

        // 代币简称
        private String tokenAbbr;

        // 是否显示
        private int tokenCanShow;

        // 代币类型
        private String tokenType;

        // 是否VIP
        private boolean vip;

        // 代币Logo
        private String tokenLogo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Balance {
        // 金额
        private String amount;

        // 代币价格（以TRX计）
        private int tokenPriceInTrx;

        // 代币ID
        private String tokenId;

        // 余额
        private String balance;

        // 代币名称
        private String tokenName;

        // 代币小数位
        private int tokenDecimal;

        // 代币简称
        private String tokenAbbr;

        // 是否显示
        private int tokenCanShow;

        // 代币类型
        private String tokenType;

        // 是否VIP
        private boolean vip;

        // 代币Logo
        private String tokenLogo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Representative {
        // 上次提取时间
        private long lastWithDrawTime;

        // 允许额度
        private int allowance;

        // 是否启用
        private boolean enabled;

        // URL
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Frozen {
        // 总冻结金额
        private int total;

        // 冻结余额列表
        private List<Balance> balances;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountResource {
        // 用于能量的冻结余额
        private FrozenBalanceForEnergy frozen_balance_for_energy;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FrozenBalanceForEnergy {
        // 用于能量的冻结余额
        private Map<String, Object> frozen_balance_for_energy;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivePermission {
        // 操作
        private String operations;

        // 密钥列表
        private List<Key> keys;

        // 阈值
        private int threshold;

        // ID
        private int id;

        // 类型
        private String type;

        // 权限名称
        private String permission_name;
    }
}
