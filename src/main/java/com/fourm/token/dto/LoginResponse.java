package com.fourm.token.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private Long doctorId;
    private Long organizationId;
}