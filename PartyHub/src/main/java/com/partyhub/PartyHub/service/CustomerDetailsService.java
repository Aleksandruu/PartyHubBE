package com.partyhub.PartyHub.service;

import com.partyhub.PartyHub.entities.CustomerDetails;

public interface CustomerDetailsService {
    void save(CustomerDetails customerDetails);

    CustomerDetails create(int age, String fullName);
}