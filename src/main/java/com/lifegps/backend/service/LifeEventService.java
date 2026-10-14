package com.lifegps.backend.service;

import com.lifegps.backend.entity.LifeEvent;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.LifeEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LifeEventService {

    private final LifeEventRepository lifeEventRepository;

    public LifeEventService(LifeEventRepository lifeEventRepository) {
        this.lifeEventRepository = lifeEventRepository;
    }

    public List<LifeEvent> getUserEvents(User user) {
        return lifeEventRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public LifeEvent createEvent(
            String eventName,
            java.time.LocalDate targetDate,
            String status,
            String notes,
            User user) {

        validate(eventName, status);

        LifeEvent event = new LifeEvent();
        event.setEventName(eventName.trim());
        event.setTargetDate(targetDate);
        event.setStatus(status == null || status.isBlank()
                ? "Planned" : status.trim());
        event.setNotes(notes);
        event.setUser(user);

        return lifeEventRepository.save(event);
    }

    @Transactional
    public LifeEvent updateEvent(
            Long id,
            String eventName,
            java.time.LocalDate targetDate,
            String status,
            String notes,
            User user) {

        validate(eventName, status);

        LifeEvent event = lifeEventRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Life event not found or access denied."));

        event.setEventName(eventName.trim());
        event.setTargetDate(targetDate);
        event.setStatus(status);
        event.setNotes(notes);

        return lifeEventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id, User user) {
        LifeEvent event = lifeEventRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Life event not found or access denied."));

        lifeEventRepository.delete(event);
    }

    private void validate(String eventName, String status) {
        if (eventName == null || eventName.isBlank()) {
            throw new IllegalArgumentException(
                    "Event name is required.");
        }

        if (eventName.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Event name must be 150 characters or fewer.");
        }

        if (status != null && !status.isBlank()
                && !status.equals("Planned")
                && !status.equals("In Progress")
                && !status.equals("Completed")) {
            throw new IllegalArgumentException(
                    "Status must be Planned, In Progress, or Completed.");
        }
    }
}