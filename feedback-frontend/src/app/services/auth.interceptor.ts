import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthStoreService } from './auth-store.service';

// pune aici exact backend-ul tău
const API_ORIGIN = 'http://localhost:8080';

function isPublicEndpoint(url: string): boolean {
  // permite atât cu /api în față cât și fără
  return (
    url.includes('/public/') ||
    url.includes('/auth/login') ||
    url.includes('/auth/register') ||
    url.includes('/health') ||
    url.includes('/error')
  );
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthStoreService);
  const token = auth.getToken();

  if (!req.url.startsWith(API_ORIGIN)) {
    return next(req);
  }

  if (!token || isPublicEndpoint(req.url)) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return next(authReq);
};
