package com.fourm.token.repository;

import com.fourm.token.entity.PermissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionConfigRepository extends JpaRepository<PermissionConfig, Long> {
    Optional<PermissionConfig> findByActionKey(String actionKey);
}