import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiService, SurveyListItem } from '../../services/api.service';

type Sentiment = { positive: number; neutral: number; negative: number };

type DashboardAggregate = {
  surveyId: string;
  totalResponses: number;
  totalForms: number;
  averageSatisfaction: number;
  sentiment: Sentiment;
  topWords: string[];
};

const STOP_WORDS = new Set([
  'si','sau','dar','ca','ce','cum','care','este','sunt','foarte','mult','mai','din','pe','la','in','cu',
  'the','and','for','with','this','that','you','are','was','were','have','has','had','too','very'
]);

function tokenize(text: string): string[] {
  return (text || '')
    .toLowerCase()
    .replace(/[^a-z0-9ăâîșşțţ\s]/gi, ' ')
    .split(/\s+/)
    .map(w => w.trim())
    .filter(w => w.length >= 3 && !STOP_WORDS.has(w));
}

function computeSentimentFromText(s: string): 'positive' | 'neutral' | 'negative' {
  const t = (s || '').toLowerCase();
  const bad = ['prost','rau','urât','urat','naspa','slab','bad','horrible','awful'];
  const good = ['bun','super','excelent','fain','minunat','perfect','good','great','awesome'];
  if (bad.some(w => t.includes(w))) return 'negative';
  if (good.some(w => t.includes(w))) return 'positive';
  return 'neutral';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit {
  surveys: SurveyListItem[] = [];
  selectedSurveyId = '';

  aggregate: DashboardAggregate | null = null;
  loading = false;
  errorMsg: string | null = null;

  constructor(private route: ActivatedRoute, private api: ApiService) {}

  ngOnInit(): void {
    const fromRoute = this.route.snapshot.paramMap.get('surveyId');
    if (fromRoute) this.selectedSurveyId = fromRoute;

    this.loadAllSurveys();
  }

  loadAllSurveys() {
    this.errorMsg = null;

    this.api.getMySurveys().subscribe({
      next: (list) => {
        this.surveys = list ?? [];

        if (!this.selectedSurveyId && this.surveys.length) {
          this.selectedSurveyId = this.surveys[0].id;
        }

        if (this.selectedSurveyId) this.refresh();
      },
      error: (e) => {
        console.error(e);
        this.surveys = [];
        this.errorMsg = 'Nu pot încărca lista de surveys (verifică login/token).';
      }
    });
  }

  onSelectSurvey(id: string) {
    this.selectedSurveyId = id;
    this.refresh();
  }

  refresh() {
    if (!this.selectedSurveyId) return;

    this.loading = true;
    this.errorMsg = null;
    this.aggregate = null;

    this.api.getResponses(this.selectedSurveyId).subscribe({
      next: (data: any) => {
        const responses: any[] = Array.isArray(data?.responses) ? data.responses : [];
        const forms: any[] = Array.isArray(data?.forms) ? data.forms : [];

        const sats: number[] = forms
          .map(f => f?.satisfaction ?? null)
          .filter((x: any) => typeof x === 'number');

        const avg = sats.length
          ? Math.round((sats.reduce((a, b) => a + b, 0) / sats.length) * 100) / 100
          : 0;

        const texts: string[] = responses
          .map(r => r?.response ?? '')
          .filter((x: any) => typeof x === 'string' && x.trim().length);

        const freq = new Map<string, number>();
        for (const txt of texts) {
          for (const w of tokenize(txt)) {
            freq.set(w, (freq.get(w) || 0) + 1);
          }
        }

        const topWords = [...freq.entries()]
          .sort((a, b) => b[1] - a[1])
          .slice(0, 25)
          .map(([w]) => w);

        const sentiment: Sentiment = { positive: 0, neutral: 0, negative: 0 };

        if (forms.length) {
          for (const f of forms) {
            const rating = f?.satisfaction;
            if (typeof rating === 'number') {
              if (rating >= 4) sentiment.positive++;
              else if (rating <= 2) sentiment.negative++;
              else sentiment.neutral++;
            }
          }
        } else {
          for (const r of responses) {
            const s = computeSentimentFromText(r?.response ?? '');
            sentiment[s]++;
          }
        }

        this.aggregate = {
          surveyId: this.selectedSurveyId,
          totalResponses: responses.length,
          totalForms: forms.length,
          averageSatisfaction: avg,
          sentiment,
          topWords,
        };

        this.loading = false;
        this.errorMsg = null;
      },
      error: (e) => {
        console.error(e);
        this.loading = false;
        this.aggregate = null;
        this.errorMsg = 'Nu pot încărca dashboard-ul (verifică token / access / backend).';
      },
    });
  }
}
