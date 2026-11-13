/**
 * Money formatting. Amounts are whole units. Indian rupees use lakh/crore grouping (₹24,50,000),
 * which is how HR in India reads salaries; everything else uses international grouping.
 */

const formatters = new Map<string, Intl.NumberFormat>();

function formatter(currency: string, compact: boolean): Intl.NumberFormat {
  const key = `${currency}|${compact}`;
  let format = formatters.get(key);
  if (!format) {
    format = new Intl.NumberFormat(currency === 'INR' ? 'en-IN' : 'en-US', {
      style: 'currency',
      currency,
      currencyDisplay: 'narrowSymbol',
      maximumFractionDigits: compact ? 1 : 0,
      minimumFractionDigits: 0,
      notation: compact ? 'compact' : 'standard',
    });
    formatters.set(key, format);
  }
  return format;
}

/** `formatMoney(2450000, 'INR')` -> "₹24,50,000". */
export function formatMoney(amount: number | null | undefined, currency: string): string {
  if (amount === null || amount === undefined) {
    return '—';
  }
  return formatter(currency, false).format(amount);
}

/** Short form for tight spaces: "$865.6M", "₹28.5L". */
export function formatMoneyCompact(amount: number | null | undefined, currency: string): string {
  if (amount === null || amount === undefined) {
    return '—';
  }
  return formatter(currency, true).format(amount);
}

/** Converted figures are never exact, so they always say so: "≈ $28,571". */
export function formatUsdApprox(amount: number | null | undefined, compact = false): string {
  if (amount === null || amount === undefined) {
    return '—';
  }
  return '≈ ' + (compact ? formatMoneyCompact(amount, 'USD') : formatMoney(amount, 'USD'));
}

/** Percentage change between two salaries, e.g. "+4.2%". */
export function formatChange(previous: number, next: number): string {
  if (previous <= 0) {
    return '';
  }
  const percent = ((next - previous) / previous) * 100;
  const sign = percent > 0 ? '+' : percent < 0 ? '−' : '±';
  return `${sign}${Math.abs(percent).toFixed(1)}%`;
}

export function toUsd(amount: number, unitsPerUsd: number): number {
  return Math.round(amount / unitsPerUsd);
}

export function currencySymbol(currency: string): string {
  return (
    formatter(currency, false)
      .formatToParts(0)
      .find((part) => part.type === 'currency')?.value ?? currency
  );
}
