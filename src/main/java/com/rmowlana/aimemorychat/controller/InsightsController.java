package com.rmowlana.aimemorychat.controller;

import com.rmowlana.aimemorychat.service.InsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    @GetMapping("/{assessmentId}")
    public ResponseEntity<Map<String, Object>> getInsights(@PathVariable String assessmentId) {
        Map<String, Object> insights = insightsService.generateInsights(assessmentId);
        return ResponseEntity.ok(insights);
    }

}
