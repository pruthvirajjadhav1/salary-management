import { Pipe, PipeTransform } from '@angular/core';

import { formatDate } from '../core/format/dates';
import { formatMoney, formatMoneyCompact, formatUsdApprox } from '../core/format/money';

/** `{{ salary | money: 'INR' }}` -> "₹24,50,000"; `{{ x | money: 'USD': true }}` -> "$865.6M". */
@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(amount: number | null | undefined, currency: string, compact = false): string {
    return compact ? formatMoneyCompact(amount, currency) : formatMoney(amount, currency);
  }
}

/** `{{ amountUsd | usd }}` -> "≈ $28,571". */
@Pipe({ name: 'usd' })
export class UsdPipe implements PipeTransform {
  transform(amount: number | null | undefined, compact = false): string {
    return formatUsdApprox(amount, compact);
  }
}

/** `{{ '2023-06-01' | day }}` -> "1 Jun 2023". */
@Pipe({ name: 'day' })
export class DayPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return formatDate(iso);
  }
}
