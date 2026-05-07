package com.tuixach.lvt.service;

import com.tuixach.lvt.config.JwtService;
import com.tuixach.lvt.dto.*;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.BadRequestException;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;

    /**
     * Đăng ký tài khoản - tạo user (chưa verify) + gửi OTP trực tiếp qua email
     */
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            // Nếu user đã tồn tại nhưng chưa verify, cho phép gửi lại OTP
            User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (existingUser != null && !existingUser.isEmailVerified()) {
                // Cập nhật thông tin và gửi lại OTP
                existingUser.setFullName(request.getFullName());
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                existingUser.setPhone(request.getPhone());
                existingUser.setAddress(request.getAddress());
                userRepository.save(existingUser);

                sendOtp(request.getEmail(), request.getFullName());
                return;
            }
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Tạo user mới (chưa verify email)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(User.Role.USER)
                .active(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Gửi OTP qua RabbitMQ → Consumer gửi email
        sendOtp(request.getEmail(), request.getFullName());

        log.info("User registered (pending verification): {}", request.getEmail());
    }

    /**
     * Xác nhận OTP → kích hoạt tài khoản → trả JWT token
     */
    public AuthResponse verifyRegistration(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Tài khoản đã được xác nhận trước đó");
        }

        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (!isValid) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Kích hoạt tài khoản
        user.setEmailVerified(true);
        userRepository.save(user);

        // Tạo JWT token
        String token = jwtService.generateToken(user);

        log.info("Email verified successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    /**
     * Gửi lại OTP
     */
    public void resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Tài khoản đã được xác nhận");
        }

        if (otpService.hasActiveOtp(request.getEmail())) {
            throw new BadRequestException(
                    "Mã OTP trước đó vẫn còn hiệu lực. Vui lòng kiểm tra email hoặc đợi hết hạn.");
        }

        sendOtp(request.getEmail(), user.getFullName());
    }

    /**
     * Đăng nhập
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException(
                    "Tài khoản chưa được xác nhận email. Vui lòng kiểm tra email hoặc yêu cầu gửi lại mã OTP.");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    /**
     * Quên mật khẩu
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại trong hệ thống"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Helper: tạo OTP + gửi message qua RabbitMQ
     */
    private void sendOtp(String email, String fullName) {
        String otpCode = otpService.generateAndSaveOtp(email);
        emailService.sendRegistrationOtpEmail(email, fullName, otpCode);
        log.info("OTP sent successfully to: {}", email);
    }
}
