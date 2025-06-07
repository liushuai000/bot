package org.example.bot.accountBot.utils;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class TronKeyUtil {

    public static void main(String[] args) {
        System.out.println(generateTronKey());
    }

    public static Map<String, String> generateTronKey() {
        ECKey ecKey = new ECKey(); // 自动生成私钥和公钥

        byte[] privateKey = ecKey.getPrivKeyBytes();
        byte[] publicKey = ecKey.getPubKey();

        // 1. Keccak256(公钥)
        byte[] hash = new Keccak.Digest256().digest(publicKey);

        // 2. 取后 20 字节 + TRON 地址前缀 0x41
        byte[] addressBytes = new byte[21];
        addressBytes[0] = (byte) 0x41; // TRON 前缀
        System.arraycopy(hash, 12, addressBytes, 1, 20);

        // 3. Base58Check 编码（添加双 SHA256 校验）
        String base58Address = encodeToBase58Check(addressBytes);

        Map<String, String> result = new HashMap<>();
        result.put("privateKey", Hex.toHexString(privateKey));
        result.put("publicKey", Hex.toHexString(publicKey));
        result.put("address", base58Address);

        return result;
    }

    // 实现 Base58Check 编码
    private static String encodeToBase58Check(byte[] payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] checksum = digest.digest(digest.digest(payload));
            ByteBuffer buffer = ByteBuffer.allocate(payload.length + 4);
            buffer.put(payload);
            buffer.put(checksum, 0, 4);
            return Base58.encode(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
