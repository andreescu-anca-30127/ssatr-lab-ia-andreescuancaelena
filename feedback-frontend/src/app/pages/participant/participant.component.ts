import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { ApiService, PublicSessionResponse } from '../../services/api.service';

@Component({
  selector: 'app-participant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './participant.component.html',
  styleUrls: ['./participant.component.css'],
})
export class ParticipantComponent implements OnInit {
  token = '';
  session: PublicSessionResponse | null = null;

  anonymous = true;
  satisfaction = 5;
  answers: Record<string, string> = {};

  submitted = false;
  errorMsg: string | null = null;

  constructor(private route: ActivatedRoute, private api: ApiService) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.errorMsg = 'Missing token in URL. Open via QR.';
      return;
    }

    this.api.getPublicSession(this.token).subscribe({
      next: (s) => {
        this.session = s;
        this.errorMsg = null;
      },
      error: (e) => {
        console.error(e);
        this.session = null;
        this.errorMsg = 'Cannot load session (invalid/expired token).';
      },
    });
  }

  submit() {
    if (!this.session) return;

    const payload = {
      surveyId: this.session.surveyId,
      userId: this.anonymous ? null : null, // nu ai login participant acum
      satisfaction: this.satisfaction,
      answers: this.session.questions.map((q) => ({
        questionId: q.id,
        response: this.answers[q.id] ?? '',
      })),
    };

    this.api.submitPublic(this.token, payload).subscribe({
      next: () => {
        this.submitted = true;
        this.errorMsg = null;
      },
      error: (e) => {
        console.error(e);
        this.submitted = false;
        this.errorMsg = 'Submit failed (window closed / token used / invalid).';
      },
    });
  }
}
