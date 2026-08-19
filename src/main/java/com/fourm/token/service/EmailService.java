package com.fourm.token.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendFollowupEmail(String toEmail, String patientName, String doctorName,
                                  String followupDate, String notes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("fourmsoftware@gmail.com");
            message.setTo(toEmail);
            message.setSubject("QueueCare — Follow-up Appointment Reminder");
            message.setText(
                    "Dear " + patientName + ",\n\n" +
                            "Thank you for visiting QueueCare Clinic.\n\n" +
                            "Your doctor, " + doctorName + ", has recommended a follow-up visit.\n\n" +
                            "Follow-up Date: " + followupDate + "\n" +
                            (notes != null && !notes.isEmpty() ? "Notes: " + notes + "\n" : "") +
                            "\nPlease visit the clinic on or around the mentioned date.\n\n" +
                            "Regards,\nQueueCare Clinic"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
}