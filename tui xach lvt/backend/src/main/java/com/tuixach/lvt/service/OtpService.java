package com.tuixach.lvt.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    public String generateAndSaveOtp(String email) {
        String otpCode = String.format("%06d", random.nextInt(1000000));
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(otpExpirationMinutes);
        
        otpStorage.put(email, new OtpData(otpCode, expiryTime));
        log.info("OTP generated for email {}: {} (TTL: {} minutes)", email, otpCode, otpExpirationMinutes);

        return otpCode;
    }

    public boolean verifyOtp(String email, String otpCode) {
        OtpData data = otpStorage.get(email);

        if (data == null || data.isExpired()) {
            log.warn("OTP expired or not found for email: {}", email);
            if (data != null) otpStorage.remove(email);
            return false;
        }

        if (data.getCode().equals(otpCode)) {
            otpStorage.remove(email);
            log.info("OTP verified successfully for email: {}", email);
            return true;
        }
        return false;
    }

    public boolean hasActiveOtp(String email) {
        OtpData data = otpStorage.get(email);
        return data != null && !data.isExpired();
    }

    @AllArgsConstructor
    @Getter
    private static class OtpData {
        private String code;
        private long expiryTime;

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
