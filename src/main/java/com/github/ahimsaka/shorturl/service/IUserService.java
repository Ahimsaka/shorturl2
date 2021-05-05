package com.github.ahimsaka.shorturl.service;

import com.github.ahimsaka.shorturl.dto.UserDto;
import com.github.ahimsaka.shorturl.entity.UrlRecord;
import com.github.ahimsaka.shorturl.entity.User;

import java.util.Collection;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto);
    User saveUser(User user);
}
