import { describe, expect, it } from 'vitest';

import { currencySymbol, formatChange, formatMoney, formatMoneyCompact, formatUsdApprox, toUsd } from './money';

describe('money formatting', () => {
  it('formats whole units with the currency symbol', () => {
    expect(formatMoney(120000, 'USD')).toBe('$120,000');
    expect(formatMoney(70300, 'GBP')).toBe('£70,300');
  });

  it('groups rupees in lakhs and crores, the way Indian HR reads them', () => {
    expect(formatMoney(2450000, 'INR')).toBe('₹24,50,000');
  });

  it('shortens large figures for summaries', () => {
    expect(formatMoneyCompact(865605992, 'USD')).toBe('$865.6M');
  });

  it('marks every converted figure as approximate', () => {
    expect(formatUsdApprox(28571)).toBe('≈ $28,571');
  });

  it('shows a dash for missing values instead of "$0"', () => {
    expect(formatMoney(null, 'USD')).toBe('—');
    expect(formatUsdApprox(undefined)).toBe('—');
  });

  it('describes a salary change as a signed percentage', () => {
    expect(formatChange(100000, 104200)).toBe('+4.2%');
    expect(formatChange(100000, 95000)).toBe('−5.0%');
    expect(formatChange(100000, 100000)).toBe('±0.0%');
  });

  it('converts to whole US dollars at the reference rate', () => {
    expect(toUsd(2400000, 84)).toBe(28571);
  });

  it('knows the symbol for a currency', () => {
    expect(currencySymbol('EUR')).toBe('€');
  });
});
