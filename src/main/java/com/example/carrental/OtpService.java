package com.example.carrental;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public final class OtpService {

    public static final long OTP_TTL_MILLIS = 10 * 60 * 1000;
    public static final long RESEND_COOLDOWN_MILLIS = 60 * 1000;
    public static final int MAX_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<String, OtpRecord> STORE = new ConcurrentHashMap<>();

    private OtpService() {
    }

    public static String issueOtp(String username) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        STORE.put(username, new OtpRecord(hash(code), System.currentTimeMillis(), 0));
        return code;
    }

    public static long secondsBeforeResendAllowed(String username) {
        OtpRecord record = STORE.get(username);
        if (record == null) {
            return 0;
        }
        long wait = RESEND_COOLDOWN_MILLIS - (System.currentTimeMillis() - record.createdAt);
        return Math.max(0, wait / 1000);
    }

    public static OtpVerifyResult verifyOtp(String username, String code) {
        OtpRecord record = STORE.get(username);
        if (record == null) {
            return OtpVerifyResult.NOT_ISSUED;
        }
        if (expired(record)) {
            STORE.remove(username);
            return OtpVerifyResult.EXPIRED;
        }
        if (record.attempts >= MAX_ATTEMPTS) {
            STORE.remove(username);
            return OtpVerifyResult.TOO_MANY_ATTEMPTS;
        }
        if (!MessageDigest.isEqual(
                hash(code).getBytes(StandardCharsets.UTF_8),
                record.hashedCode.getBytes(StandardCharsets.UTF_8))) {
            record.attempts++;
            return OtpVerifyResult.INVALID;
        }
        STORE.remove(username);
        return OtpVerifyResult.OK;
    }

    public static void invalidate(String username) {
        STORE.remove(username);
    }

    private static boolean expired(OtpRecord record) {
        return System.currentTimeMillis() - record.createdAt > OTP_TTL_MILLIS;
    }

    private static String hash(String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }

    public enum OtpVerifyResult {
        OK, INVALID, EXPIRED, TOO_MANY_ATTEMPTS, NOT_ISSUED
    }

    private static final class OtpRecord {
        final String hashedCode;
        final long createdAt;
        volatile int attempts;

        OtpRecord(String hashedCode, long createdAt, int attempts) {
            this.hashedCode = hashedCode;
            this.createdAt = createdAt;
            this.attempts = attempts;
        }
    }
}
