package com.fourm.token.controller;

import com.fourm.token.dto.CompleteRequest;
import com.fourm.token.entity.Token;
import com.fourm.token.service.PermissionService;
import com.fourm.token.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PermissionService permissionService;

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{doctorId}/complete")
    public ResponseEntity<?> complete(@PathVariable Long doctorId, Authentication authentication,
                                      @RequestBody(required = false) CompleteRequest request) {
        permissionService.checkPermission("COMPLETE", authentication);
        Boolean followupNeeded = request != null ? request.getFollowupNeeded() : false;
        String followupDate = request != null ? request.getFollowupDate() : null;
        String followupNotes = request != null ? request.getFollowupNotes() : null;
        Token token = tokenService.completeConsultation(doctorId, followupNeeded, followupDate, followupNotes);
        return ResponseEntity.ok(token);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{doctorId}/hold")
    public ResponseEntity<?> hold(@PathVariable Long doctorId, Authentication authentication) {
        permissionService.checkPermission("HOLD", authentication);
        Token token = tokenService.holdConsultation(doctorId);
        return ResponseEntity.ok(token);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PostMapping("/{doctorId}/resume/{tokenId}")
    public ResponseEntity<?> resume(@PathVariable Long doctorId, @PathVariable Long tokenId) {
        Token token = tokenService.resumeHold(doctorId, tokenId);
        return ResponseEntity.ok(token);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/{doctorId}/held")
    public ResponseEntity<List<Token>> getHeld(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.getHeldTokens(doctorId));
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/{doctorId}/queue")
    public ResponseEntity<List<Token>> getQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.getWaitingQueue(doctorId));
    }
}