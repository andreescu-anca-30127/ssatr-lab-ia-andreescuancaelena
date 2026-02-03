import { Injectable } from '@angular/core';
import { BehaviorSubject, map } from 'rxjs';

export type SessionStatus = 'DRAFT' | 'ACTIVE' | 'CLOSED';

export interface Question {
  id: string;
  text: string;
}

export interface FeedbackResponse {
  id: string;
  sessionToken: string;
  rating: number;     
  comment: string;
  anonymous: boolean;
  createdAt: number;  
  sentiment: 'positive' | 'neutral' | 'negative';
}

export interface FeedbackSession {
  id: string;
  token: string; // “QR token”
  title: string;
  status: SessionStatus;
  startAt: number; // timestamp
  endAt: number;   // timestamp
  questions: Question[];
  responses: FeedbackResponse[];
}

const LS_KEY = 'feedback_frontend_store_v1';

function uid(prefix = '') {
  return prefix + Math.random().toString(16).slice(2) + Date.now().toString(16);
}

function sentimentFrom(rating: number, comment: string): 'positive' | 'neutral' | 'negative' {
  // logică super simplă pt demo
  if (rating >= 4) return 'positive';
  if (rating <= 2) return 'negative';
  // rating 3 -> neutral, dar dacă textul are cuvinte “naspa” => negative
  const t = (comment || '').toLowerCase();
  const bad = ['prost', 'rau', 'urât', 'urat', 'naspa', 'slab', 'horrible', 'bad'];
  if (bad.some(w => t.includes(w))) return 'negative';
  return 'neutral';
}

function tokenizeWords(text: string): string[] {
  return (text || '')
    .toLowerCase()
    .replace(/[^a-z0-9ăâîșţșț\s]/gi, ' ')
    .split(/\s+/)
    .filter(w => w.length >= 3 && !['the','and','for','with','this','that','este','sunt','foarte','mult'].includes(w));
}

@Injectable({ providedIn: 'root' })
export class SessionStoreService {
  private sessions$ = new BehaviorSubject<FeedbackSession[]>(this.load());

  // observable public
  sessionsObs$ = this.sessions$.asObservable();

  getSessionsSnapshot() {
    return this.sessions$.value;
  }

  getSessionByToken(token: string) {
    return this.sessionsObs$.pipe(
      map(list => list.find(s => s.token === token) || null)
    );
  }

  createSession(title: string, startAt: number, endAt: number, questionsText: string[]) {
    const session: FeedbackSession = {
      id: uid('sess_'),
      token: uid('t_').slice(0, 10),
      title,
      status: 'DRAFT',
      startAt,
      endAt,
      questions: questionsText.map(t => ({ id: uid('q_'), text: t })),
      responses: []
    };
    const next = [session, ...this.sessions$.value];
    this.sessions$.next(next);
    this.save(next);
    return session;
  }

  setStatus(sessionId: string, status: SessionStatus) {
    const next = this.sessions$.value.map(s => s.id === sessionId ? { ...s, status } : s);
    this.sessions$.next(next);
    this.save(next);
  }

  submitResponse(token: string, payload: { rating: number; comment: string; anonymous: boolean; }) {
    const list = this.sessions$.value;
    const s = list.find(x => x.token === token);
    if (!s) throw new Error('Session not found');
    const now = Date.now();

    if (s.status !== 'ACTIVE') throw new Error('Session not active');
    if (now < s.startAt || now > s.endAt) throw new Error('Feedback window closed');

    const response: FeedbackResponse = {
      id: uid('r_'),
      sessionToken: token,
      rating: payload.rating,
      comment: payload.comment,
      anonymous: payload.anonymous,
      createdAt: now,
      sentiment: sentimentFrom(payload.rating, payload.comment)
    };

    const updated: FeedbackSession = { ...s, responses: [response, ...s.responses] };
    const next = list.map(x => x.id === s.id ? updated : x);

    this.sessions$.next(next);
    this.save(next);

    return response;
  }

  // --- analytics pentru dashboard ---
  getLiveStatsBySessionId(sessionId: string) {
    return this.sessionsObs$.pipe(
      map(list => {
        const s = list.find(x => x.id === sessionId);
        if (!s) return null;

        const total = s.responses.length;
        const pos = s.responses.filter(r => r.sentiment === 'positive').length;
        const neu = s.responses.filter(r => r.sentiment === 'neutral').length;
        const neg = s.responses.filter(r => r.sentiment === 'negative').length;

        const avgRating = total ? (s.responses.reduce((a, r) => a + r.rating, 0) / total) : 0;

        // response rate simulat: total / 100
        const expected = 100;
        const responseRatePercent = Math.min(100, Math.round((total / expected) * 100));

        // top words
        const freq = new Map<string, number>();
        for (const r of s.responses) {
          for (const w of tokenizeWords(r.comment)) {
            freq.set(w, (freq.get(w) || 0) + 1);
          }
        }
        const topWords = [...freq.entries()]
          .sort((a, b) => b[1] - a[1])
          .slice(0, 12)
          .map(([word, count]) => ({ word, count }));

        return {
          sessionId: s.id,
          title: s.title,
          total,
          positive: pos,
          neutral: neu,
          negative: neg,
          avgRating,
          responseRatePercent,
          topWords,
          startAt: s.startAt,
          endAt: s.endAt,
          status: s.status
        };
      })
    );
  }

  // --- persistence ---
  private save(data: FeedbackSession[]) {
    localStorage.setItem(LS_KEY, JSON.stringify(data));
  }

  private load(): FeedbackSession[] {
    const raw = localStorage.getItem(LS_KEY);
    if (!raw) return [];
    try {
      return JSON.parse(raw) as FeedbackSession[];
    } catch {
      return [];
    }
  }

  // helper pt demo: seed rapid
  seedDemoIfEmpty() {
    if (this.sessions$.value.length) return;

    const now = Date.now();
    const s = this.createSession(
      'Demo Event',
      now - 10 * 60 * 1000,
      now + 60 * 60 * 1000,
      ['Cum ți s-a părut evenimentul?', 'Ce ai îmbunătăți?']
    );
    this.setStatus(s.id, 'ACTIVE');
  }
}