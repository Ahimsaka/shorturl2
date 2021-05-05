package com.github.ahimsaka.shorturl.repository;

import com.github.ahimsaka.shorturl.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
