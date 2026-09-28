package com.example.ForgeX.service;

public interface EmailService {

    boolean sendOtp(String toEmail, String subject, String body);
}
