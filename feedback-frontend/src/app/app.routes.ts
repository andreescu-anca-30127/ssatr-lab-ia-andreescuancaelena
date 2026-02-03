import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'organizer', pathMatch: 'full' },

  {
    path: 'organizer',
    loadComponent: () =>
      import('./pages/organizer/organizer.component').then(
        (m) => m.OrganizerComponent
      ),
  },
  {
    path: 'participant',
    loadComponent: () =>
      import('./pages/participant/participant.component').then(
        (m) => m.ParticipantComponent
      ),
  },

  // dashboard routes
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then(
        (m) => m.DashboardComponent
      ),
  },
  {
    path: 'dashboard/:surveyId',
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then(
        (m) => m.DashboardComponent
      ),
  },

  { path: '**', redirectTo: 'organizer' },
];
