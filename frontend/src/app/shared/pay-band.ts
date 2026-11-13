import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface BandStats {
  min: number;
  p25: number;
  median: number;
  p75: number;
  max: number;
}

/** Position of `value` within `[low, high]` as a percentage, clamped to the track. */
export function bandPosition(value: number, [low, high]: readonly [number, number]): number {
  if (high <= low) {
    return 50;
  }
  return Math.min(100, Math.max(0, ((value - low) / (high - low)) * 100));
}

/**
 * The pay band: a thin line from min to max, the middle 50% (P25–P75) shaded, a tick at the median and,
 * optionally, an amber marker for one person. Rows that share a `domain` can be compared at a glance.
 */
@Component({
  selector: 'app-pay-band',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="band" [class.is-large]="size() === 'large'" role="img" [attr.aria-label]="label()">
      <span class="range" [style.left.%]="pos().min" [style.width.%]="pos().max - pos().min"></span>
      <span class="iqr" [style.left.%]="pos().p25" [style.width.%]="pos().p75 - pos().p25"></span>
      <span class="median" [style.left.%]="pos().median"></span>
      @if (pos().marker !== null) {
        <span class="marker" [style.left.%]="pos().marker"></span>
      }
    </div>
  `,
  styles: `
    :host {
      display: block;
      min-width: 6rem;
    }
    .band {
      position: relative;
      height: 16px;
    }
    .band.is-large {
      height: 28px;
    }
    .range {
      position: absolute;
      top: 50%;
      height: 2px;
      margin-top: -1px;
      border-radius: 1px;
      background: var(--band-track);
    }
    .iqr {
      position: absolute;
      top: 3px;
      bottom: 3px;
      min-width: 2px;
      border-radius: 3px;
      background: var(--band-iqr);
    }
    .is-large .iqr {
      top: 6px;
      bottom: 6px;
    }
    .median {
      position: absolute;
      top: 1px;
      bottom: 1px;
      width: 2px;
      margin-left: -1px;
      border-radius: 1px;
      background: var(--band-median);
    }
    .marker {
      position: absolute;
      top: 50%;
      width: 12px;
      height: 12px;
      margin: -6px 0 0 -6px;
      border-radius: 50%;
      background: var(--amber);
      box-shadow:
        0 0 0 2px var(--surface),
        0 1px 3px rgb(31 28 36 / 0.35);
    }
    .is-large .marker {
      width: 16px;
      height: 16px;
      margin: -8px 0 0 -8px;
    }
  `,
})
export class PayBand {
  readonly stats = input.required<BandStats>();
  readonly domain = input.required<readonly [number, number]>();
  readonly marker = input<number | null>(null);
  readonly label = input('');
  readonly size = input<'compact' | 'large'>('compact');

  protected readonly pos = computed(() => {
    const s = this.stats();
    const d = this.domain();
    const marker = this.marker();
    return {
      min: bandPosition(s.min, d),
      p25: bandPosition(s.p25, d),
      median: bandPosition(s.median, d),
      p75: bandPosition(s.p75, d),
      max: bandPosition(s.max, d),
      marker: marker === null ? null : bandPosition(marker, d),
    };
  });
}

/** A domain that covers every band with a little air, so no band touches the column edge. */
export function sharedDomain(bands: readonly BandStats[], markers: readonly number[] = []): [number, number] {
  if (bands.length === 0) {
    return [0, 1];
  }
  const low = Math.min(...bands.map((b) => b.min), ...markers);
  const high = Math.max(...bands.map((b) => b.max), ...markers);
  const pad = (high - low) * 0.03 || high * 0.05 || 1;
  return [Math.max(0, low - pad), high + pad];
}
