package com.fourm.token.dto;

import lombok.Data;

@Data
public class PublicBookingRequest {
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String email;
    private String reasonForVisit;
    private Long doctorId;
    private Long organizationId;
}