import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Explains the pay band once per panel instead of on every row. */
@Component({
  selector: 'app-band-legend',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="item"><span class="swatch range"></span>Min–max</span>
    <span class="item"><span class="swatch iqr"></span>Middle 50%</span>
    <span class="item"><span class="swatch median"></span>Median</span>
    @if (withMarker()) {
      <span class="item"><span class="swatch marker"></span>{{ markerLabel() }}</span>
    }
  `,
  styles: `
    :host {
      display: inline-flex;
      flex-wrap: wrap;
      gap: 0.25rem 0.875rem;
      font-size: 0.75rem;
      color: var(--ink-3);
    }
    .item {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
    }
    .swatch {
      display: inline-block;
    }
    .range {
      width: 14px;
      height: 2px;
      background: var(--band-track);
    }
    .iqr {
      width: 14px;
      height: 9px;
      border-radius: 2px;
      background: var(--band-iqr);
    }
    .median {
      width: 2px;
      height: 11px;
      background: var(--band-median);
    }
    .marker {
      width: 9px;
      height: 9px;
      border-radius: 50%;
      background: var(--amber);
    }
  `,
})
export class BandLegend {
  readonly withMarker = input(false);
  readonly markerLabel = input('This person');
}
