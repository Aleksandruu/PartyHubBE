package com.partyhub.PartyHub.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.partyhub.PartyHub.dto.ChargeRequest;
import com.partyhub.PartyHub.dto.PaymentResponse;
import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.Ticket;
import com.partyhub.PartyHub.entities.User;
import com.partyhub.PartyHub.exceptions.EventNotFoundException;
import com.partyhub.PartyHub.service.*;
import com.partyhub.PartyHub.util.QRCodeUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentController {

    private final EventService eventService;
    private final TicketService ticketService;
    private final EmailSenderService emailSenderService;
    private final DiscountService discountService;
    private final UserService userService;

    @Value("${stripe.keys.secret}")
    private String apiKey;

    @PostMapping("/charge")
    public PaymentResponse chargeCard(@RequestBody ChargeRequest chargeRequest)
            throws Exception {
        Stripe.apiKey = apiKey;

        BigDecimal discount = discountService.applyDiscounts(chargeRequest, eventService, userService);

        Event event = eventService.getEventById(chargeRequest.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found."));
        BigDecimal price = BigDecimal.valueOf(chargeRequest.getTickets())
                .multiply(BigDecimal.valueOf(event.getPrice()))
                .multiply(BigDecimal.valueOf(100))
                .subtract(discount);

        ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(price.longValueExact())
                .setCurrency("RON")
                .setDescription("Payment for " + chargeRequest.getTickets() + " tickets")
                .setSource(chargeRequest.getToken())
                .build();

        Charge charge = Charge.create(params);

        List<Pair<String, byte[]>> qrCodeAttachments = generateTicketsAndQRCodeAttachments(chargeRequest, event);

        emailSenderService.sendEmailWithAttachments(chargeRequest.getUserEmail(), "Your Tickets", "Here are your tickets:", qrCodeAttachments);

        return new PaymentResponse(charge.getId(), BigDecimal.valueOf(charge.getAmount()), charge.getCurrency(), charge.getDescription());
    }

    private List<Pair<String, byte[]>> generateTicketsAndQRCodeAttachments(ChargeRequest chargeRequest, Event event)
            throws Exception {
        List<Pair<String, byte[]>> qrCodeAttachments = new ArrayList<>();
        for (int i = 0; i < chargeRequest.getTickets(); i++) {
            Ticket ticket = new Ticket(UUID.randomUUID(), null, 0, "ticket", event);
            ticketService.saveTicket(ticket);
            String qrCodeData = ticket.getId().toString();
            byte[] qrCodeImage = QRCodeUtil.generateQRCodeImage(qrCodeData, 300, 300);
            qrCodeAttachments.add(Pair.of("Ticket-" + ticket.getId() + ".png", qrCodeImage));
        }
        return qrCodeAttachments;
    }
}
