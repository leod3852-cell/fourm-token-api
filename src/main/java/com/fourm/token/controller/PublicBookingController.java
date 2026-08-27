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
import com.fourm.token.repository.TokenRepository;
import com.fourm.token.enums.TokenStatus;
import java.util.stream.Collectors;
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
    @Autowired
    private TokenRepository tokenRepository;

    // check token status - public, no login (search by token number + phone for security)
    @GetMapping("/token-status")
    public ResponseEntity<?> getTokenStatus(@RequestParam String tokenNo, @RequestParam String phone) {
        Token token = tokenRepository.findAll().stream()
                .filter(t -> t.getTokenNo().equals(tokenNo) && t.getPatient().getPhone().equals(phone))
                .findFirst()
                .orElse(null);

        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token not found. Check your token number and phone."));
        }

        Doctor doctor = token.getDoctor();

        // current token being served for this doctor
        Token currentToken = tokenRepository.findFirstByDoctorIdAndStatus(doctor.getId(), TokenStatus.CURRENT)
                .orElse(null);

        // count patients ahead in waiting queue (created before this token, still waiting)
        long patientsAhead = tokenRepository.findByDoctorIdAndStatus(doctor.getId(), TokenStatus.WAITING).stream()
                .filter(t -> t.getCreatedAt().isBefore(token.getCreatedAt()))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("tokenNo", token.getTokenNo());
        response.put("status", token.getStatus());
        response.put("doctorName", doctor.getName());
        response.put("currentlyServing", currentToken != null ? currentToken.getTokenNo() : "—");
        response.put("patientsAhead", token.getStatus() == TokenStatus.WAITING ? patientsAhead : 0);

        return ResponseEntity.ok(response);
    }

    // find active token(s) by phone alone - public, no login
    @GetMapping("/my-tokens")
    public ResponseEntity<?> getMyTokens(@RequestParam String phone, @RequestParam Long organizationId) {
        List<TokenStatus> activeStatuses = List.of(TokenStatus.WAITING, TokenStatus.CURRENT, TokenStatus.ON_HOLD);
        List<Token> myTokens = tokenRepository.findByPatientPhoneAndDoctorOrganizationIdAndStatusIn(phone, organizationId, activeStatuses);

        List<Map<String, Object>> result = myTokens.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tokenNo", t.getTokenNo());
            map.put("status", t.getStatus());
            map.put("doctorName", t.getDoctor().getName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}