package com.example.ForgeX.service;

import com.example.ForgeX.dto.ResetPasswordDTO;

public interface  ForgotService {
    String generateOtp(String email);
    boolean verifyOtp(String email,String otp);

    String resetPassword(String email,ResetPasswordDTO request);

}
