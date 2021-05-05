package com.github.ahimsaka.shorturl.dto;

import com.github.ahimsaka.shorturl.validation.PasswordMatches;
import com.github.ahimsaka.shorturl.validation.ValidEmail;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@PasswordMatches
public class UserDto {
    @ValidEmail
    @NotNull
    @NotEmpty
    private String username;

    @NotNull
    @NotEmpty
    private String password;
    private String matchingPassword;

}
