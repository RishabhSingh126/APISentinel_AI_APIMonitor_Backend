package com.portfolio.apimonitor.controller;

import com.portfolio.apimonitor.model.ApiEndpoint;
import com.portfolio.apimonitor.model.User;
import com.portfolio.apimonitor.repository.ApiEndpointRepository;
import com.portfolio.apimonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class ApiConfigController {

    private final ApiEndpointRepository apiEndpointRepository;
    private final UserRepository userRepository;

    /**
     * Requirement: GET /api/endpoints (List user's APIs)
     * This fetches only the APIs that belong to the currently logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<ApiEndpoint>> getUserEndpoints(Authentication authentication) {
        // 1. Get the email of the logged-in user from the JWT token
        String email = authentication.getName();

        // 2. Find the user in the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Fetch all APIs, but filter ONLY the ones belonging to this user ID
        List<ApiEndpoint> userApis = apiEndpointRepository.findAll().stream()
                .filter(api -> api.getUser().getId().equals(user.getId()))
                .toList();

        return ResponseEntity.ok(userApis);
    }

    /**
     * Requirement: POST /api/endpoints (Add new API)
     * This saves a new API to the database and links it to the logged-in user.
     */
    @PostMapping
    public ResponseEntity<ApiEndpoint> addEndpoint(@RequestBody ApiEndpoint newApi, Authentication authentication) {
        // 1. Get the logged-in user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Link the new API to this specific user
        newApi.setUser(user);

        // 3. Set default status to UP so it looks nice on the dashboard before the first ping
        newApi.setStatus("UP");

        // 4. Save to the database!
        ApiEndpoint savedApi = apiEndpointRepository.save(newApi);

        return ResponseEntity.ok(savedApi);
    }

    /**
     * Requirement: DELETE /api/endpoints/{id} (Delete an API)
     * Securely deletes an API and all its history, ensuring the user actually owns it first.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEndpoint(@PathVariable Long id, Authentication authentication) {
        // 1. Get the logged-in user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find the API
        ApiEndpoint api = apiEndpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API not found"));

        // 3. SECURITY CHECK: Does this API belong to the user trying to delete it?
        if (!api.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You do not have permission to delete this API.");
        }

        // 4. Delete it! (The cascade rule we added will handle the logs automatically)
        apiEndpointRepository.delete(api);

        return ResponseEntity.ok().build();
    }
}