package com.example.ForgeX.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ForgeX.dto.ResetPasswordDTO;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.User;
import com.example.ForgeX.repository.UserRepository;
import com.example.ForgeX.service.ForgotService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class ForgotPassword {

    @Autowired
    private ForgotService forgotService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/forgot/password")
    public ResponseEntity<String> sendOtp(@RequestParam("email") @Email String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        String otp = forgotService.generateOtp(user.getEmail());
        return new ResponseEntity<>(otp, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam("email") @Email String email, String otp) {
        boolean flag = forgotService.verifyOtp(email, otp);
        if(flag){
            return  new ResponseEntity<>("OTP Validate",HttpStatus.OK);
        }
       else return  new ResponseEntity<>("Invalid OTP",HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("email")String email,@Valid @RequestBody ResetPasswordDTO request ){
        String ChangedPassword=forgotService.resetPassword(email, request);
        return  new ResponseEntity<>(ChangedPassword,HttpStatus.OK);
    }
}
