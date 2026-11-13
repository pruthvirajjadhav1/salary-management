const dayFormat = new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });

/** ISO date "2023-06-01" -> "1 Jun 2023". Parsed as a calendar date, so time zones cannot shift it. */
export function formatDate(iso: string | null | undefined): string {
  if (!iso) {
    return '—';
  }
  const [year, month, day] = iso.slice(0, 10).split('-').map(Number);
  return dayFormat.format(new Date(Date.UTC(year, month - 1, day)));
}

/** Whole years and months between an ISO date and `today`, e.g. "3 yrs 4 mos". */
export function formatTenure(isoStart: string, today: Date = new Date()): string {
  const [year, month, day] = isoStart.split('-').map(Number);
  let months = (today.getFullYear() - year) * 12 + (today.getMonth() + 1 - month);
  if (today.getDate() < day) {
    months -= 1;
  }
  if (months < 0) {
    return 'Starts ' + formatDate(isoStart);
  }
  const years = Math.floor(months / 12);
  const rest = months % 12;
  const parts = [];
  if (years > 0) parts.push(`${years} ${years === 1 ? 'yr' : 'yrs'}`);
  if (rest > 0 || years === 0) parts.push(`${rest} ${rest === 1 ? 'mo' : 'mos'}`);
  return parts.join(' ');
}

/** Date -> ISO calendar date in local time ("2026-11-13"), for sending to the API. */
export function toIsoDate(date: Date | null | undefined): string | null {
  if (!date) {
    return null;
  }
  const pad = (n: number) => `${n}`.padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** ISO calendar date -> local Date at midnight, for date pickers. */
export function fromIsoDate(iso: string | null | undefined): Date | null {
  if (!iso) {
    return null;
  }
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}
