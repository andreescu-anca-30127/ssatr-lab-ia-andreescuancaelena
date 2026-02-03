package com.ssatr.Project.service;

import com.ssatr.Project.dto.CreateSurveyRequest;
import com.ssatr.Project.dto.PublicSessionResponse;
import com.ssatr.Project.dto.QrTokenResponse;
import com.ssatr.Project.dto.SubmitSurveyRequest;
import com.ssatr.Project.entity.Form;
import com.ssatr.Project.entity.QrToken;
import com.ssatr.Project.entity.Question;
import com.ssatr.Project.entity.Response;
import com.ssatr.Project.entity.Survey;
import com.ssatr.Project.qr.QrCodeService;
import com.ssatr.Project.repository.FormRepository;
import com.ssatr.Project.repository.QrTokenRepository;
import com.ssatr.Project.repository.QuestionRepository;
import com.ssatr.Project.repository.ResponseRepository;
import com.ssatr.Project.repository.SurveyRepository;
import com.ssatr.Project.repository.UserRepository;
import com.ssatr.Project.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ssatr.Project.dto.SurveyListItem;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ResponseRepository responseRepository;
    private final FormRepository formRepository;
    private final UserRepository userRepository;
    private final QrTokenRepository qrTokenRepository;

    private final JwtService jwtService;
    private final QrCodeService qrCodeService;

    private final long qrTokenMinutes;
    private final String publicSubmitUrl;

    public SurveyService(
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            ResponseRepository responseRepository,
            FormRepository formRepository,
            UserRepository userRepository,
            QrTokenRepository qrTokenRepository,
            JwtService jwtService,
            QrCodeService qrCodeService,
            @Value("${app.jwt.qrTokenMinutes}") long qrTokenMinutes,
            @Value("${app.publicSubmitUrl}") String publicSubmitUrl
    ) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.responseRepository = responseRepository;
        this.formRepository = formRepository;
        this.userRepository = userRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.jwtService = jwtService;
        this.qrCodeService = qrCodeService;
        this.qrTokenMinutes = qrTokenMinutes;
        this.publicSubmitUrl = publicSubmitUrl;
    }

    // CREATE SURVEY
    public String createSurvey(CreateSurveyRequest req) {
        if (req.feedbackEnd().isBefore(req.feedbackStart())) {
            throw new RuntimeException("feedbackEnd must be after feedbackStart");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        Survey survey = new Survey();
        survey.setUserId(organizer.getId());
        survey.setFeedbackStart(req.feedbackStart());
        survey.setFeedbackEnd(req.feedbackEnd());
        surveyRepository.save(survey);

        for (String text : req.questions()) {
            Question q = new Question();
            q.setText(text);
            q.setSurveyId(survey.getId());
            questionRepository.save(q);
        }

        return survey.getId();
    }

    // QR HELPERS
    private Survey getSurveyOrThrow(String surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
    }

    private void assertOrganizerOwnsSurvey(Survey survey) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!survey.getUserId().equals(organizer.getId())) {
            throw new RuntimeException("Forbidden");
        }
    }

    private QrToken getOrCreateValidQrToken(String surveyId) {
        OffsetDateTime now = OffsetDateTime.now();

        Optional<QrToken> existing =
                qrTokenRepository.findTopBySurveyIdAndUsedAtIsNullAndExpiresAtAfterOrderByExpiresAtDesc(
                        surveyId, now);

        if (existing.isPresent()) {
            return existing.get();
        }

        String jwt = jwtService.generateQrToken(surveyId, qrTokenMinutes);

        QrToken t = new QrToken();
        t.setToken(jwt);
        t.setSurveyId(surveyId);
        t.setExpiresAt(now.plusMinutes(qrTokenMinutes));

        return qrTokenRepository.save(t);
    }

    // QR PNG

    public byte[] generateQrPng(String surveyId) {
        QrTokenResponse r = generateQrTokenOnly(surveyId);
        return qrCodeService.generatePng(r.url(), 300);
    }

    // QR TOKEN ONLY
    public QrTokenResponse generateQrTokenOnly(String surveyId) {
        Survey survey = getSurveyOrThrow(surveyId);
        assertOrganizerOwnsSurvey(survey);

        QrToken tokenRow = getOrCreateValidQrToken(surveyId);
        String url = publicSubmitUrl + tokenRow.getToken();

        return new QrTokenResponse(tokenRow.getToken(), url);
    }


    // PUBLIC SESSION
    public PublicSessionResponse getPublicSession(String token) {
        Claims claims = jwtService.parse(token).getBody();

        if (!"QR".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid token type");
        }

        String surveyId = claims.get("surveyId", String.class);

        QrToken tokenRow = qrTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not recognized"));

        if (OffsetDateTime.now().isAfter(tokenRow.getExpiresAt())) {
            throw new RuntimeException("Token expired");
        }

        Survey survey = getSurveyOrThrow(surveyId);

        List<Question> questions = questionRepository.findAllBySurveyId(surveyId);

        var qDtos = questions.stream()
                .map(q -> new PublicSessionResponse.QuestionDto(q.getId(), q.getText()))
                .toList();

        return new PublicSessionResponse(
                surveyId,
                survey.getFeedbackStart(),
                survey.getFeedbackEnd(),
                qDtos
        );
    }


    // SUBMIT
    public void submitSurvey(String token, SubmitSurveyRequest req) {
        Claims claims = jwtService.parse(token).getBody();

        if (!"QR".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid token type");
        }

        String tokenSurveyId = claims.get("surveyId", String.class);
        if (!req.surveyId().equals(tokenSurveyId)) {
            throw new RuntimeException("Token survey mismatch");
        }

        QrToken tokenRow = qrTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not recognized"));

        if (tokenRow.isUsed()) throw new RuntimeException("Token already used");
        if (OffsetDateTime.now().isAfter(tokenRow.getExpiresAt())) throw new RuntimeException("Token expired");

        Survey survey = getSurveyOrThrow(req.surveyId());

        OffsetDateTime now = OffsetDateTime.now();
        if (survey.getFeedbackStart() != null && now.isBefore(survey.getFeedbackStart()))
            throw new RuntimeException("Feedback not started");
        if (survey.getFeedbackEnd() != null && now.isAfter(survey.getFeedbackEnd()))
            throw new RuntimeException("Feedback ended");

        if (req.satisfaction() != null) {
            Form f = new Form();
            f.setSurvey(req.surveyId());
            f.setUser(req.userId());
            f.setSatisfaction(req.satisfaction());
            formRepository.save(f);
        }

        for (var a : req.answers()) {
            Response r = new Response();
            r.setQuestionId(a.questionId());
            r.setUserId(req.userId());
            r.setResponse(a.response());
            responseRepository.save(r);
        }

        tokenRow.setUsedAt(OffsetDateTime.now());
        qrTokenRepository.save(tokenRow);
    }

    // ORGANIZER RESPONSES
    public Map<String, Object> getResponses(String surveyId) {
        Survey survey = getSurveyOrThrow(surveyId);
        assertOrganizerOwnsSurvey(survey);

        List<Question> questions = questionRepository.findAllBySurveyId(surveyId);
        List<String> qIds = questions.stream().map(Question::getId).toList();

        var responses = responseRepository.findAllByQuestionIdIn(qIds);
        var forms = formRepository.findAllBySurvey(surveyId);

        return Map.of(
                "surveyId", surveyId,
                "questions", questions,
                "responses", responses,
                "forms", forms,
                "responseCount", responses.size(),
                "formCount", forms.size()
        );
    }

    // QUESTIONS
    public List<Question> getQuestions(String surveyId) {
        return questionRepository.findAllBySurveyId(surveyId);
    }
    public List<SurveyListItem> getMySurveys() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        return surveyRepository.findAllByUserIdOrderByFeedbackStartDesc(organizer.getId())
                .stream()
                .map(s -> new SurveyListItem(s.getId(), s.getFeedbackStart(), s.getFeedbackEnd()))
                .toList();
    }
}
