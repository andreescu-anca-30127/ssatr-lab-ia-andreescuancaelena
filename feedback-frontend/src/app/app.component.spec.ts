import { Routes } from '@angular/router';
import { OrganizerComponent } from './pages/organizer/organizer.component';
import { ParticipantComponent } from './pages/participant/participant.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';

export const routes: Routes = [
  { path: '', redirectTo: 'organizer', pathMatch: 'full' },

  { path: 'organizer', component: OrganizerComponent },

  { path: 'participant', component: ParticipantComponent },

  { path: 'dashboard', component: DashboardComponent },
  { path: 'dashboard/:surveyId', component: DashboardComponent },

  { path: '**', redirectTo: 'organizer' },
];
