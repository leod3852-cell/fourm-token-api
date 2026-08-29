package com.fourm.token.controller;

import com.fourm.token.dto.TokenRegisterRequest;
import com.fourm.token.entity.*;
import com.fourm.token.repository.DoctorRepository;
import com.fourm.token.repository.PatientRepository;
import com.fourm.token.service.PermissionService;
import com.fourm.token.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fourm.token.dto.PatientUpdateRequest;
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PermissionService permissionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tokens")
    public ResponseEntity<?> registerToken(@RequestBody TokenRegisterRequest request, HttpServletRequest httpRequest) {
        Long organizationId = (Long) httpRequest.getAttribute("organizationId");

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setBp(request.getBp());
        patient.setWeight(request.getWeight());
        patient.setReasonForVisit(request.getReasonForVisit());
        patient.setIsEmergency(request.getIsEmergency() != null && request.getIsEmergency());
        patient.setEmail(request.getEmail());
        patient.setOrganizationId(organizationId);
        patient = patientRepository.save(patient);

        Token token = tokenService.registerToken(patient, doctor);
        return ResponseEntity.ok(token);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping("/doctors/{doctorId}/call-next")
    public ResponseEntity<?> callNext(@PathVariable Long doctorId, Authentication authentication) {
        permissionService.checkPermission("CALL_NEXT", authentication);
        Token token = tokenService.callNextPatient(doctorId);
        return ResponseEntity.ok(token);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/doctors")
    public ResponseEntity<?> getDoctors(HttpServletRequest request) {
        Long organizationId = (Long) request.getAttribute("organizationId");
        return ResponseEntity.ok(doctorRepository.findByOrganizationId(organizationId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tokens")
    public ResponseEntity<?> getAllTokens() {
        return ResponseEntity.ok(tokenService.getAllTokens());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/patients/lookup")
    public ResponseEntity<Map<String, Object>> lookupPatient(@RequestParam String phone, HttpServletRequest httpRequest) {
        Long organizationId = (Long) httpRequest.getAttribute("organizationId");
        List<Patient> previousVisits = patientRepository.findByPhoneOrderByIdDesc(phone).stream()
                .filter(p -> organizationId.equals(p.getOrganizationId()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();

        if (previousVisits.isEmpty()) {
            response.put("found", false);
        } else {
            Patient latest = previousVisits.get(0);
            response.put("found", true);
            response.put("name", latest.getName());
            response.put("age", latest.getAge());
            response.put("gender", latest.getGender());
            response.put("email", latest.getEmail());
            response.put("visitCount", previousVisits.size());
        }

        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/patients/history")
    public ResponseEntity<?> getPatientHistory(@RequestParam String phone) {
        return ResponseEntity.ok(tokenService.getPatientHistory(phone));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/doctors/{doctorId}/availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long doctorId, @RequestBody Map<String, Boolean> body) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setIsAvailable(body.get("isAvailable"));
        doctorRepository.save(doctor);
        return ResponseEntity.ok(doctor);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/doctors/{doctorId}/token-limit")
    public ResponseEntity<?> setTokenLimit(@PathVariable Long doctorId, @RequestBody Map<String, Integer> body) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setMaxTokensPerDay(body.get("maxTokensPerDay"));
        doctorRepository.save(doctor);
        return ResponseEntity.ok(doctor);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/patients/{patientId}")
    public ResponseEntity<?> updatePatient(@PathVariable Long patientId, @RequestBody PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (request.getBp() != null) patient.setBp(request.getBp());
        if (request.getWeight() != null) patient.setWeight(request.getWeight());
        if (request.getReasonForVisit() != null) patient.setReasonForVisit(request.getReasonForVisit());
        if (request.getEmail() != null) patient.setEmail(request.getEmail());
        if (request.getIsEmergency() != null) patient.setIsEmergency(request.getIsEmergency());

        patientRepository.save(patient);
        return ResponseEntity.ok(patient);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/doctors/{doctorId}/booking-window")
    public ResponseEntity<?> setBookingWindow(@PathVariable Long doctorId, @RequestBody Map<String, String> body) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setBookingStartTime(body.get("bookingStartTime"));
        doctor.setBookingEndTime(body.get("bookingEndTime"));
        doctorRepository.save(doctor);
        return ResponseEntity.ok(doctor);
    }
}