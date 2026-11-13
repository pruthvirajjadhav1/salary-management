import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Button } from 'primeng/button';

import { EmployeeDetails, PayBand as PayBandPosition, SalaryChange } from '../../core/api/models';
import { formatTenure } from '../../core/format/dates';
import { formatChange } from '../../core/format/money';
import { BandLegend } from '../../shared/band-legend';
import { DayPipe, MoneyPipe, UsdPipe } from '../../shared/format.pipes';
import { InitialsAvatar } from '../../shared/initials-avatar';
import { PayBand, sharedDomain } from '../../shared/pay-band';

interface HistoryEntry extends SalaryChange {
  change: string;
  direction: 'up' | 'down' | 'flat' | 'start' | 'currency';
}

/** Newest first; each entry knows its change from the one before it (only when the currency is the same). */
export function historyEntries(history: readonly SalaryChange[]): HistoryEntry[] {
  return history.map((entry, index) => {
    const previous = history[index + 1];
    if (!previous) {
      return { ...entry, change: '', direction: 'start' };
    }
    if (previous.currencyCode !== entry.currencyCode) {
      return { ...entry, change: `${previous.currencyCode} → ${entry.currencyCode}`, direction: 'currency' };
    }
    const delta = entry.annualSalary - previous.annualSalary;
    return {
      ...entry,
      change: formatChange(previous.annualSalary, entry.annualSalary),
      direction: delta > 0 ? 'up' : delta < 0 ? 'down' : 'flat',
    };
  });
}

const BAND_COPY: Record<PayBandPosition, string> = {
  BELOW: 'Below peer range',
  WITHIN: 'Within peer range',
  ABOVE: 'Above peer range',
};

@Component({
  selector: 'app-employee-profile',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Button, InitialsAvatar, PayBand, BandLegend, MoneyPipe, UsdPipe, DayPipe],
  templateUrl: './employee-profile.html',
  styleUrl: './employee-profile.scss',
})
export class EmployeeProfile {
  readonly employee = input.required<EmployeeDetails>();
  readonly edit = output<void>();
  readonly remove = output<void>();

  protected readonly history = computed(() => historyEntries(this.employee().salaryHistory));
  protected readonly tenure = computed(() => formatTenure(this.employee().hireDate));
  protected readonly peers = computed(() => this.employee().peerComparison);
  protected readonly bandCopy = computed(() => {
    const peers = this.peers();
    return peers ? BAND_COPY[peers.position.band] : '';
  });
  protected readonly peerDomain = computed(() => {
    const peers = this.peers();
    return peers ? sharedDomain([peers.peers], [this.employee().annualSalary]) : ([0, 1] as [number, number]);
  });
  protected readonly peerLabel = computed(() => {
    const e = this.employee();
    const count = this.peers()?.peers.headcount ?? 0;
    return count === 1
      ? `${e.fullName} is the only ${e.jobTitle} in ${e.countryName}.`
      : `Compared with all ${count} ${e.jobTitle}s in ${e.countryName}, including ${firstName(e.fullName)}.`;
  });
}

function firstName(fullName: string): string {
  return fullName.split(' ')[0];
}
