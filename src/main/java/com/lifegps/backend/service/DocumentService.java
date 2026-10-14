
package com.lifegps.backend.service;

import com.lifegps.backend.entity.Document;
import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.DocumentRepository;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.lifegps.backend.entity.Reminder;
import com.lifegps.backend.repository.ReminderRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final Path uploadDirectory =
            Paths.get("uploads", "lifegps")
                    .toAbsolutePath()
                    .normalize();

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "png", "jpg", "jpeg");

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document addDocument(Document document, User user) {
        document.setUser(user);
        document.setUploadDate(LocalDate.now());

        if (document.getStatus() == null
                || document.getStatus().isBlank()) {
            document.setStatus("Active");
        }

        return documentRepository.save(document);
    }

    public List<Document> getUserDocuments(User user) {
        return documentRepository.findByUser(user);
    }

    public Document uploadDocument(
            MultipartFile file,
            String documentName,
            String category,
            LocalDate expiryDate,
            User user) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please select a file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must be 10 MB or smaller.");
        }

        validateDocumentDetails(documentName, category);

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose a PDF, PNG, JPG, or JPEG file.");
        }

        String extension = originalName
                .substring(originalName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, PNG, JPG, and JPEG files are allowed.");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(storedName).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file name.");
        }

        try {
            Files.createDirectories(uploadDirectory);
            file.transferTo(destination);

            Document document = new Document();
            document.setDocumentName(documentName.trim());
            document.setCategory(category.trim());
            document.setExpiryDate(expiryDate);
            document.setFilePath(storedName);
            document.setUploadDate(LocalDate.now());
            document.setStatus("Active");
            document.setUser(user);

            try {
                return documentRepository.save(document);
            } catch (RuntimeException exception) {
                Files.deleteIfExists(destination);
                throw exception;
            }

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save the uploaded file.");
        }
    }

    @Transactional
    public Document updateDocument(
            Long documentId,
            Document updatedDetails,
            User user) {

        Document existing = getOwnedDocument(documentId, user);

        validateDocumentDetails(
                updatedDetails.getDocumentName(),
                updatedDetails.getCategory());

        existing.setDocumentName(
                updatedDetails.getDocumentName().trim());

        existing.setCategory(
                updatedDetails.getCategory().trim());

        existing.setExpiryDate(updatedDetails.getExpiryDate());

        if (updatedDetails.getStatus() != null
                && !updatedDetails.getStatus().isBlank()) {
            existing.setStatus(updatedDetails.getStatus().trim());
        }

        // Keep the existing file, owner, ID and original upload date.
        return documentRepository.save(existing);
    }

    @Transactional
    public void deleteDocument(Long documentId, User user) {
        Document document = getOwnedDocument(documentId, user);

        String storedFileName = document.getFilePath();

        // Delete the database record only after confirming ownership.
        documentRepository.delete(document);

        // Remove the stored file, if one exists.
        if (storedFileName != null && !storedFileName.isBlank()) {
            Path filePath = uploadDirectory.resolve(storedFileName).normalize();

            if (filePath.startsWith(uploadDirectory)) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException exception) {
                    // The database record is deleted. Report that
                    // cleanup may need attention.
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Document record deleted, but its stored file "
                                    + "could not be removed.");
                }
            }
        }
    }

    public Resource getDocumentFile(Long documentId, User user) {
        Document document = getOwnedDocument(documentId, user);

        if (document.getFilePath() == null
                || document.getFilePath().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No file is attached to this document.");
        }

        Path filePath = uploadDirectory
                .resolve(document.getFilePath())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)
                || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Stored file not found.");
        }

        return new FileSystemResource(filePath);
    }

    private Document getOwnedDocument(Long documentId, User user) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document not found."));

        if (document.getUser() == null
                || !document.getUser().getId().equals(user.getId())) {
            // Do not reveal documents belonging to other users.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Document not found.");
        }

        return document;
    }

    private void validateDocumentDetails(
            String documentName,
            String category) {

        if (documentName == null || documentName.isBlank()
                || category == null || category.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document name and category are required.");
        }

        if (documentName.length() > 150 || category.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document name or category is too long.");
        }
    }
}