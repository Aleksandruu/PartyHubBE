package com.partyhub.PartyHub.service;

public interface EmailSenderService {
    void sendEmail(String to, String subject, String body);
    void sendHtmlEmail(String to, String subject, String body);

}
