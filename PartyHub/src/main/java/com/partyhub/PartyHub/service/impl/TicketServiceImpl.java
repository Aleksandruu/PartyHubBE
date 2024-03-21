package com.partyhub.PartyHub.service.impl;


import com.partyhub.PartyHub.controller.ApiResponse;
import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.Ticket;
import com.partyhub.PartyHub.exceptions.EventNotFoundException;
import com.partyhub.PartyHub.repository.EventRepository;
import com.partyhub.PartyHub.repository.TicketRepository;
import com.partyhub.PartyHub.service.TicketService;
import com.partyhub.PartyHub.util.TicketNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EventRepository eventRepository;


    @Override
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket generateAndSaveTicketForEvent(float pricePaid, String type, UUID eventId, LocalDateTime validationDate) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));


        Ticket ticket = new Ticket();
        ticket.setValidationDate(validationDate);
        ticket.setPricePaid(pricePaid);
        ticket.setType(type);
        ticket.setEvent(event);


        return ticketRepository.save(ticket);
    }
    @Override
    public Optional<Ticket> findById(UUID ticketId) {
        return ticketRepository.findById(ticketId);

    }

    @Override
    public ApiResponse validateTicket(UUID ticketId) {
        Ticket ticket = findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found!"));

        if (ticket.getValidationDate() == null) {

            saveTicket(ticket);
            return new ApiResponse(true, "Ticket has been successfully validated.");
        } else {
            return new ApiResponse(false, "Ticket is already validated at " + ticket.getValidationDate().toString());
        }
    }

}
