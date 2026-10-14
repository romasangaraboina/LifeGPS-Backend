package com.lifegps.backend.repository;

import com.lifegps.backend.entity.LifeEvent;
import com.lifegps.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LifeEventRepository
        extends JpaRepository<LifeEvent, Long> {

    List<LifeEvent> findByUserOrderByCreatedAtDesc(User user);

    Optional<LifeEvent> findByIdAndUser(Long id, User user);
}