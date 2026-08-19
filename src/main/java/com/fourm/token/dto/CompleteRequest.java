package com.fourm.token.dto;

import lombok.Data;

@Data
public class CompleteRequest {
    private Boolean followupNeeded;
    private String followupDate;
    private String followupNotes;
}