import { Injectable } from '@angular/core';

const KEY = 'access_token';

@Injectable({ providedIn: 'root' })
export class AuthStoreService {
  private memoryToken: string | null = null;

  setToken(token: string) {
    this.memoryToken = token;
    if (this.canUseStorage()) localStorage.setItem(KEY, token);
  }

  getToken(): string | null {
    if (this.memoryToken) return this.memoryToken;
    if (this.canUseStorage()) return localStorage.getItem(KEY);
    return null;
  }

  clearToken() {
    this.memoryToken = null;
    if (this.canUseStorage()) localStorage.removeItem(KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private canUseStorage(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }
}
