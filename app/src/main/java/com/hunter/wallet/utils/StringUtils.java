package com.hunter.wallet.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static boolean hasText(String s) {
        if (s != null && s.trim().length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean matchExp(String s, String exp) {
        if (hasText(s) && hasText(exp) && Pattern.compile(exp).matcher(s).find()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean equal(String s1, String s2) {
        if (hasText(s1) && hasText(s2) && s1.equals(s2)) {
            return true;
        } else {
            return false;
        }
    }

    public static byte[] toUTF8(String s) {
        if (s != null) {
            return s.getBytes(UTF8);
        } else {
            return new byte[0];
        }
    }
}
