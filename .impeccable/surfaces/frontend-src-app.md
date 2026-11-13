---
version: 1
slug: "frontend-src-app"
primary_target: "frontend/src/app"
related_targets: []
---

# Surface: ACME People & Pay web app (all routes)

Mode: Operate. Audience: HR manager at a desk, daylight, long sessions beside Excel. Tasks: find and edit an employee, record a salary change with reason, answer pay questions by country / job title, see a person against peers, export CSV.

## Direction contract

THESIS: A compensation workbench in the established HR-suite canon (bar: Rippling, Pave, Deel), not a dashboard of cards. Refuses the KPI-card grid and chart-library defaults; the pay band (min-P25-median-P75-max range bar) is the one visual idiom, reused on every screen.

OWN-WORLD: Light canvas #f7f6f8, white work surfaces with hairline borders, deep plum sidebar #2a1430 and plum primary actions, a single amber marker (#f0b43c) reserved for "this person" / current selection. Hanken Grotesk throughout, tabular numerals, dense 13-14px tables, PrimeIcons line icons, 6px radii, no shadows except overlays.

STORY: Open on the org's pay at a glance, drill into a country, then a job title, then the people in it, then one person's history and peer position; every figure says its currency.

FIRST VIEWPORT: Sidebar left (232px). Pay overview: title + one-line context; a single four-cell summary strip; below, a two-thirds table "Pay by country" with USD-normalised bands sharing one scale, one-third "Payroll by department" ranked bars. Primary action lives in People ("Add employee").

FORM: Canon (user-pinned competitor-like path), code-led; no seed key (roll overridden by user pin).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
