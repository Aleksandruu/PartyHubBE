package com.partyhub.PartyHub.service;

import com.partyhub.PartyHub.dto.ChargeRequest;
import com.partyhub.PartyHub.entities.Discount;

import java.math.BigDecimal;
import java.util.Optional;

public interface DiscountService {
    Discount saveDiscount(Discount discount);
    Optional<Discount> findByCode(String code);
     void deleteDiscountByCode(String code);

    BigDecimal applyDiscounts(ChargeRequest chargeRequest, EventService eventService, UserService userService);


}
