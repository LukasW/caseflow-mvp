import { Routes } from '@angular/router';
import { homeAccessGuard, roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [homeAccessGuard],
    loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'faelle/erfassen',
    canActivate: [roleGuard('CASE_MANAGER')],
    loadComponent: () =>
      import('./features/case-capture/case-capture.component').then((m) => m.CaseCaptureComponent),
  },
  {
    path: 'no-access',
    loadComponent: () =>
      import('./features/no-access/no-access.component').then((m) => m.NoAccessComponent),
  },
  { path: '**', redirectTo: '' },
];
