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

        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        try {
            User user = userRepository.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("No user found with username: " + username);
            }

            return new MyUserDetails(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
