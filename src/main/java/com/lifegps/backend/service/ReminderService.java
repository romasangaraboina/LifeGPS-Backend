
package com.lifegps.backend.service;

import com.lifegps.backend.entity.Document;
import com.lifegps.backend.entity.Reminder;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.DocumentRepository;
import com.lifegps.backend.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final DocumentRepository documentRepository;

    public ReminderService(
            ReminderRepository reminderRepository,
            DocumentRepository documentRepository) {
        this.reminderRepository = reminderRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public List<Reminder> getReminders(User user) {
        generateExpiryReminders(user);
        return reminderRepository.findByUserOrderByReminderDateAsc(user);
    }

    private void generateExpiryReminders(User user) {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(30);

        List<Document> documents = documentRepository.findByUser(user);

        for (Document document : documents) {
            LocalDate expiryDate = document.getExpiryDate();

            if (expiryDate == null ||
                    expiryDate.isBefore(today) ||
                    expiryDate.isAfter(limit)) {
                continue;
            }

            String title = "Document Expiry";

            if (!reminderRepository.existsByUserAndDocument_IdAndTitle(
                    user, document.getId(), title)) {

                Reminder reminder = new Reminder();
                reminder.setTitle(title);
                reminder.setCategory("Documents");
                reminder.setMessage(
                        document.getDocumentName() +
                        " expires on " + expiryDate +
                        ". Please renew it before expiry.");
                reminder.setReminderDate(expiryDate);
                reminder.setStatus("Pending");
                reminder.setUser(user);
                reminder.setDocument(document);

                reminderRepository.save(reminder);
            }
        }
    }

    @Transactional
    public Reminder updateStatus(Long id, String status, User user) {
        if (!"Pending".equals(status) && !"Completed".equals(status)) {
            throw new IllegalArgumentException(
                    "Status must be Pending or Completed");
        }

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Reminder not found"));

        if (reminder.getUser() == null ||
                !reminder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Reminder not found");
        }

        reminder.setStatus(status);
        return reminderRepository.save(reminder);
    }

    @Transactional
    public Reminder createReminder(String title, String message, String category,
                                   LocalDate reminderDate, User user) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Reminder title is required");
        }
        if (title.trim().length() > 150) {
            throw new IllegalArgumentException("Title must be 150 characters or fewer");
        }
        if (reminderDate == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        Reminder reminder = new Reminder();
        reminder.setTitle(title.trim());
        reminder.setMessage(message == null ? null : message.trim());
        reminder.setCategory(category == null || category.isBlank() ? "Personal" : category.trim());
        reminder.setReminderDate(reminderDate);
        reminder.setStatus("Pending");
        reminder.setUser(user);
        return reminderRepository.save(reminder);
    }

    @Transactional
    public void deleteReminder(Long id, User user) {
        Reminder reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        reminderRepository.delete(reminder);
    }
}
