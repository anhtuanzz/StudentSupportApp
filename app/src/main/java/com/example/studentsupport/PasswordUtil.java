package com.example.studentsupport;

import android.util.Base64;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Demo: PBKDF2 mật khẩu, không lưu mật khẩu gốc vào SQLite. */
final class PasswordUtil {
    private static final int ITERATIONS = 120_000;
    static String newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }
    static String hash(String password, String saltBase64) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.decode(saltBase64, Base64.DEFAULT), ITERATIONS, 256);
        try {
            byte[] bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể băm mật khẩu", e);
        } finally {
            spec.clearPassword();
        }
    }
}
