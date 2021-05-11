package com.github.ahimsaka.shorturl.service.impl;

import com.github.ahimsaka.shorturl.dto.UserDto;
import com.github.ahimsaka.shorturl.entity.Role;
import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.entity.VerificationToken;
import com.github.ahimsaka.shorturl.exception.UserAlreadyExistException;
import com.github.ahimsaka.shorturl.repository.RoleRepository;
import com.github.ahimsaka.shorturl.repository.UserRepository;
import com.github.ahimsaka.shorturl.repository.VerificationTokenRepository;
import com.github.ahimsaka.shorturl.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService implements IUserService {
    final private Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException {
        if (usernameExist(userDto.getUsername())) {
            throw new UserAlreadyExistException("There is an account with that user name: " + userDto.getUsername());
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEnabled(false);
        user.setRoles(Set.of(roleRepository.findByName("USER")));

        return userRepository.save(user);
    }

    private boolean usernameExist(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Override
    public User getUser(String verificationToken) {
        User user = verificationTokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

    @Override
    public VerificationToken getVerificationToken(String verificationToken) {
        return verificationTokenRepository.findByToken(verificationToken);
    }

    @Override
    public User saveRegisteredUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepository.save(myToken);
    }

    @Override
    public VerificationToken generateNewVerificationToken(final String existingVerificationToken) {
        VerificationToken vToken = verificationTokenRepository.findByToken(existingVerificationToken);
        vToken.updateToken(UUID.randomUUID().toString());
        vToken = verificationTokenRepository.save(vToken);
        return vToken;
    }
}
