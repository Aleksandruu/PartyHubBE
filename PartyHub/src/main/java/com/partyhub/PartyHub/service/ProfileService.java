package com.partyhub.PartyHub.service;

import com.partyhub.PartyHub.dto.ProfileDto;


public interface ProfileService {
    ProfileDto getProfile(String email);
    void updateProfileDetails(String email, ProfileDto updatedProfile);
     void deleteProfile(String email);
     void resetPassword(String email, String newPassword);
}