import { describe, expect, it } from 'vitest';

import { SalaryChange } from '../../core/api/models';
import { historyEntries } from './employee-profile';

const change = (id: number, annualSalary: number, currencyCode = 'USD'): SalaryChange => ({
  id,
  annualSalary,
  currencyCode,
  effectiveDate: '2025-04-01',
  reason: null,
  recordedAt: '2025-04-01T00:00:00Z',
});

describe('salary history timeline', () => {
  it('shows each change against the salary before it (newest first)', () => {
    const entries = historyEntries([change(3, 110000), change(2, 100000), change(1, 100000)]);

    expect(entries.map((e) => [e.change, e.direction])).toEqual([
      ['+10.0%', 'up'],
      ['±0.0%', 'flat'],
      ['', 'start'],
    ]);
  });

  it('does not compute a percentage across a currency change', () => {
    const [relocation] = historyEntries([change(2, 80000, 'GBP'), change(1, 100000, 'USD')]);

    expect(relocation.direction).toBe('currency');
    expect(relocation.change).toBe('USD → GBP');
  });
});
