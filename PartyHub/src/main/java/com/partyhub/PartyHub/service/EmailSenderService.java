package com.partyhub.PartyHub.service;

import jakarta.mail.MessagingException;
import org.springframework.data.util.Pair;

import java.util.List;

public interface EmailSenderService {
    void sendEmail(String to, String subject, String body);

    void sendEmailWithAttachments(String userEmail, String yourTickets, String emailBody, List<Pair<String,byte[]>> qrCodeAttachments) throws MessagingException;
}
