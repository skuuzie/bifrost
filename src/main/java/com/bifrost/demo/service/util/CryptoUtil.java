package com.bifrost.demo.service.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public final class CryptoUtil {
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    public static byte[] getFinalKey(String commonKey, String token) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey secretKeySpec = new SecretKeySpec(EncodingUtil.b64DecodeUrlSafe(commonKey), "HmacSHA512");
        SecretKey secretKeySpec2 = new SecretKeySpec(EncodingUtil.b64DecodeUrlSafe("AA=="), "HmacSHA256");

        Mac mac = Mac.getInstance("HmacSHA512");
        Mac mac2 = Mac.getInstance("HmacSHA256");

        mac.init(secretKeySpec);
        mac2.init(secretKeySpec2);

        if (token != null) {
            mac.update(token.getBytes());
        }

        return mac2.doFinal(mac.doFinal());
    }

    public static byte[] AESGCMEncrypt(String commonKey, String token, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKey key = new SecretKeySpec(getFinalKey(commonKey, token), "AES");

        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

        byte[] ciphertext = cipher.doFinal(data);
        byte[] encryptedWithIv = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedWithIv, iv.length, ciphertext.length);

        return encryptedWithIv;
    }

    public static byte[] AESGCMDecrypt(String commonKey, String token, byte[] data) throws Exception {
        SecretKey key = new SecretKeySpec(getFinalKey(commonKey, token), "AES");

        byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_LENGTH_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(data, GCM_IV_LENGTH_BYTES, data.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        return cipher.doFinal(ciphertext);
    }

    public static int getGCMActualSize(byte[] data) {
        return data.length - (GCM_IV_LENGTH_BYTES + (GCM_TAG_LENGTH_BITS / 8));
    }

    public static String getRandomNumber(int n) {
        SecureRandom secureRandom = new SecureRandom();
        int max = (int) Math.pow(10, n);
        int otpInt = secureRandom.nextInt(max);
        return String.format("%0" + n + "d", otpInt);
    }
}
