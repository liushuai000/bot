package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


/**
 * 交易实体类，用于表示区块链交易的详细信息。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TronHistoryDTO {
    /**
     * 交易 ID。
     */
    private String transaction_id;

    /**
     * 交易状态。
     */
    private int status;

    /**
     * 区块时间戳。
     */
    private long block_ts;

    /**
     * 发送方地址。
     */
    private String from_address;

    /**
     * 发送方标签信息。
     */
    private FromAddressTag from_address_tag;

    /**
     * 接收方地址。
     */
    private String to_address;

    /**
     * 接收方标签信息。
     */
    private ToAddressTag to_address_tag;

    /**
     * 区块高度。
     */
    private long block;

    /**
     * 合约地址。
     */
    private String contract_address;

    /**
     * 触发信息。
     */
    private TriggerInfo trigger_info;

    /**
     * 交易数量。
     */
    private String quant;

    /**
     * 批准金额。
     */
    private String approval_amount;

    /**
     * 事件类型。
     */
    private String event_type;

    /**
     * 交易是否已确认。
     */
    private boolean confirmed;

    /**
     * 合约返回值。
     */
    private String contractRet;

    /**
     * 最终结果。
     */
    private String finalResult;

    /**
     * 代币信息。
     */
    private TokenInfo tokenInfo;

    /**
     * 交易是否回滚。
     */
    private boolean revert;

    /**
     * 合约类型。
     */
    private String contract_type;

    /**
     * 发送方地址是否为合约地址。
     */
    private boolean fromAddressIsContract;

    /**
     * 接收方地址是否为合约地址。
     */
    private boolean toAddressIsContract;

    /**
     * 交易是否有风险。
     */
    private boolean riskTransaction;

    /**
     * 表示接收方标签信息的类。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ToAddressTag {
        /**
         * 接收方标签。
         */
        private String to_address_tag;

        /**
         * 接收方标签 logo。
         */
        private String to_address_tag_logo;
    }
    /**
     * 表示触发信息的类。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class TriggerInfo {
        /**
         * 方法名称。
         */
        private String method;

        /**
         * 数据。
         */
        private String data;

        /**
         * 参数信息。
         */
        private Parameter parameter;

        /**
         * 方法名称。
         */
        private String methodName;

        /**
         * 合约地址。
         */
        private String contract_address;

        /**
         * 调用值。
         */
        private long call_value;
    }

    /**
     * 表示代币信息的类。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class TokenInfo {
        /**
         * 代币 ID。
         */
        private String tokenId;

        /**
         * 代币简称。
         */
        private String tokenAbbr;

        /**
         * 代币名称。
         */
        private String tokenName;

        /**
         * 代币小数位数。
         */
        private int tokenDecimal;

        /**
         * 代币是否显示。
         */
        private int tokenCanShow;

        /**
         * 代币类型。
         */
        private String tokenType;

        /**
         * 代币 logo。
         */
        private String tokenLogo;

        /**
         * 代币级别。
         */
        private String tokenLevel;

        /**
         * 发行者地址。
         */
        private String issuerAddr;

        /**
         * 是否为 VIP 代币。
         */
        private boolean vip;
    }

    /**
     * 表示参数信息的类。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Parameter {
        /**
         * 交易数量。
         */
        private String _value;

        /**
         * 接收方地址。
         */
        private String _to;
    }
    /**
     * 表示发送方标签信息的类。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class FromAddressTag {
        /**
         * 发送方标签。
         */
        private String from_address_tag;

        /**
         * 发送方标签 logo。
         */
        private String from_address_tag_logo;
    }



}




