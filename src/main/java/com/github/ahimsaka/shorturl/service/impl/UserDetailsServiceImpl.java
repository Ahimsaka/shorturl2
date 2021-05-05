package com.github.ahimsaka.shorturl.service.impl;

import com.github.ahimsaka.shorturl.entity.User;
import com.github.ahimsaka.shorturl.repository.UserRepository;
import com.github.ahimsaka.shorturl.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Could not find user: " + username);
        }

        return new MyUserDetails(user);
    }
}
