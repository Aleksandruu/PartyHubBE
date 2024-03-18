package com.partyhub.PartyHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.partyhub.PartyHub.dto.EventDto;
import com.partyhub.PartyHub.dto.EventStatisticsDTO;
import com.partyhub.PartyHub.dto.EventSummaryDto;
import com.partyhub.PartyHub.entities.Discount;
import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.Statistics;
import com.partyhub.PartyHub.entities.Ticket;
import com.partyhub.PartyHub.mappers.EventMapper;
import com.partyhub.PartyHub.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/admin")
public class AdminController {
    private final EventService eventService;
    private final EventMapper eventMapper;
    private final ObjectMapper objectMapper;
    private final TicketService ticketService;
    private final EmailSenderService emailSenderService;
    private final DiscountService discountService;
    private StatisticsService statisticsService;




    @PostMapping("/event")
    public ResponseEntity<Event> addEvent(@Valid @RequestParam("eventData") String eventDataJson,
                                          @RequestParam("mainBanner") MultipartFile mainBannerFile,
                                          @RequestParam("secondaryBanner") MultipartFile secondaryBannerFile) {
        try {
            EventDto eventDto = objectMapper.readValue(eventDataJson, EventDto.class);
            Event event = eventMapper.dtoToEvent(eventDto);

            byte[] mainBanner = processBannerFile(mainBannerFile);
            byte[] secondaryBanner = processBannerFile(secondaryBannerFile);

            event.setMainBanner(mainBanner);
            event.setSecondaryBanner(secondaryBanner);

            Statistics statistics = new Statistics();
            event.setStatistics(statistics);

            Event savedEvent = eventService.addEvent(event);
            return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private byte[] processBannerFile(MultipartFile bannerFile) {
        try {
            if (bannerFile != null && !bannerFile.isEmpty()) {
                return bannerFile.getBytes();
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @PutMapping("event/{id}")
    public ResponseEntity<ApiResponse> editEvent(@PathVariable UUID id, @RequestBody EventDto eventDto) {
        try {
            Event event = eventMapper.dtoToEvent(eventDto);
            Event updatedEvent = eventService.editEvent(id, event);
            if (updatedEvent != null) {
                return new ResponseEntity<>(new ApiResponse(true, "Event updated!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse(false, "Event not found!"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, "Event not updated!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventSummaryDto>> getAllEventSummaries() {
        List<EventSummaryDto> eventSummaries = eventService.getAllEventSummaries();
        return new ResponseEntity<>(eventSummaries, HttpStatus.OK);
    }

    @PostMapping("/events/{eventId}/invites")
    public ResponseEntity<?> generateAndSendInvites(@PathVariable UUID eventId, @RequestBody Integer numberOfInvites) {
        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        List<Ticket> invites = new ArrayList<>();
        for (int i = 0; i < numberOfInvites; i++) {
            Ticket invite = new Ticket(UUID.randomUUID(),null, 0, "invite", event);
            invites.add(ticketService.saveTicket(invite));
        }

        Statistics stats = event.getStatistics();
        if (stats == null) {
            stats = new Statistics();
            stats.setEvent(event);
            event.setStatistics(stats);
        }
        stats.setGeneratedInvites(stats.getGeneratedInvites() + numberOfInvites);
        statisticsService.save(stats);

        String emailBody = invites.stream()
                .map(invite -> "Invitation Code: " + invite.getId().toString())
                .collect(Collectors.joining("\n"));
        emailSenderService.sendEmail("danielmamara71@gmail.com", "Event Invitations", emailBody);

        return ResponseEntity.ok("Invitations generated and sent successfully.");
    }

    @PostMapping("/discount")
    public ResponseEntity<ApiResponse> createDiscount(@RequestParam UUID eventId,
                                                      @RequestParam int discountValue) {
        try {
            Optional<Event> eventOptional = eventService.getEventById(eventId);
            if (eventOptional.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse(false, "Event not found!"), HttpStatus.NOT_FOUND);
            }


            String code = generateRandomCode();

            Discount discount = new Discount();
            discount.setCode(code);
            discount.setDiscountValue(discountValue);
            discount.setEventId(eventId);

            discountService.saveDiscount(discount);

            return new ResponseEntity<>(new ApiResponse(true, code), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, "Discount not created!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private String generateRandomCode() {
        String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
        int CODE_LENGTH = 10;
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        Random random = new Random();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    @GetMapping("/events/upcoming")
    public ResponseEntity<List<EventSummaryDto>> getUpcomingEvents() {
        LocalDate currentDate = LocalDate.now();
        List<EventSummaryDto> upcomingEvents = eventService.getUpcomingEvents()
                .stream()
                .filter(event -> event.getDate().isAfter(currentDate) || event.getDate().isEqual(currentDate))
                .map(event -> new EventSummaryDto(event.getId(), event.getName(), event.getCity()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(upcomingEvents, HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}/data")
    public ResponseEntity<?> getEventData(@PathVariable UUID eventId) {
        Optional<EventStatisticsDTO> eventStatisticsDTO = eventService.getEventStatisticsDTO(eventId);

        return eventStatisticsDTO
                .map(dto -> ResponseEntity.ok(dto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
