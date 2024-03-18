package com.partyhub.PartyHub.service.impl;

import com.partyhub.PartyHub.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl  implements EmailSenderService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
    public void sendEmailWithAttachments(String to, String subject, String body, List<Pair<String, byte[]>> attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        for (Pair<String, byte[]> attachment : attachments) {
            helper.addAttachment(attachment.getFirst(), new ByteArrayResource(attachment.getSecond()));
        }

        mailSender.send(message);
    }
}
