package com.partyhub.PartyHub.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.partyhub.PartyHub.entities.Ticket;
import com.partyhub.PartyHub.service.EmailSenderService;
import com.partyhub.PartyHub.util.QRCodeUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl  implements EmailSenderService {
    private final JavaMailSender mailSender;
    private final QRCodeUtil qrCode;

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
    public static byte[] generateQRCodeImage(String data, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
    @Override
    public void sendTicketEmail(String to, List<Ticket> tickets) throws Exception {
        String subject = "Your Event Tickets";
        String body = "Here are your tickets. Please print them and bring them to the event.";
        List<Pair<String, byte[]>> attachments = new ArrayList<>();

        for (Ticket ticket : tickets) {
            byte[] qrCode = generateQRCodeImage(ticket.getId().toString(), 300, 300);
            attachments.add(Pair.of("Ticket-" + ticket.getId() + ".png", qrCode));
        }

        sendEmailWithAttachments(to, subject, body, attachments);
    }

}
