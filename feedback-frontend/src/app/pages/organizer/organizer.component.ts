import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ApiService, QuestionDto } from '../../services/api.service';
import { AuthStoreService } from '../../services/auth-store.service';

@Component({
  selector: 'app-organizer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './organizer.component.html',
  styleUrls: ['./organizer.component.css'],
})
export class OrganizerComponent {
  username = '';
  password = '';
  loggedIn = false;

  feedbackStartLocal = '';
  feedbackEndLocal = '';
  questionsText = '';

  surveyId: string | null = null;
  questions: QuestionDto[] = [];
  qrUrl: string | null = null;
  participantUrl: string | null = null;

  loginError: string | null = null;
  createError: string | null = null;
  successMsg: string | null = null;
  qrError: string | null = null;

  constructor(
    private api: ApiService,
    private authStore: AuthStoreService,
    private router: Router
  ) {
    this.loggedIn = this.authStore.isLoggedIn();
  }

  /**
   * FIX IMPORTANT:
   * datetime-local nu are timezone.
   * toISOString() îl mută în UTC și poate da "Feedback not started".
   * Aici trimitem cu offset local: 2026-02-02T22:52:00+02:00
   */
  private toIsoWithOffset(dtLocal: string): string {
    const d = new Date(dtLocal);
    const pad = (n: number) => String(n).padStart(2, '0');

    const tz = -d.getTimezoneOffset(); // minute
    const sign = tz >= 0 ? '+' : '-';
    const hh = pad(Math.floor(Math.abs(tz) / 60));
    const mm = pad(Math.abs(tz) % 60);

    return (
      `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
      `T${pad(d.getHours())}:${pad(d.getMinutes())}:00${sign}${hh}:${mm}`
    );
  }

  login() {
    this.loginError = null;

    this.api.login(this.username, this.password).subscribe({
      next: (r) => {
        this.authStore.setToken(r.accessToken);
        this.loggedIn = true;
        this.successMsg = 'Login OK';
      },
      error: () => {
        this.loggedIn = false;
        this.loginError = 'Login failed';
      },
    });
  }

  logout() {
    this.authStore.clearToken();
    this.loggedIn = false;

    this.surveyId = null;
    this.questions = [];
    this.participantUrl = null;

    if (this.qrUrl) URL.revokeObjectURL(this.qrUrl);
    this.qrUrl = null;

    this.loginError = null;
    this.createError = null;
    this.successMsg = null;
    this.qrError = null;
  }

  createSurvey() {
    this.createError = null;
    this.qrError = null;

    const qs = this.questionsText
      .split('\n')
      .map((x) => x.trim())
      .filter(Boolean);

    this.api
      .createSurvey({
        // AICI e fix-ul: NU mai trimitem UTC cu toISOString()
        feedbackStart: this.toIsoWithOffset(this.feedbackStartLocal),
        feedbackEnd: this.toIsoWithOffset(this.feedbackEndLocal),
        questions: qs,
      })
      .subscribe({
        next: (r) => {
          this.surveyId = r.surveyId;
          this.successMsg = 'Survey creat';

          this.loadQuestions();
          this.loadQrAndUrl();
        },
        error: () => {
          this.createError = 'Create survey failed';
        },
      });
  }

  loadQuestions() {
    if (!this.surveyId) return;

    this.api.getQuestions(this.surveyId).subscribe({
      next: (data: any) => {
        this.questions = Array.isArray(data) ? data : data.value;
      },
    });
  }

  // ===== UNICUL FLOW PENTRU QR + URL =====
  loadQrAndUrl() {
    if (!this.surveyId) return;

    this.api.getQrToken(this.surveyId).subscribe({
      next: (r) => {
        this.participantUrl = r.url;

        this.api.getQrPng(this.surveyId!).subscribe({
          next: (blob) => {
            if (this.qrUrl) URL.revokeObjectURL(this.qrUrl);
            this.qrUrl = URL.createObjectURL(blob);
          },
          error: () => (this.qrError = 'QR failed'),
        });
      },
      error: () => (this.createError = 'Nu pot lua participant URL'),
    });
  }

  openParticipant() {
    if (this.participantUrl) window.open(this.participantUrl, '_blank');
  }

  openDashboard() {
    if (this.surveyId) this.router.navigate(['/dashboard', this.surveyId]);
  }
}
