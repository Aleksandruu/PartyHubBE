package com.partyhub.PartyHub.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @Min(value = 18, message = "Age must be at least 18")
    private int age;

    @Min(value = 0, message = "Discount for next ticket must be positive")
    private int discountForNextTicket;
}
