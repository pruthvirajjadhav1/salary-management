import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Toast } from 'primeng/toast';
import { map } from 'rxjs';

import { ReferenceApi } from './core/api/insights-api';
import { formatDate } from './core/format/dates';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Toast, ConfirmDialog],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly reference = inject(ReferenceApi);

  protected readonly ratesAsOf = toSignal(
    this.reference.data().pipe(map((data) => formatDate(data.countries[0]?.rateAsOf))),
  );
}
