
package com.lifegps.backend.controller;

import com.lifegps.backend.entity.Reminder;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.UserRepository;
import com.lifegps.backend.service.ReminderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;
    private final UserRepository userRepository;

    public ReminderController(
            ReminderService reminderService,
            UserRepository userRepository) {
        this.reminderService = reminderService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getReminders(Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);

            List<ReminderResponse> result =
                    reminderService.getReminders(user)
                            .stream()
                            .map(ReminderResponse::from)
                            .toList();

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request,
            Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);

            Reminder reminder = reminderService.updateStatus(
                    id, request.status(), user);

            return ResponseEntity.ok(ReminderResponse.from(reminder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateRequest request,
            Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);
            Reminder reminder = reminderService.createReminder(
                    request.title(), request.message(), request.category(),
                    request.reminderDate(), user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ReminderResponse.from(reminder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);
            reminderService.deleteReminder(id, user);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Please log in first");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    public record StatusRequest(String status) {}

    public record CreateRequest(
            String title,
            String message,
            String category,
            LocalDate reminderDate) {}

    public record ReminderResponse(
            Long id,
            String title,
            String message,
            String category,
            LocalDate reminderDate,
            String status,
            Long documentId,
            LocalDateTime createdAt) {

        public static ReminderResponse from(Reminder reminder) {
            return new ReminderResponse(
                    reminder.getId(),
                    reminder.getTitle(),
                    reminder.getMessage(),
                    reminder.getCategory(),
                    reminder.getReminderDate(),
                    reminder.getStatus(),
                    reminder.getDocument() == null
                            ? null : reminder.getDocument().getId(),
                    reminder.getCreatedAt());
        }
    }
}