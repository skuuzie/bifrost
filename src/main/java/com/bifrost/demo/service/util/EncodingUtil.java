package com.bifrost.demo.service.util;

import java.util.Base64;

public final class EncodingUtil {
    public static String b64EncodeUrlSafe(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static byte[] b64DecodeUrlSafe(String encodedString) {
        return Base64.getUrlDecoder().decode(encodedString);
    }
}
