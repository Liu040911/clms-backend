package com.clms.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 微信消息推送安全模式签名与解密工具。
 */
public final class WeChatMessageCryptoUtil {

    private static final String SHA1 = "SHA-1";
    private static final String AES_TRANSFORMATION = "AES/CBC/NoPadding";
    private static final int BLOCK_SIZE = 32;

    private WeChatMessageCryptoUtil() {
    }

    public static String sign(String... values) {
        try {
            List<String> sorted = new ArrayList<>();
            for (String value : values) {
                sorted.add(value == null ? "" : value);
            }
            Collections.sort(sorted);
            StringBuilder raw = new StringBuilder();
            for (String item : sorted) {
                raw.append(item);
            }

            MessageDigest digest = MessageDigest.getInstance(SHA1);
            byte[] hash = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String h = Integer.toHexString(b & 0xFF);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成微信签名失败", e);
        }
    }

    public static boolean verifyUrlSignature(String signature, String token, String timestamp, String nonce) {
        String expected = sign(token, timestamp, nonce);
        return expected.equals(signature);
    }

    public static boolean verifyMsgSignature(String msgSignature, String token, String timestamp, String nonce, String encrypt) {
        String expected = sign(token, timestamp, nonce, encrypt);
        return expected.equals(msgSignature);
    }

    public static String decrypt(String encodingAesKey, String encrypted, String expectedAppId) {
        try {
            byte[] aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
            byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            byte[] original = cipher.doFinal(encryptedBytes);
            byte[] unpadded = pkcs7Unpad(original);

            if (unpadded.length < 20) {
                throw new IllegalArgumentException("微信消息解密后长度不合法");
            }

            int xmlLength = ByteBuffer.wrap(unpadded, 16, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            if (xmlLength < 0 || 20 + xmlLength > unpadded.length) {
                throw new IllegalArgumentException("微信消息长度字段不合法");
            }

            String content = new String(unpadded, 20, xmlLength, StandardCharsets.UTF_8);
            String appId = new String(unpadded, 20 + xmlLength, unpadded.length - 20 - xmlLength, StandardCharsets.UTF_8);

            if (expectedAppId != null && !expectedAppId.isBlank() && !expectedAppId.equals(appId)) {
                throw new IllegalArgumentException("微信消息AppId校验失败");
            }

            return content;
        } catch (Exception e) {
            throw new IllegalStateException("微信消息解密失败", e);
        }
    }

    private static byte[] pkcs7Unpad(byte[] decrypted) {
        int pad = decrypted[decrypted.length - 1] & 0xFF;
        if (pad < 1 || pad > BLOCK_SIZE) {
            pad = 0;
        }
        int len = decrypted.length - pad;
        byte[] result = new byte[len];
        System.arraycopy(decrypted, 0, result, 0, len);
        return result;
    }
}
