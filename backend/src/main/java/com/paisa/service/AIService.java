package com.paisa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paisa.dto.AIRecommendationDto;
import com.paisa.dto.TransactionDto;
import com.paisa.entity.Transaction;
import com.paisa.repository.TransactionRepository;
import com.paisa.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {
    
    @Value("${gemini.api-key}")
    private String geminiApiKey;
    
    @Value("${gemini.base-url}")
    private String geminiBaseUrl;
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public AIService(TransactionRepository transactionRepository, 
                    UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }
    
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }
    
    private String getUserIdFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
    
    public AIRecommendationDto getRecommendations(String email) {
        String userId = getUserIdFromEmail(email);
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetween(userId, threeMonthsAgo, LocalDateTime.now());
        
        if (transactions.isEmpty()) {
            return getDefaultRecommendations();
        }
        
        String prompt = buildPrompt(transactions);
        String response = callGeminiAPI(prompt);
        
        return parseAIResponse(response);
    }
    
    private String buildPrompt(List<Transaction> transactions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following financial transactions and provide personalized recommendations. ");
        prompt.append("Return ONLY valid JSON in this exact format: {\"recommendations\": [\"rec1\", \"rec2\", ...], \"summary\": \"brief summary\"}\n\n");
        prompt.append("Transactions:\n");
        
        Map<String, BigDecimal> expensesByCategory = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
        
        prompt.append("Expense Categories:\n");
        expensesByCategory.forEach((category, amount) -> 
            prompt.append(String.format("- %s: ₹%.2f\n", category, amount))
        );
        
        BigDecimal totalExpense = expensesByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        prompt.append(String.format("\nTotal Income: ₹%.2f\n", totalIncome));
        prompt.append(String.format("Total Expense: ₹%.2f\n", totalExpense));
        
        prompt.append("\nProvide 3-5 actionable financial recommendations and a brief summary. ");
        prompt.append("Focus on savings, budgeting, and expense optimization. ");
        prompt.append("Return ONLY valid JSON, no markdown, no code blocks.");
        
        return prompt.toString();
    }
    
    private String callGeminiAPI(String prompt) {
        try {
            if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("your-gemini-api-key")) {
                throw new RuntimeException("GEMINI_API_KEY is not set or invalid");
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);
            
            contents.put("parts", parts);
            requestBody.put("contents", List.of(contents));
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);
            
            String url = String.format("/v1beta/models/gemini-pro:generateContent?key=%s", geminiApiKey);
            
            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .map(body -> new RuntimeException("Gemini API error: " + body)))
                    .bodyToMono(String.class)
                    .block();
            
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Empty response from Gemini API");
            }
            
            return sanitizeAndExtractJSON(response);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get AI recommendations: " + e.getMessage(), e);
        }
    }
    
    private String sanitizeAndExtractJSON(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return extractJSONFromText(text);
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return null;
    }
    
    private String extractJSONFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        text = text.trim();
        
        int startIdx = text.indexOf("{");
        int endIdx = text.lastIndexOf("}");
        
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return text.substring(startIdx, endIdx + 1);
        }
        
        return null;
    }
    
    private AIRecommendationDto parseAIResponse(String response) {
        if (response == null) {
            return getDefaultRecommendations();
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            List<String> recommendations = new ArrayList<>();
            
            JsonNode recsNode = jsonNode.path("recommendations");
            if (recsNode.isArray()) {
                for (JsonNode rec : recsNode) {
                    recommendations.add(rec.asText());
                }
            }
            
            String summary = jsonNode.path("summary").asText("No summary available");
            
            if (recommendations.isEmpty()) {
                return getDefaultRecommendations();
            }
            
            return new AIRecommendationDto(recommendations, summary);
        } catch (Exception e) {
            return getDefaultRecommendations();
        }
    }
    
    public AIRecommendationDto getDefaultRecommendations() {
        List<String> defaultRecs = List.of(
            "Track your expenses regularly to identify spending patterns",
            "Set up a monthly budget and stick to it",
            "Review your subscriptions and cancel unused services",
            "Build an emergency fund covering 3-6 months of expenses"
        );
        return new AIRecommendationDto(defaultRecs, "Start tracking your finances to get personalized recommendations");
    }
}

