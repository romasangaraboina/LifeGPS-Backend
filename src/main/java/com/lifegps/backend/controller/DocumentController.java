
package com.lifegps.backend.controller;

import com.lifegps.backend.entity.Document;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.UserRepository;
import com.lifegps.backend.service.DocumentService;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    public DocumentController(
            DocumentService documentService,
            UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Please log in.");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.UNAUTHORIZED,
                                "User not found."));
    }

    @PostMapping
    public ResponseEntity<Document> addDocument(
            @RequestBody Document document,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);
        return ResponseEntity.ok(
                documentService.addDocument(document, user));
    }

    @GetMapping
    public ResponseEntity<List<Document>> getDocuments(
            Authentication authentication) {

        User user = getLoggedInUser(authentication);
        return ResponseEntity.ok(
                documentService.getUserDocuments(user));
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            @RequestParam("category") String category,
            @RequestParam(value = "expiryDate", required = false)
                    String expiryDate,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);
        LocalDate parsedExpiryDate = null;

        if (expiryDate != null && !expiryDate.isBlank()) {
            try {
                parsedExpiryDate = LocalDate.parse(expiryDate);
            } catch (DateTimeParseException exception) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Expiry date must use YYYY-MM-DD format.");
            }
        }

        Document savedDocument = documentService.uploadDocument(
                file, documentName, category, parsedExpiryDate, user);

        return ResponseEntity.ok(savedDocument);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @RequestBody Document updatedDetails,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return ResponseEntity.ok(
                documentService.updateDocument(id, updatedDetails, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);
        documentService.deleteDocument(id, user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);
        Resource resource = documentService.getDocumentFile(id, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"LifeGPS-document\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}