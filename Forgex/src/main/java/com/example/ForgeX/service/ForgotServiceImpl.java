package com.example.ForgeX.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ForgeX.dto.ResetPasswordDTO;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.User;
import com.example.ForgeX.repository.UserRepository;

@Service
public class ForgotServiceImpl implements ForgotService {

    @Autowired
    private EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    Map<String, String> map = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public String generateOtp(String email) {

        int otp = 100000 + random.nextInt(900000);
        String otpString = String.valueOf(otp);
        map.put(email, otpString);

        String subject = "AgriPro - Password Reset OTP";

        String message = "Hello,\n\n"
                + "We received a request to reset your ForgeX account password.\n\n"
                + "Your One-Time Password (OTP) is: " + otpString + "\n\n"
                + "This OTP is valid for a limited time. Please do not share this OTP with anyone.\n\n"
                + "If you did not request a password reset, please ignore this email.\n\n"
                + "Regards,\n"
                + "ForgeX Team";
        boolean flag = emailService.sendOtp(email, subject, message);
        if (flag) {
            return "OTP sent Successfully to email:" + email;
        } else
            return "invalid email";
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null) {
            return false;
        }
        String savedOtp = map.get(email);
        if (otp.equals(savedOtp)) {
            map.remove(email);
            return true;
        }
        return false;
    }

    @Override
    public String resetPassword(String email, ResetPasswordDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        if (request.getConfirmPassword().equals(request.getPassword())) {
            user.setPassword(passwordEncoder().encode(request.getPassword()));
            userRepository.save(user);
            return "Password Changed Successfully!";
        }
        return "New password and confirm password do not match";
    }

}
