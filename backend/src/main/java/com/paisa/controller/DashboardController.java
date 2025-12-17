package com.paisa.controller;

import com.paisa.dto.DashboardSummaryDto;
import com.paisa.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(Authentication authentication) {
        String userId = authentication.getName();
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }
}

