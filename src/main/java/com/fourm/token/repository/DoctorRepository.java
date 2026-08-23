package com.fourm.token.repository;

import com.fourm.token.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByOrganizationId(Long organizationId);
}