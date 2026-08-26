package com.fourm.token.repository;

import com.fourm.token.entity.Token;
import com.fourm.token.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findFirstByDoctorIdAndStatusOrderByCreatedAtAsc(Long doctorId, TokenStatus status);

    Optional<Token> findFirstByDoctorIdAndStatus(Long doctorId, TokenStatus status);

    List<Token> findByDoctorIdAndStatus(Long doctorId, TokenStatus status);
    List<Token> findByDoctorOrganizationIdAndCreatedAtBetween(Long organizationId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Token> findByStatus(TokenStatus status);
    List<Token> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Token> findByPatientPhoneAndDoctorOrganizationIdAndStatusIn(String phone, Long organizationId, List<TokenStatus> statuses);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByDoctorIdAndCreatedAtBetween(Long doctorId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Token> findByPatient_PhoneOrderByCreatedAtDesc(String phone);
}