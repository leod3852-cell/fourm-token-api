package com.fourm.token.controller;

import com.fourm.token.dto.PublicBookingRequest;
import com.fourm.token.entity.Doctor;
import com.fourm.token.entity.Patient;
import com.fourm.token.entity.Token;
import com.fourm.token.repository.DoctorRepository;
import com.fourm.token.repository.PatientRepository;
import com.fourm.token.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fourm.token.repository.OrganizationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    // list available doctors for an organization (public, no login)
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getDoctors(@RequestParam Long organizationId) {
        return ResponseEntity.ok(doctorRepository.findByOrganizationId(organizationId));
    }

    // book a token - public, no login
    @PostMapping("/book")
    public ResponseEntity<?> bookToken(@RequestBody PublicBookingRequest request) {
        try {
            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            Patient patient = new Patient();
            patient.setName(request.getName());
            patient.setAge(request.getAge());
            patient.setGender(request.getGender());
            patient.setPhone(request.getPhone());
            patient.setEmail(request.getEmail());
            patient.setReasonForVisit(request.getReasonForVisit());
            patient.setIsEmergency(false);
            patient.setOrganizationId(request.getOrganizationId());
            patient = patientRepository.save(patient);

            Token token = tokenService.bookTokenPublicly(patient, doctor);

            Map<String, Object> response = new HashMap<>();
            response.put("tokenNo", token.getTokenNo());
            response.put("patientName", patient.getName());
            response.put("doctorName", doctor.getName());
            response.put("status", token.getStatus());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Booking failed: " + e.getMessage()));
        }
    }
    @Autowired
    private OrganizationRepository organizationRepository;

    // get organization name - public, no login
    @GetMapping("/organization")
    public ResponseEntity<?> getOrganization(@RequestParam Long organizationId) {
        return organizationRepository.findById(organizationId)
                .map(org -> ResponseEntity.ok(Map.of("id", org.getId(), "name", org.getName())))
                .orElse(ResponseEntity.notFound().build());
    }
}