package com.ssatr.Project.controller;

import com.ssatr.Project.dto.PublicSessionResponse;
import com.ssatr.Project.dto.SubmitSurveyRequest;
import com.ssatr.Project.dto.SubmitSurveyResponse;
import com.ssatr.Project.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final SurveyService surveyService;

    public PublicController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitSurveyResponse> submit(
            @RequestParam("token") String token,
            @RequestBody @Valid SubmitSurveyRequest req
    ) {
        surveyService.submitSurvey(token, req);
        return ResponseEntity.ok(new SubmitSurveyResponse("Submission successful"));
    }

    @GetMapping("/session")
    public ResponseEntity<PublicSessionResponse> session(@RequestParam("token") String token) {
        return ResponseEntity.ok(surveyService.getPublicSession(token));
    }
}
