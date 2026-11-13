import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { PayBand, bandPosition, sharedDomain } from './pay-band';

describe('pay band', () => {
  it('places a value proportionally within the domain', () => {
    expect(bandPosition(150, [100, 200])).toBe(50);
  });

  it('clamps values outside the domain to the track ends', () => {
    expect(bandPosition(50, [100, 200])).toBe(0);
    expect(bandPosition(500, [100, 200])).toBe(100);
  });

  it('centres everything when the domain has no width', () => {
    expect(bandPosition(7, [7, 7])).toBe(50);
  });

  it('builds one domain that covers every band and marker with some air', () => {
    const [low, high] = sharedDomain(
      [
        { min: 100, p25: 120, median: 140, p75: 160, max: 200 },
        { min: 150, p25: 170, median: 180, p75: 190, max: 300 },
      ],
      [320],
    );
    expect(low).toBeLessThan(100);
    expect(high).toBeGreaterThan(320);
  });

  it('renders the middle 50% and the median where the statistics say', () => {
    const fixture = TestBed.createComponent(PayBand);
    fixture.componentRef.setInput('stats', { min: 0, p25: 25, median: 50, p75: 75, max: 100 });
    fixture.componentRef.setInput('domain', [0, 100]);
    fixture.componentRef.setInput('marker', 90);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    const iqr = el.querySelector<HTMLElement>('.iqr')!;
    expect(iqr.style.left).toBe('25%');
    expect(iqr.style.width).toBe('50%');
    expect(el.querySelector<HTMLElement>('.median')!.style.left).toBe('50%');
    expect(el.querySelector<HTMLElement>('.marker')!.style.left).toBe('90%');
  });
});
