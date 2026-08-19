package com.fourm.token.service;

import com.fourm.token.entity.PermissionConfig;
import com.fourm.token.repository.PermissionConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    @Autowired
    private PermissionConfigRepository permissionConfigRepository;

    @PostConstruct
    public void initDefaults() {
        createIfMissing("CALL_NEXT", true, false);
        createIfMissing("COMPLETE", true, true);
        createIfMissing("HOLD", true, true);
    }

    private void createIfMissing(String key, boolean admin, boolean doctor) {
        if (permissionConfigRepository.findByActionKey(key).isEmpty()) {
            PermissionConfig config = new PermissionConfig();
            config.setActionKey(key);
            config.setAllowAdmin(admin);
            config.setAllowDoctor(doctor);
            permissionConfigRepository.save(config);
        }
    }

    public void checkPermission(String actionKey, Authentication authentication) {
        PermissionConfig config = permissionConfigRepository.findByActionKey(actionKey)
                .orElseThrow(() -> new IllegalStateException("Permission config not found: " + actionKey));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        boolean allowed = (isAdmin && config.getAllowAdmin()) || (isDoctor && config.getAllowDoctor());

        if (!allowed) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
    }
}