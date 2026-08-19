package com.fourm.token.repository;

import com.fourm.token.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByPhoneOrderByIdDesc(String phone);
}