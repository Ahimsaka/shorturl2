package com.github.ahimsaka.shorturl.service;

import com.github.ahimsaka.shorturl.dto.UserDto;
import com.github.ahimsaka.shorturl.entity.UrlRecord;
import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.entity.VerificationToken;
import com.github.ahimsaka.shorturl.exception.UserAlreadyExistException;

import java.util.Collection;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException;

    User getUser(String verificationToken);

    User saveRegisteredUser(User user);

    void createVerificationToken(User user, String token);

    VerificationToken getVerificationToken(String verificationToken);

    VerificationToken generateNewVerificationToken(String token);
}
