package com.fourm.token.controller;

import com.fourm.token.entity.Token;
import com.fourm.token.repository.TokenRepository;
import com.fourm.token.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TokenRepository tokenRepository;

    // Live queue - TODAY'S tokens only
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping
    public ResponseEntity<List<Token>> getLiveQueue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return ResponseEntity.ok(tokenRepository.findByCreatedAtBetween(startOfDay, endOfDay));
    }

    // History - date range filter
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/history")
    public ResponseEntity<List<Token>> getHistory(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
        return ResponseEntity.ok(tokenRepository.findByCreatedAtBetween(start, end));
    }
}