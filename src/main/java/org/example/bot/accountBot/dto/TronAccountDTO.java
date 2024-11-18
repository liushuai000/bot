package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TronAccountDTO {

    private int totalFrozenV2;
    private int transactions_out;
    private int frozenForEnergyV2;
    private int rewardNum;
    private int delegatedFrozenV2BalanceForBandwidth;
    private OwnerPermission ownerPermission;
    private String redTag;
    private int delegateFrozenForEnergy;
    private long balance;
    private int frozenForBandWidthV2;
    private int canWithdrawAmountV2;
    private Map<String, Object> delegated;
    private int transactions_in;
    private int totalTransactionCount;
    private Representative representative;
    private String announcement;
    private List<String> allowExchange;
    private int accountType;
    private List<String> exchanges;
    private Frozen frozen;
    private int transactions;
//    private int delegatedFrozenV2BalanceForEnergy;
    private String name;
//    private int frozenForEnergy;
    private double energyCost;
    private List<ActivePermission> activePermissions;
//    private int acquiredDelegatedFrozenV2BalanceForBandwidth;
    private double netCost;
//    private int acquiredDelegateFrozenForBandWidth;
    private String greyTag;
    private String publicTag;
    private List<WithPriceToken> withPriceTokens;
//    private int unfreezeV2;
    private boolean feedbackRisk;
    private BigDecimal voteTotal;
    private BigDecimal totalFrozen;
    private long latest_operation_time;
//    private int frozenForBandWidth;
//    private int reward;
    private String addressTagLogo;
    private String address;
    private List<Object> frozen_supply;
    private Bandwidth bandwidth;
    private long date_created;
    private int acquiredDelegatedFrozenV2BalanceForEnergy;
    private AccountResource accountResource;
    private String blueTag;
    private int witness;
    private int freezing;
    private int delegateFrozenForBandWidth;
    private boolean activated;
    private int acquiredDelegateFrozenForEnergy;

    @Data
    public static class OwnerPermission {
        private List<Key> keys;
        private int threshold;
        private String permission_name;
    }

    @Data
    public static class Key {
        private String address;
        private int weight;
    }

    @Data
    public static class Representative {
        private long lastWithDrawTime;
        private int allowance;
        private boolean enabled;
        private String url;
    }

    @Data
    public static class Frozen {
        private int total;
        private List<Object> balances;
    }

    @Data
    public static class ActivePermission {
        private String operations;
        private List<Key> keys;
        private int threshold;
        private int id;
        private String type;
        private String permission_name;
    }

    @Data
    public static class WithPriceToken {
        private String amount;
        private BigDecimal tokenPriceInTrx;
        private String tokenId;
        private String balance;
        private String tokenName;
        private int tokenDecimal;
        private String tokenAbbr;
        private BigDecimal tokenCanShow;
        private String tokenType;
        private boolean vip;
        private String tokenLogo;
    }

    @Data
    public static class Bandwidth {
        private int energyRemaining;
        private long totalEnergyLimit;
        private long totalEnergyWeight;
        private int netUsed;
        private int storageLimit;
        private double storagePercentage;
        private Map<String, Object> assets;
        private double netPercentage;
        private int storageUsed;
        private int storageRemaining;
        private int freeNetLimit;
        private int energyUsed;
        private int freeNetRemaining;
        private int netLimit;
        private int netRemaining;
        private int energyLimit;
        private int freeNetUsed;
        private long totalNetWeight;
        private double freeNetPercentage;
        private double energyPercentage;
        private long totalNetLimit;
    }

    @Data
    public static class AccountResource {
        private Map<String, Object> frozen_balance_for_energy;
    }
}
