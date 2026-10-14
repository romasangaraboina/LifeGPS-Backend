package com.lifegps.backend.controller;

import com.lifegps.backend.entity.LifeEvent;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.UserRepository;
import com.lifegps.backend.service.LifeEventService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/life-events")
public class LifeEventController {

    private final LifeEventService lifeEventService;
    private final UserRepository userRepository;

    public LifeEventController(
            LifeEventService lifeEventService,
            UserRepository userRepository) {
        this.lifeEventService = lifeEventService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new IllegalArgumentException(
                    "Please log in first.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Logged-in user not found."));
    }

    @GetMapping
    public ResponseEntity<?> getEvents(Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);

            List<EventResponse> events =
                    lifeEventService.getUserEvents(user)
                            .stream()
                            .map(EventResponse::new)
                            .toList();

            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody EventRequest request,
            Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);

            LifeEvent event = lifeEventService.createEvent(
                    request.getEventName(),
                    request.getTargetDate(),
                    request.getStatus(),
                    request.getNotes(),
                    user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new EventResponse(event));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequest request,
            Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);

            LifeEvent event = lifeEventService.updateEvent(
                    id,
                    request.getEventName(),
                    request.getTargetDate(),
                    request.getStatus(),
                    request.getNotes(),
                    user);

            return ResponseEntity.ok(new EventResponse(event));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = getLoggedInUser(authentication);
            lifeEventService.deleteEvent(id, user);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class EventRequest {
        private String eventName;
        private LocalDate targetDate;
        private String status;
        private String notes;

        public EventRequest() {
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public LocalDate getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(LocalDate targetDate) {
            this.targetDate = targetDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    public static class EventResponse {
        private Long id;
        private String eventName;
        private LocalDate targetDate;
        private String status;
        private String notes;
        private LocalDateTime createdAt;

        public EventResponse() {
        }

        public EventResponse(LifeEvent event) {
            this.id = event.getId();
            this.eventName = event.getEventName();
            this.targetDate = event.getTargetDate();
            this.status = event.getStatus();
            this.notes = event.getNotes();
            this.createdAt = event.getCreatedAt();
        }

        public Long getId() {
            return id;
        }

        public String getEventName() {
            return eventName;
        }

        public LocalDate getTargetDate() {
            return targetDate;
        }

        public String getStatus() {
            return status;
        }

        public String getNotes() {
            return notes;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}