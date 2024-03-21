package com.partyhub.PartyHub.controller;


import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.Statistics;
import com.partyhub.PartyHub.entities.Ticket;
import com.partyhub.PartyHub.service.StatisticsService;
import com.partyhub.PartyHub.service.TicketService;
import com.partyhub.PartyHub.util.TicketNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scanner")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ScannerController {

    private final TicketService ticketService;
    private final StatisticsService statisticsService;

    @PostMapping("/validate/{ticketId}")
    public ResponseEntity<ApiResponse> validateTicket(@PathVariable UUID ticketId) {
        ApiResponse response = ticketService.validateTicket(ticketId);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }
}
