package com.lifegps.backend.controller;

import com.lifegps.backend.entity.User;
import com.lifegps.backend.repository.UserRepository;
import com.lifegps.backend.service.AssistantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final UserRepository userRepository;

    public AssistantController(AssistantService assistantService, UserRepository userRepository) {
        this.assistantService = assistantService;
        this.userRepository = userRepository;
    }

    public record AskRequest(String question) {}

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody AskRequest request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in.");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found."));
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please type a question.");
        }
        return ResponseEntity.ok(Map.of("answer", assistantService.answer(request.question(), user)));
    }
}
