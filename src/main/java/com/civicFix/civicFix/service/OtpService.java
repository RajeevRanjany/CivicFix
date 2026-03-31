package com.civicFix.civicFix.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX = "OTP_";
    private static final int OTP_EXPIRY_MINUTES = 5;

    public String generateOtp(String phone) {

        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));

        redisTemplate.opsForValue().set(
                OTP_PREFIX + phone,
                otp,
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );

        log.info("OTP for {} is: {}", phone, otp);

        return otp;
    }

    public boolean verifyOtp(String phone, String otp) {

        String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + phone);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            return false;
        }

        redisTemplate.delete(OTP_PREFIX + phone);
        return true;
    }
}