# Design system — ACME People & Pay

Recorded from the shipped build (`frontend/src/styles.scss`, `frontend/src/app/theme.ts`). Mode: Operate — an HR workbench in the established HR-suite canon (bar: Rippling, Pave, Deel).

## Principle
One visual idiom: the **pay band** (min–max whisker, shaded middle 50%, median tick, optional amber marker for "this person"). It appears on the overview (countries on one USD scale), the country page (job titles on one local-currency scale) and the profile (person against peers). Everything else stays quiet so the band carries meaning.

## Color
| Token | Value | Use |
|---|---|---|
| `--plum-900` | `#2a1430` | Sidebar |
| `--plum-600` | `#6c3168` | Primary actions, median tick, links, focus |
| `--plum-50` / `--plum-100` | `#f8f1f8` / `#efdcef` | Selection, highlights, active chips |
| `--amber` | `#f0b43c` | **Only** "this person" marker; `--amber-50` for the selected table row |
| `--canvas` / `--surface` / `--surface-sunken` | `#f7f6f8` / `#fff` / `#faf9fb` | Page, panels, table headers and inset blocks |
| `--ink` / `--ink-2` / `--ink-3` | `#1f1c24` / `#57515f` / `#6a6472` | Text tiers (all ≥ 4.5:1 on white) |
| `--line` / `--line-strong` | `#e7e3ea` / `#d6d0db` | Hairlines, input borders |
| `--band-track` / `--band-iqr` | `#948d9c` / `#c9a5c8` | Pay band (track ≥ 3:1 for graphics) |
| `--positive` / `--negative` | `#1d7348` / `#b42318` | Raises, within-band; cuts, below-band, destructive |

## Type
Hanken Grotesk (variable, self-hosted), tabular numerals globally. Scale: page title 1.5rem/700, panel title 0.9375rem/650, figures 1.375rem/650, body 0.875rem, tables 0.8125rem, secondary 0.75rem.

## Shape and layout
6px radii on panels, strips and controls; hairline borders, no shadows except overlays. 232px sidebar, collapsing to a top bar under 900px. Figures sit in one hairline-divided **summary strip**, never a card grid. Dense tables (≈36px rows) with sunken headers; secondary columns drop at 1280 / 1100 / 640px.

## Components
PrimeNG 21 on a custom Aura preset (plum primary, warm-grey surfaces). Hand-built: pay band, band legend, initials avatar. The profile and the add/edit form share one right-hand drawer; delete uses a confirm dialog (protected focus for a destructive action).

## Money
Local currency for anything within a country (₹ in lakh/crore grouping); every converted figure is prefixed "≈" and labelled USD.
