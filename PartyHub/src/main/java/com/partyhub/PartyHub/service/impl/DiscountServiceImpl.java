package com.partyhub.PartyHub.service.impl;

import com.partyhub.PartyHub.dto.ChargeRequest;
import com.partyhub.PartyHub.entities.Discount;
import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.User;
import com.partyhub.PartyHub.exceptions.UserNotFoundException;
import com.partyhub.PartyHub.repository.DiscountRepository;
import com.partyhub.PartyHub.service.DiscountService;
import com.partyhub.PartyHub.service.EventService;
import com.partyhub.PartyHub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final EventService eventService;
    private final UserService userService;
    private final DiscountRepository discountRepository;
    @Override
    public Discount saveDiscount(Discount discount) {
        return discountRepository.save(discount);
    }
    @Override
    public Optional<Discount> findByCode(String code) {
        return discountRepository.findByCode(code);
    }

    @Override
    public void deleteDiscountByCode(String code) {
        discountRepository.deleteByCode(code);
    }
    @Override
    public BigDecimal applyDiscounts(ChargeRequest chargeRequest, EventService eventService, UserService userService) {
        AtomicReference<BigDecimal> discountAmount = new AtomicReference<>(BigDecimal.ZERO);
        Optional<Event> eventOptional = eventService.getEventById(chargeRequest.getEventId());

        if (eventOptional.isEmpty()) {
            return discountAmount.get();
        }

        Event event = eventOptional.get();
        BigDecimal eventPrice = BigDecimal.valueOf(event.getPrice());

        if (!chargeRequest.getDiscountCode().isEmpty()) {
            findByCode(chargeRequest.getDiscountCode()).ifPresent(discount -> {
                BigDecimal discountValue = eventPrice.multiply(BigDecimal.valueOf(discount.getDiscountValue()).divide(BigDecimal.valueOf(100)));
                discountAmount.set(discountAmount.get().add(discountValue));
                deleteDiscountByCode(chargeRequest.getDiscountCode());
            });
        }

        if (!chargeRequest.getReferralEmail().isEmpty()) {
            userService.findByEmail(chargeRequest.getReferralEmail()).ifPresent(user -> {
                BigDecimal referralDiscountValue = eventPrice.multiply(BigDecimal.valueOf(event.getDiscount()).divide(BigDecimal.valueOf(100))).multiply(BigDecimal.valueOf(chargeRequest.getTickets()));
                discountAmount.set(discountAmount.get().add(referralDiscountValue));

                int discountForNextTicket = user.getUserDetails().getDiscountForNextTicket() + referralDiscountValue.intValue();
                user.getUserDetails().setDiscountForNextTicket(discountForNextTicket);
                userService.save(user);
            });
        }

        return discountAmount.get();
    }
}
