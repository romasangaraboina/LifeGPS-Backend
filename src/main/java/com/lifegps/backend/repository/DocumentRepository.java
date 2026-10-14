package com.lifegps.backend.repository;

import com.lifegps.backend.entity.Document;
import com.lifegps.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUser(User user);
}