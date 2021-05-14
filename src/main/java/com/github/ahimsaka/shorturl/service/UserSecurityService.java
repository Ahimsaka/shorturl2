package com.github.ahimsaka.shorturl.service;

public interface SecurityUserService {
    String validatePasswordResetToken(String token);
}
