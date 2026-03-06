package com.civicFix.civicFix.controller;

import com.civicFix.civicFix.entity.Role;
import com.civicFix.civicFix.entity.User;
import com.civicFix.civicFix.repository.UserRepository;
import com.civicFix.civicFix.security.JwtUtils;
import com.civicFix.civicFix.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @PostMapping("/send-otp")
    public String sendOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        otpService.generateOtp(phone);
        return "OTP sent (check console for now)";
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");
        String otp = request.get("otp");

        boolean valid = otpService.verifyOtp(phone, otp);

        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByPhoneNumber(phone)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .phoneNumber(phone)
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtUtils.generateToken(user.getId(), user.getRole().name());

        return Map.of("token", token);
    }
}