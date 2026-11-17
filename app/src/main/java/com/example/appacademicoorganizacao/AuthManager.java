package com.example.appacademicoorganizacao;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class AuthManager {

    public enum Result { SUCCESS, INVALID_CREDENTIALS, LOCKED }
    public static final int MAX_EMAIL_LENGTH = 254;
    public static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 30_000L;
    private static final String PREFS = "auth_guard";
    private static final String KEY_FAILS = "fails";
    private static final String KEY_LOCK_UNTIL = "lock_until";
    private static final String SALT = "continuum-demo-salt";
    // sha256("continuum-demo-salt:usuario@gmail.com")
    private static final String EMAIL_HASH =
            "a6971d661b00692180fa81e20168fe36ea064dda4c8abeb68768ecb488f05510";
    // sha256("continuum-demo-salt:Usuario123")
    private static final String PASSWORD_HASH =
            "07a182607007770cc679b9ccbda61d53e2d5f068d938bf88b9d5a397fffbc037";

    private AuthManager() { }

    public static String normalizeEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        return email.length() <= MAX_EMAIL_LENGTH
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int lockRemainingSeconds(Context ctx) {
        long until = prefs(ctx).getLong(KEY_LOCK_UNTIL, 0L);
        long left = until - System.currentTimeMillis();
        return left > 0 ? (int) Math.ceil(left / 1000.0) : 0;
    }

    public static Result authenticate(Context ctx, String rawEmail, String password) {
        if (lockRemainingSeconds(ctx) > 0) {
            return Result.LOCKED;
        }

        String email = normalizeEmail(rawEmail);

        boolean emailOk = constantTimeEquals(hash(email), EMAIL_HASH);
        boolean passOk = constantTimeEquals(hash(password), PASSWORD_HASH);

        if (emailOk && passOk) {
            prefs(ctx).edit().putInt(KEY_FAILS, 0).putLong(KEY_LOCK_UNTIL, 0L).apply();
            return Result.SUCCESS;
        }

        int fails = prefs(ctx).getInt(KEY_FAILS, 0) + 1;
        SharedPreferences.Editor ed = prefs(ctx).edit();
        if (fails >= MAX_FAILED_ATTEMPTS) {
            ed.putInt(KEY_FAILS, 0).putLong(KEY_LOCK_UNTIL, System.currentTimeMillis() + LOCK_DURATION_MS);
            ed.apply();
            return Result.LOCKED;
        }
        ed.putInt(KEY_FAILS, fails).apply();
        return Result.INVALID_CREDENTIALS;
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest((SALT + ":" + value).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
