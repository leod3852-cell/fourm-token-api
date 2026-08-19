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

    List<Token> findByStatus(TokenStatus status);
    List<Token> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}