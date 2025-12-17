package com.paisa.controller;

import com.paisa.dto.AIRecommendationDto;
import com.paisa.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AIController {
    
    private final AIService aiService;
    
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }
    
    @PostMapping("/recommendations")
    public ResponseEntity<AIRecommendationDto> getRecommendations(Authentication authentication) {
        String email = authentication.getName();
        try {
            AIRecommendationDto recommendations = aiService.getRecommendations(email);
            return ResponseEntity.ok(recommendations);
        } catch (RuntimeException e) {
            // Log the error for debugging
            System.err.println("AI Service Error: " + e.getMessage());
            e.printStackTrace();
            // Return default recommendations instead of error
            return ResponseEntity.ok(aiService.getDefaultRecommendations());
        } catch (Exception e) {
            System.err.println("Unexpected AI Service Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(aiService.getDefaultRecommendations());
        }
    }
}

