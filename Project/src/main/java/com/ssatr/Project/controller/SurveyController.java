package com.ssatr.Project.controller;

import com.ssatr.Project.dto.CreateSurveyRequest;
import com.ssatr.Project.dto.CreateSurveyResponse;
import com.ssatr.Project.dto.QrTokenResponse;
import com.ssatr.Project.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ssatr.Project.dto.SurveyListItem;
import java.util.List;

@RestController
@RequestMapping("/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    public ResponseEntity<CreateSurveyResponse> create(@RequestBody @Valid CreateSurveyRequest req) {
        String surveyId = surveyService.createSurvey(req);
        return ResponseEntity.ok(new CreateSurveyResponse(surveyId));
    }

    @GetMapping("/{surveyId}/qr")
    public ResponseEntity<byte[]> qr(@PathVariable String surveyId) {
        byte[] png = surveyService.generateQrPng(surveyId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .body(png);
    }

    @GetMapping("/{surveyId}/responses")
    public ResponseEntity<?> responses(@PathVariable String surveyId) {
        return ResponseEntity.ok(surveyService.getResponses(surveyId));
    }

    @GetMapping("/{surveyId}/questions")
    public ResponseEntity<?> questions(@PathVariable String surveyId) {
        return ResponseEntity.ok(surveyService.getQuestions(surveyId));
    }

    @GetMapping("/{surveyId}/qr-token")
    public ResponseEntity<QrTokenResponse> qrToken(@PathVariable String surveyId) {
        return ResponseEntity.ok(surveyService.generateQrTokenOnly(surveyId));
    }

    @GetMapping
    public ResponseEntity<List<SurveyListItem>> mySurveys() {
        return ResponseEntity.ok(surveyService.getMySurveys());
    }
}
