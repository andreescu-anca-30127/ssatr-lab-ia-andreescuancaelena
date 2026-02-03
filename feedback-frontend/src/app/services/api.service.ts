import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse {
  accessToken: string;
}

export interface CreateSurveyRequest {
  feedbackStart: string;
  feedbackEnd: string;
  questions: string[];
}

export interface CreateSurveyResponse {
  surveyId: string;
}

export interface QuestionDto {
  id: string;
  text: string;
  surveyId: string;
}

export interface QrTokenResponse {
  token: string;
  url: string;
}

//NEW: list surveys for organizer
export interface SurveyListItem {
  id: string;
  feedbackStart: string | null;
  feedbackEnd: string | null;
}

//PUBLIC SESSION (participant)
export interface PublicSessionResponse {
  surveyId: string;
  feedbackStart: string | null;
  feedbackEnd: string | null;
  questions: Array<{ id: string; text: string }>;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // AUTH
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, {
      username,
      password,
    });
  }

  // ORGANIZER 
  createSurvey(req: CreateSurveyRequest): Observable<CreateSurveyResponse> {
    return this.http.post<CreateSurveyResponse>(`${this.baseUrl}/surveys`, req);
  }

  // NEW: all my surveys
  getMySurveys(): Observable<SurveyListItem[]> {
    return this.http.get<SurveyListItem[]>(`${this.baseUrl}/surveys`);
  }

  getQuestions(surveyId: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/surveys/${encodeURIComponent(surveyId)}/questions`
    );
  }

  getQrPng(surveyId: string): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/surveys/${encodeURIComponent(surveyId)}/qr`,
      { responseType: 'blob' }
    );
  }

  getResponses(surveyId: string): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/surveys/${encodeURIComponent(surveyId)}/responses`
    );
  }

  getQrToken(surveyId: string): Observable<QrTokenResponse> {
    return this.http.get<QrTokenResponse>(
      `${this.baseUrl}/surveys/${encodeURIComponent(surveyId)}/qr-token`
    );
  }

  // PARTICIPANT (PUBLIC)
  getPublicSession(qrToken: string): Observable<PublicSessionResponse> {
    return this.http.get<PublicSessionResponse>(
      `${this.baseUrl}/public/session?token=${encodeURIComponent(qrToken)}`
    );
  }

  submitPublic(qrToken: string, payload: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/public/submit?token=${encodeURIComponent(qrToken)}`,
      payload
    );
  }
}
