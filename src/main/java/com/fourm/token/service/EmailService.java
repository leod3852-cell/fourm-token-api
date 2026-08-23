package com.fourm.token.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    public void sendFollowupEmail(String toEmail, String patientName, String doctorName,
                                  String followupDate, String notes) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.set("accept", "application/json");

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", "QueueCare Clinic");
            sender.put("email", "fourmsoftware@gmail.com");

            Map<String, Object> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            recipient.put("name", patientName);

            String textContent =
                    "Dear " + patientName + ",\n\n" +
                            "Thank you for visiting QueueCare Clinic.\n\n" +
                            "Your doctor, " + doctorName + ", has recommended a follow-up visit.\n\n" +
                            "Follow-up Date: " + followupDate + "\n" +
                            (notes != null && !notes.isEmpty() ? "Notes: " + notes + "\n" : "") +
                            "\nPlease visit the clinic on or around the mentioned date.\n\n" +
                            "Regards,\nQueueCare Clinic";

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", new Object[]{recipient});
            body.put("subject", "QueueCare — Follow-up Appointment Reminder");
            body.put("textContent", textContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

            System.out.println("Follow-up email sent to " + toEmail);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
}