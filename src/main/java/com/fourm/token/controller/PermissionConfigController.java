package com.fourm.token.controller;

import com.fourm.token.entity.PermissionConfig;
import com.fourm.token.repository.PermissionConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
public class PermissionConfigController {

    @Autowired
    private PermissionConfigRepository permissionConfigRepository;

    // GET all permission configs
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping
    public ResponseEntity<List<PermissionConfig>> getAll() {
        return ResponseEntity.ok(permissionConfigRepository.findAll());
    }

    // UPDATE a permission config
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionConfig> update(
            @PathVariable Long id,
            @RequestBody PermissionConfig updated) {

        PermissionConfig config = permissionConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission config not found"));

        config.setAllowAdmin(updated.getAllowAdmin());
        config.setAllowDoctor(updated.getAllowDoctor());

        return ResponseEntity.ok(permissionConfigRepository.save(config));
    }
}