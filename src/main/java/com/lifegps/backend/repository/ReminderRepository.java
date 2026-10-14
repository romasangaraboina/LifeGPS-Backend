
package com.lifegps.backend.repository;

import com.lifegps.backend.entity.Reminder;
import com.lifegps.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserOrderByReminderDateAsc(User user);

    java.util.Optional<Reminder> findByIdAndUser(Long id, User user);

    boolean existsByUserAndDocument_IdAndTitle(
            User user, Long documentId, String title);
}
