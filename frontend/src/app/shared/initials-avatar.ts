import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** "Priya Sharma" -> "PS". */
export function initialsOf(fullName: string): string {
  const parts = fullName.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) {
    return '?';
  }
  const first = parts[0][0];
  const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
  return (first + last).toUpperCase();
}

const TINTS = [
  ['#f8f1f8', '#572654'],
  ['#fdf3df', '#7a4f00'],
  ['#e9f2f1', '#1f5a52'],
  ['#eef0f8', '#33407a'],
  ['#f6efe9', '#6e4426'],
];

@Component({
  selector: 'app-initials-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `{{ initials() }}`,
  host: {
    'aria-hidden': 'true',
    '[style.background]': 'tint()[0]',
    '[style.color]': 'tint()[1]',
    '[class.is-large]': "size() === 'large'",
  },
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex: none;
      width: 2rem;
      height: 2rem;
      border-radius: 50%;
      font-size: 0.75rem;
      font-weight: 650;
      letter-spacing: 0.02em;
    }
    :host(.is-large) {
      width: 3rem;
      height: 3rem;
      font-size: 1rem;
    }
  `,
})
export class InitialsAvatar {
  readonly name = input.required<string>();
  readonly size = input<'small' | 'large'>('small');

  protected readonly initials = computed(() => initialsOf(this.name()));
  protected readonly tint = computed(() => {
    let hash = 0;
    for (const char of this.name()) {
      hash = (hash * 31 + char.charCodeAt(0)) | 0;
    }
    return TINTS[Math.abs(hash) % TINTS.length];
  });
}
