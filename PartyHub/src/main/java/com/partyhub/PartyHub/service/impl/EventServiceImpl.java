package com.partyhub.PartyHub.service.impl;

import com.partyhub.PartyHub.dto.EventStatisticsDTO;
import com.partyhub.PartyHub.dto.EventSummaryDto;
import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.Statistics;
import com.partyhub.PartyHub.exceptions.EventNotFoundException;
import com.partyhub.PartyHub.repository.EventRepository;
import com.partyhub.PartyHub.service.EventService;
import com.partyhub.PartyHub.service.StatisticsService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final StatisticsService statisticsService;




    @Override
    @Transactional
    public Event addEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event editEvent(UUID id, Event eventDetails) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found for this id :: " + id));

        existingEvent.setName(eventDetails.getName());
        existingEvent.setMainBanner(eventDetails.getMainBanner());
        existingEvent.setSecondaryBanner(eventDetails.getSecondaryBanner());
        existingEvent.setLocation(eventDetails.getLocation());
        existingEvent.setDate(eventDetails.getDate());
        existingEvent.setDetails(eventDetails.getDetails());
        existingEvent.setPrice(eventDetails.getPrice());
        existingEvent.setDiscount(eventDetails.getDiscount());
        existingEvent.setTicketsNumber(eventDetails.getTicketsNumber());

        return eventRepository.save(existingEvent);
    }

    @Override
    public Optional<Event> getNearestEvent() {
        LocalDate today = LocalDate.now();
        return eventRepository.findTopByDateAfterOrderByDateAsc(today);
    }

    @Override
    public Optional<Event> getEventById(UUID id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<EventSummaryDto> getAllEventSummaries() {
        return eventRepository.findAll().stream()
                .map(event -> new EventSummaryDto(event.getId(), event.getName(), event.getCity(), event.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventSummaryDto> getUpcomingEvents() {
        LocalDate today = LocalDate.now();
        return eventRepository.findByDateAfter(today).stream()
                .map(event -> new EventSummaryDto(event.getName(), event.getCity(), event.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EventStatisticsDTO> getEventStatisticsDTO(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found for this id :: " + eventId));

        Optional<Statistics> statisticsOptional = statisticsService.getStatisticsByEventId(eventId);

        EventStatisticsDTO dto = new EventStatisticsDTO(
                event.getName(),
                event.getLocation(),
                event.getDate(),
                event.getPrice(),
                event.getDiscount(),
                statisticsOptional.orElse(null)
        );

        return Optional.of(dto);
    }
}
