package com.fourm.token.controller;

import com.fourm.token.entity.Token;
import com.fourm.token.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fourm.token.service.TokenService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenService tokenService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping
    public ResponseEntity<List<Token>> getLiveQueue(HttpServletRequest request) {
        Long organizationId = (Long) request.getAttribute("organizationId");
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return ResponseEntity.ok(
                tokenRepository.findByDoctorOrganizationIdAndCreatedAtBetween(organizationId, startOfDay, endOfDay));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/history")
    public ResponseEntity<List<Token>> getHistory(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long organizationId = (Long) request.getAttribute("organizationId");
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
        return ResponseEntity.ok(
                tokenRepository.findByDoctorOrganizationIdAndCreatedAtBetween(organizationId, start, end));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping("/{tokenId}/cancel")
    public ResponseEntity<Token> cancelToken(
            HttpServletRequest request,
            @PathVariable Long tokenId) {

        tokenService.cancelToken(tokenId);

        return ResponseEntity.ok(
                tokenRepository.findById(tokenId).orElseThrow()
        );
    }
}