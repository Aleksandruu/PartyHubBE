package com.partyhub.PartyHub.controller;

import com.partyhub.PartyHub.dto.ProfileDto;
import com.partyhub.PartyHub.exceptions.UserNotFoundException;
import com.partyhub.PartyHub.service.ProfileService;
import com.partyhub.PartyHub.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/profile")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDto> getProfile() {
        String email = SecurityUtil.getCurrentUserEmail();
        ProfileDto profile = profileService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody ProfileDto updatedProfile) {
        String email = SecurityUtil.getCurrentUserEmail();
        try {
            profileService.updateProfileDetails(email, updatedProfile);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false,  "Failed to update profile"));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteProfile() {
        String email = SecurityUtil.getCurrentUserEmail();
        try {
            profileService.deleteProfile(email);
            return ResponseEntity.ok(new ApiResponse(true, "Account deleted successfully!"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false,"User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Failed to delete profile"));
        }
    }

}
