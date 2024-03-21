package com.partyhub.PartyHub.service.impl;

import com.partyhub.PartyHub.entities.Event;
import com.partyhub.PartyHub.entities.User;
import com.partyhub.PartyHub.entities.UserDetails;
import com.partyhub.PartyHub.exceptions.UserNotFoundException;
import com.partyhub.PartyHub.repository.UserRepository;
import com.partyhub.PartyHub.service.EventService;
import com.partyhub.PartyHub.service.UserService;
import com.partyhub.PartyHub.util.PromoCodeGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EventService eventService;

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
  
    @Override
    public Optional<User> findByVerificationToken(UUID verificationToken) {
        return userRepository.findByVerificationToken(verificationToken);
    }
  
    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
  
    @Override
    public void save(User user) {
        if (user.getPromoCode() == null || user.getPromoCode().isEmpty()) {
            String promoCode;
            do {
                promoCode = PromoCodeGenerator.generatePromoCode(user.getUserDetails().getFullName());
            } while (userRepository.existsByPromoCode(promoCode));
            user.setPromoCode(promoCode);
        }
        userRepository.save(user);
    }
  
    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void generateAndSetPromoCodeForUser(UUID userId) {
        User user = findById(userId);
        if (StringUtils.isEmpty(user.getPromoCode())) {
            String promoCode;
            do {
                promoCode = PromoCodeGenerator.generatePromoCode(user.getUserDetails().getFullName());
            } while (userRepository.existsByPromoCode(promoCode));

            user.setPromoCode(promoCode);
            userRepository.save(user);
        }
    }
  
    @Override
    public boolean isPromoCodeInUse(String promoCode) {
        return userRepository.existsByPromoCode(promoCode);
    }

    @Override
    public boolean doesPromoCodeExist(String promoCode) {
        return userRepository.findByPromoCode(promoCode).isPresent();
    }
    @Override
    public Optional<User> findByPromoCode(String promoCode) {
        return userRepository.findByPromoCode(promoCode);
    }

    public void increaseDiscountForNextTicket(String email, UUID eventId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        Event event = eventService.getEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        UserDetails userDetails = user.getUserDetails();
        if (userDetails != null) {
            int discountAsInt = (int) event.getDiscount();
            userDetails.setDiscountForNextTicket(discountAsInt);
            userRepository.save(user);
        } else {
            throw new IllegalStateException("UserDetails not found for user with email: " + email);
        }
    }
}

