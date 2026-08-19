package com.fourm.token.controller;

import com.fourm.token.entity.Doctor;
import com.fourm.token.entity.Token;
import com.fourm.token.entity.User;
import com.fourm.token.enums.TokenStatus;
import com.fourm.token.repository.DoctorRepository;
import com.fourm.token.repository.TokenRepository;
import com.fourm.token.repository.UserRepository;
import com.fourm.token.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Token> allTokens;

        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            allTokens = tokenRepository.findByCreatedAtBetween(start, end);
        } else {
            allTokens = tokenService.getAllTokens();
        }

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        List<Doctor> doctors;

        if (isDoctor) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            Long myDoctorId = user.getDoctorId();

            doctors = doctorRepository.findAll().stream()
                    .filter(d -> d.getId().equals(myDoctorId))
                    .collect(Collectors.toList());

            allTokens = allTokens.stream()
                    .filter(t -> t.getDoctor().getId().equals(myDoctorId))
                    .collect(Collectors.toList());
        } else {
            doctors = doctorRepository.findAll();
        }

        long total = allTokens.size();
        long completed = allTokens.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count();
        long waiting = allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.WAITING || t.getStatus() == TokenStatus.CURRENT)
                .count();

        List<Token> finalAllTokens = allTokens;
        List<Map<String, Object>> byDoctor = doctors.stream().map(doc -> {
            long docTotal = finalAllTokens.stream().filter(t -> t.getDoctor().getId().equals(doc.getId())).count();
            long docCompleted = finalAllTokens.stream()
                    .filter(t -> t.getDoctor().getId().equals(doc.getId()) && t.getStatus() == TokenStatus.COMPLETED)
                    .count();

            Map<String, Object> map = new HashMap<>();
            map.put("doctorId", doc.getId());
            map.put("doctorName", doc.getName());
            map.put("totalTokens", docTotal);
            map.put("completedTokens", docCompleted);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalTokens", total);
        response.put("completed", completed);
        response.put("waiting", waiting);
        response.put("byDoctor", byDoctor);

        return ResponseEntity.ok(response);
    }
}