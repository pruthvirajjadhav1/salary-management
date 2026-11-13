import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'overview' },
  {
    path: 'overview',
    title: 'Pay overview · ACME People & Pay',
    loadComponent: () => import('./features/overview/overview.page').then((m) => m.OverviewPage),
  },
  {
    path: 'overview/:countryCode',
    title: 'Country pay · ACME People & Pay',
    loadComponent: () => import('./features/country/country-pay.page').then((m) => m.CountryPayPage),
  },
  {
    path: 'people',
    title: 'People · ACME People & Pay',
    loadComponent: () => import('./features/people/people.page').then((m) => m.PeoplePage),
  },
  { path: '**', redirectTo: 'overview' },
];
