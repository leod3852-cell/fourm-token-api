package com.fourm.token.service;

import com.fourm.token.entity.Doctor;
import com.fourm.token.entity.Patient;
import com.fourm.token.entity.Token;
import com.fourm.token.enums.TokenStatus;
import com.fourm.token.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDate;
import java.util.List;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;

    // Reception/Admin registers a patient -> creates a new token
    public Token registerToken(Patient patient, Doctor doctor) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        long todayCount = tokenRepository.countByDoctorIdAndCreatedAtBetween(doctor.getId(), startOfDay, endOfDay);
        int nextTokenNumber = (int) todayCount + 1;

        Token token = new Token();
        token.setTokenNo("TOKEN-" + nextTokenNumber);
        token.setPatient(patient);
        token.setDoctor(doctor);
        token.setStatus(TokenStatus.WAITING);
        token.setCreatedAt(LocalDateTime.now());
        return tokenRepository.save(token);
    }
    // ONLY ADMIN calls this - picks oldest waiting patient for a doctor
    public Token callNextPatient(Long doctorId) {

        // safety check: doctor should not already have a "current" patient
        tokenRepository.findFirstByDoctorIdAndStatus(doctorId, TokenStatus.CURRENT)
                .ifPresent(t -> {
                    throw new IllegalStateException("Doctor already has a current patient");
                });

        Token next = tokenRepository
                .findFirstByDoctorIdAndStatusOrderByCreatedAtAsc(doctorId, TokenStatus.WAITING)
                .orElseThrow(() -> new NoSuchElementException("No patient waiting for this doctor"));

        next.setStatus(TokenStatus.CURRENT);
        next.setCalledAt(LocalDateTime.now());
        return tokenRepository.save(next);
    }

    // Doctor calls this to finish consultation
    public Token completeConsultation(Long doctorId, Boolean followupNeeded, String followupDate, String followupNotes) {
        Token current = tokenRepository
                .findFirstByDoctorIdAndStatus(doctorId, TokenStatus.CURRENT)
                .orElseThrow(() -> new NoSuchElementException("No active consultation for this doctor"));

        current.setStatus(TokenStatus.COMPLETED);
        current.setCompletedAt(LocalDateTime.now());
        current.setFollowupNeeded(followupNeeded != null && followupNeeded);
        current.setFollowupDate(followupDate);
        current.setFollowupNotes(followupNotes);

        Token saved = tokenRepository.save(current);

        if (saved.getFollowupNeeded() && saved.getPatient().getEmail() != null
                && !saved.getPatient().getEmail().isEmpty()) {
            emailService.sendFollowupEmail(
                    saved.getPatient().getEmail(),
                    saved.getPatient().getName(),
                    saved.getDoctor().getName(),
                    followupDate,
                    followupNotes
            );
        }

        return saved;
    }

    public Token cancelToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new NoSuchElementException("Token not found"));

        if (token.getStatus() != TokenStatus.WAITING) {
            throw new IllegalStateException("Only waiting tokens can be cancelled");
        }

        token.setStatus(TokenStatus.CANCELLED);

        return tokenRepository.save(token);
    }
    // get all waiting tokens for a doctor
    public List<Token> getWaitingQueue(Long doctorId) {
        return tokenRepository.findByDoctorIdAndStatus(doctorId, TokenStatus.WAITING);
    }

    // get all tokens (for live queue / reports)
    public List<Token> getAllTokens() {
        return tokenRepository.findAll();
    }
    // Doctor puts current patient ON HOLD (e.g. sent for test)
    public Token holdConsultation(Long doctorId) {
        Token current = tokenRepository
                .findFirstByDoctorIdAndStatus(doctorId, TokenStatus.CURRENT)
                .orElseThrow(() -> new NoSuchElementException("No active consultation to hold"));

        current.setStatus(TokenStatus.ON_HOLD);
        return tokenRepository.save(current);
    }

    // Doctor resumes a held patient back to CURRENT
    public Token resumeHold(Long doctorId, Long tokenId) {
        // safety: doctor should not already have a CURRENT patient
        tokenRepository.findFirstByDoctorIdAndStatus(doctorId, TokenStatus.CURRENT)
                .ifPresent(t -> {
                    throw new IllegalStateException("Doctor already has a current patient. Complete or hold them first.");
                });

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new NoSuchElementException("Token not found"));

        if (token.getStatus() != TokenStatus.ON_HOLD) {
            throw new IllegalStateException("This token is not on hold");
        }

        token.setStatus(TokenStatus.CURRENT);
        return tokenRepository.save(token);
    }

    // get held tokens for a doctor
    public List<Token> getHeldTokens(Long doctorId) {
        return tokenRepository.findByDoctorIdAndStatus(doctorId, TokenStatus.ON_HOLD);
    }
    // Get all visits/tokens for a patient by phone number
    public List<Token> getPatientHistory(String phone) {
        return tokenRepository.findByPatient_PhoneOrderByCreatedAtDesc(phone);
    }
    // PUBLIC BOOKING - thread-safe, with all validation rules
    public synchronized Token bookTokenPublicly(Patient patient, Doctor doctor) {

        // Rule 4: doctor must be available
        if (doctor.getIsAvailable() != null && !doctor.getIsAvailable()) {
            throw new IllegalStateException("This doctor is currently unavailable for booking. Please choose another doctor or try later.");
        }

        // Rule 1: prevent duplicate active token for same phone number (same organization)
        List<TokenStatus> activeStatuses = List.of(TokenStatus.WAITING, TokenStatus.CURRENT, TokenStatus.ON_HOLD);
        List<Token> existingActive = tokenRepository.findByPatientPhoneAndDoctorOrganizationIdAndStatusIn(
                patient.getPhone(), doctor.getOrganizationId(), activeStatuses);

        if (!existingActive.isEmpty()) {
            throw new IllegalStateException("You already have an active token (" +
                    existingActive.get(0).getTokenNo() + "). Please complete that visit before booking again.");
        }

        // Rule 2: synchronized block ensures no two patients get the same token number
        // even if they book at the exact same millisecond
        return registerToken(patient, doctor);
    }
}