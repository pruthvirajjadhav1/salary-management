import { describe, expect, it } from 'vitest';

import { formatDate, formatTenure, fromIsoDate, toIsoDate } from './dates';

describe('dates', () => {
  it('formats ISO calendar dates without time-zone drift', () => {
    expect(formatDate('2023-06-01')).toBe('1 Jun 2023');
    expect(formatDate(null)).toBe('—');
  });

  it('describes tenure in years and months', () => {
    const today = new Date(2026, 10, 13);
    expect(formatTenure('2020-08-01', today)).toBe('6 yrs 3 mos');
    expect(formatTenure('2025-11-13', today)).toBe('1 yr');
    expect(formatTenure('2026-10-20', today)).toBe('0 mos');
  });

  it('says when a future hire starts', () => {
    expect(formatTenure('2027-01-04', new Date(2026, 10, 13))).toBe('Starts 4 Jan 2027');
  });

  it('round-trips dates between pickers and the API', () => {
    expect(toIsoDate(fromIsoDate('2024-02-29'))).toBe('2024-02-29');
    expect(toIsoDate(null)).toBeNull();
  });
});
