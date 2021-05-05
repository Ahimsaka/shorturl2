package com.github.ahimsaka.shorturl.repository;

import com.github.ahimsaka.shorturl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
